package fullforum.controllers;

import fullforum.data.models.Access;
import fullforum.data.models.Document;
import fullforum.data.repos.DocumentRepository;
import fullforum.data.repos.MembershipRepository;
import fullforum.data.repos.UserRepository;
import fullforum.dto.in.CreateDocumentModel;
import fullforum.dto.in.PatchDocumentModel;
import fullforum.dto.out.IdDto;
import fullforum.dto.out.QDocument;
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
    DocumentRepository documentRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    MembershipRepository membershipRepository;


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

        if (document.getPublicDocumentAccess().equals(Access.ReadWrite) || auth.userId() == document.getCreatorId()) {
            document.setData(model.data == null ? document.getData() : model.data);
            document.setTitle(model.title == null ? document.getTitle() : model.title);
            document.setDescription(model.description == null ? document.getDescription() : model.description);
        } else {
            throw new ForbidException();
        }

        if (auth.userId() == document.getCreatorId()) {
            document.setTeamId(model.teamId == null ? document.getTeamId() : model.teamId);
            document.setIsAbandoned(model.isAbandoned == null ? document.getIsAbandoned() : model.isAbandoned);
            document.setPublicDocumentAccess(model.publicDocumentAccess == null ? document.getPublicCommentAccess()
                    : model.publicDocumentAccess);
            document.setPublicCommentAccess(model.publicCommentAccess == null ? document.getPublicCommentAccess()
                    : model.publicCommentAccess);
            document.setPublicCanShare(model.publicCanShare == null ? document.getPublicCanShare()
                    : model.publicCanShare);
            document.setTeamDocumentAccess(model.teamDocumentAccess == null ? document.getTeamCommentAccess()
                    : model.teamDocumentAccess);
            document.setTeamCommentAccess(model.teamCommentAccess == null ? document.getTeamCommentAccess()
                    : model.teamCommentAccess);
            document.setTeamCanShare(model.teamCanShare == null ? document.getTeamCanShare()
                    : model.teamCanShare);
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
        if (document.getCreatorId() == auth.userId()) {
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
            return QDocument.convert(document, modelMapper);
        } else {
            if (document.getPublicDocumentAccess().equals(Access.None)) {
                throw new ForbidException();
            }
            return QDocument.convert(document, modelMapper);
        }
    }


    @GetMapping
    public List<QDocument> getDocuments(
            @RequestParam(required = false) Long creatorId,
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) Boolean myfavorite,
            @RequestParam(required = false) Boolean isAbandoned
    ) {
        if (!auth.isLoggedIn()) {
            throw new UnauthorizedException();
        }
        List results;
        List<QDocument> documents = new ArrayList<>();

        if (myfavorite != null && myfavorite) {
            var query = entityManager.createQuery(
                    "select d from Document d join Favorite f" +
                            " on d.id = f.documentId" +
                            " where f.userId = :userId" +
                            " and d.isAbandoned = false")
                    .setParameter("userId", auth.userId());
            results = query.getResultList();
            for (var result : results) {
                var document = (Document) result;
                documents.add(QDocument.convert(document, modelMapper));
            }
            return documents;

        } else if (isAbandoned != null && isAbandoned) {
            var query = entityManager.createQuery(
                    "select d from Document d" +
                            " where d.creatorId = :userId " +
                            " and d.isAbandoned = true")
                    .setParameter("userId", auth.userId());
            results = query.getResultList();
        } else {
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




}
