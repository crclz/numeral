package fullforum.controllers;

import fullforum.data.models.Access;
import fullforum.data.models.Comment;
import fullforum.data.models.Document;
import fullforum.data.models.ViewRecord;
import fullforum.data.repos.*;
import fullforum.dto.in.CreateDocumentModel;
import fullforum.dto.in.PatchDocumentModel;
import fullforum.dto.out.IdDto;
import fullforum.dto.out.QDocument;
import fullforum.dto.out.UserPermission;
import fullforum.errhand.ForbidException;
import fullforum.errhand.NotFoundException;
import fullforum.errhand.UnauthorizedException;
import fullforum.services.IAuth;
import fullforum.services.Snowflake;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@Transactional
@RestController
@RequestMapping("/api/documents")
@Validated// PathVariable and params auto validation
public class DocumentController {
    @Autowired
    Snowflake snowflake;

    @Autowired
    IAuth auth;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    EntityManager entityManager;

    @Autowired
    TeamRepository teamRepository;

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    DocumentRepository documentRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    MembershipRepository membershipRepository;

    @Autowired
    ViewRecordRepository viewRecordRepository;


    @PostMapping
    public IdDto createDocument(@RequestBody @Valid CreateDocumentModel model) {
        if (!auth.isLoggedIn()) {
            throw new UnauthorizedException();
        }
        var document = new Document(snowflake.nextId(), auth.userId(), model.title, model.description, model.data);
        documentRepository.save(document);
        return new IdDto(document.getId());
    }

    @PatchMapping("{id}")

    public void patchDocument(@RequestBody @Valid PatchDocumentModel model, @PathVariable long id) {
        if (!auth.isLoggedIn()) {
            throw new UnauthorizedException();
        }
        var document = documentRepository.findById(id).orElse(null);
        if (document == null) {
            throw new NotFoundException();
        }
        //检查权限
        boolean havePermission;
        if (auth.userId() == document.getCreatorId()) {
            havePermission = true;
        } else if (document.getTeamId() != null){ //团队文档
            var membership = membershipRepository.findByUserIdAndTeamId(auth.userId(), document.getTeamId());
            havePermission = (membership != null && document.getTeamDocumentAccess().equals(Access.ReadWrite));
        } else { //非团队文档
            havePermission = document.getPublicDocumentAccess().equals(Access.ReadWrite);
        }


        if (havePermission) {
            document.setData(model.data == null ? document.getData() : model.data);
            document.setTitle(model.title == null ? document.getTitle() : model.title);
            document.setDescription(model.description == null ? document.getDescription() : model.description);
        } else {
            throw new ForbidException();
        }

        if (document.getTeamId() != null) {
            var team = teamRepository.findById(document.getTeamId()).orElse(null);
            assert team != null;//删除team的时候会清空doc的teamId
            if (auth.userId() == document.getCreatorId() || auth.userId() == team.getLeaderId()) {
                document.setTeamId(model.teamId == null ? document.getTeamId() : model.teamId);
                document.setTeamDocumentAccess(model.teamDocumentAccess == null ? document.getTeamCommentAccess()
                        : model.teamDocumentAccess);
                document.setTeamCommentAccess(model.teamCommentAccess == null ? document.getTeamCommentAccess()
                        : model.teamCommentAccess);
                document.setTeamCanShare(model.teamCanShare == null ? document.getTeamCanShare()
                        : model.teamCanShare);
            }
        }

        if (auth.userId() == document.getCreatorId()) {
            document.setIsAbandoned(model.isAbandoned == null ? document.getIsAbandoned() : model.isAbandoned);
            document.setPublicDocumentAccess(model.publicDocumentAccess == null ? document.getPublicCommentAccess()
                    : model.publicDocumentAccess);
            document.setPublicCommentAccess(model.publicCommentAccess == null ? document.getPublicCommentAccess()
                    : model.publicCommentAccess);
            document.setPublicCanShare(model.publicCanShare == null ? document.getPublicCanShare()
                    : model.publicCanShare);
        }
        document.updatedAtNow();
        document.setModifyCountAndModifier(auth.userId());
        documentRepository.save(document);

    }

    @DeleteMapping("{id}")
    public void removeDocument(@PathVariable Long id) {
        if (!auth.isLoggedIn()) {
            throw new UnauthorizedException();
        }
        var document = documentRepository.findById(id).orElse(null);
        if (document == null) {
            throw new NotFoundException();
        }
        if (document.getCreatorId() != auth.userId()) {
            throw new ForbidException();
        }

        var comments = commentRepository.findAllByDocumentId(id);
        for (Comment comment : comments) {
            commentRepository.deleteById(comment.getId());
        }

        documentRepository.deleteById(id);
    }

    @GetMapping("{id}")
    public QDocument getDocumentById(@PathVariable Long id) {
        if (!auth.isLoggedIn()) {
            throw new UnauthorizedException();
        }
        var document = documentRepository.findById(id).orElse(null);

        if (document == null) {
            return null;
        }

        var viewRecord = viewRecordRepository.findByDocumentIdAndUserId(document.getId(), auth.userId());
        if (viewRecord == null) {
            viewRecord = new ViewRecord(snowflake.nextId(), auth.userId(), document.getId());
        } else {
            viewRecord.updatedAtNow();
        }

        if (document.getCreatorId() == auth.userId()) {
            viewRecordRepository.save(viewRecord);
            return QDocument.convert(document, modelMapper);
        }

        if (document.getTeamId() != null) {
            var membership = membershipRepository.findByUserIdAndTeamId(auth.userId(), document.getTeamId());
            if (membership == null) {
                throw new ForbidException();
            }
            if (document.getTeamDocumentAccess().equals(Access.None)) {
                throw new ForbidException();
            }
            viewRecordRepository.save(viewRecord);
            return QDocument.convert(document, modelMapper);
        } else {
            if (document.getPublicDocumentAccess().equals(Access.None)) {
                throw new ForbidException();
            }
            viewRecordRepository.save(viewRecord);
            return QDocument.convert(document, modelMapper);
        }
    }


    @GetMapping
    public List<QDocument> getDocuments(
            @RequestParam(required = false) Long creatorId,
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) Boolean myfavorite,
            @RequestParam(required = false) Boolean isAbandoned,
            @RequestParam(required = false) Boolean recent
    ) {
        if (!auth.isLoggedIn()) {
            throw new UnauthorizedException();
        }
        List results;
        List<QDocument> documents = new ArrayList<>();

        if (myfavorite != null && myfavorite) { //返回当前用户收藏的文档
            var query = entityManager.createQuery(
                    "select d from Document d join Favorite f" +
                            " on d.id = f.documentId" +
                            " where f.userId = :userId" +
                            " and d.isAbandoned = false")
                    .setParameter("userId", auth.userId());
            results = query.getResultList();
        } else if (isAbandoned != null && isAbandoned) { //返回当前用户回收站内文档
            var query = entityManager.createQuery(
                    "select d from Document d" +
                            " where d.creatorId = :userId " +
                            " and d.isAbandoned = true")
                    .setParameter("userId", auth.userId());
            results = query.getResultList();
        } else if (recent != null && recent) { //返回当前用户最近浏览的文档
            var query = entityManager.createQuery(
                    "select d from Document d join ViewRecord v" +
                            " on d.id = v.documentId" +
                            " where (v.userId = :userId)" +
                            " and d.isAbandoned = false" +
                            " order by v.updatedAt desc " )
                    .setParameter("userId", auth.userId());
            results = query.getResultList();
            for (var result:results) {
                var document = (Document) result;
                documents.add(QDocument.convert(document, modelMapper));
                if (documents.size() >= 15) {
                    break;
                }
            }
            return documents;
        } else { //根据
            if (teamId != null) {
                var membership = membershipRepository.findByUserIdAndTeamId(auth.userId(), teamId);
                if (membership == null) {
                    throw new ForbidException();
                }
            }
            var query = entityManager.createQuery(
                    "select d from Document d" +
                            " where (:creatorId is null or d.creatorId = :creatorId)" +
                            " and (:teamId is null or d.teamId = :teamId)" +
                            " and d.isAbandoned = false")
                    .setParameter("creatorId", creatorId)
                    .setParameter("teamId", teamId);
            results = query.getResultList();
        }
        for (var result : results) {
            var document = (Document) result;
            if (auth.userId() != document.getCreatorId()) {//非文档创建者
                if (teamId != null && !document.getTeamDocumentAccess().equals(Access.None)) {//团队文档
                    documents.add(QDocument.convert(document, modelMapper));
                } else if (teamId == null && !document.getPublicDocumentAccess().equals(Access.None)) {//非团队文档
                    documents.add(QDocument.convert(document, modelMapper));
                }
            } else {
                documents.add(QDocument.convert(document, modelMapper));
            }
        }
        return documents;
    }

    @GetMapping("/permission/{id}")
    public UserPermission getCurrentUserPermission(@PathVariable Long id) {
        if (!auth.isLoggedIn()) {
            throw new UnauthorizedException();
        }
        var document = documentRepository.findById(id).orElse(null);
        if (document == null) {
            throw new NotFoundException();
        }

        if (auth.userId() == document.getCreatorId()) {
            return new UserPermission(auth.userId(), Access.ReadWrite, Access.ReadWrite, true);
        }

        if (document.getTeamId() != null) {
            var membership = membershipRepository.findByUserIdAndTeamId(auth.userId(), document.getTeamId());
            var team = teamRepository.findById(document.getTeamId()).orElse(null);
            assert team != null;

            if (membership == null) {
                return new UserPermission(auth.userId(), Access.None, Access.None, false);
            }

            if (team.getLeaderId() == auth.userId()) {
                return new UserPermission(auth.userId(), Access.ReadWrite, Access.ReadWrite,true);
            } else {
                return new UserPermission(auth.userId(), document.getTeamDocumentAccess(),
                        document.getTeamCommentAccess(), document.getTeamCanShare());
            }
        } else {
            return new UserPermission(auth.userId(), document.getPublicDocumentAccess(),
                    document.getPublicCommentAccess(), document.getPublicCanShare());
        }

    }




}
