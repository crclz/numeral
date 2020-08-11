package fullforum.controllers;

import fullforum.data.models.Access;
import fullforum.data.models.Document;
import fullforum.data.models.Favorite;
import fullforum.data.repos.DocumentRepository;
import fullforum.dto.in.CreateDocumentModel;
import fullforum.dto.in.PatchDocumentModel;
import fullforum.dto.out.IdDto;
import fullforum.dto.out.QDocument;
import fullforum.errhand.ForbidException;
import fullforum.errhand.NotFoundException;
import fullforum.errhand.UnauthorizedException;
import fullforum.services.IAuth;
import fullforum.services.Snowflake;
import org.hibernate.cfg.NotYetImplementedException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.print.Doc;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
@Validated// PathVariable and params auto validation
public class DocumentController {
    @Autowired
    Snowflake snowflake;

    @Autowired
    DocumentRepository documentRepository;

    @Autowired
    IAuth auth;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    EntityManager entityManager;


    @PostMapping
    public IdDto createDocument(@RequestBody CreateDocumentModel model) {
        if(!auth.isLoggedIn()) {
            throw new UnauthorizedException();
        }
        var document = new Document(snowflake.nextId(), auth.userId(), model.title, model.description, model.data);
        documentRepository.save(document);
        return new IdDto(document.getId());
    }

    @PatchMapping("{id}")

    public void patchDocument(@RequestBody PatchDocumentModel model, @PathVariable long id)
    {
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
        var document = documentRepository.findById(id).orElse(null);
        if (document == null) {
            return null;
        }
        return QDocument.convert(document, modelMapper);
    }


    @GetMapping
    public List<QDocument> getDocuments(
            @RequestParam(required = false) Long creatorId,
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) Boolean myfavorite,
            @RequestParam(required = false) Boolean isAbandoned
    ) {

        System.out.println(creatorId + " " + teamId + " " + myfavorite + " " + isAbandoned);
        List results;
        List<QDocument> documents = new ArrayList<>();

        if (myfavorite != null && myfavorite) {
            if (!auth.isLoggedIn()) {
                throw new UnauthorizedException();
            }
            var query = entityManager.createQuery(
                    "select d from Document d join Favorite f" +
                            " on d.id = f.documentId" +
                            " where f.userId = :userId")
                    .setParameter("userId", auth.userId());
            results = query.getResultList();
            for (var result : results) {
                var objs = (Object[]) result;
                var document = (Document)objs[0];
                documents.add(QDocument.convert(document, modelMapper));
            }
            return documents;

        } else if (isAbandoned != null && isAbandoned) {
            if (!auth.isLoggedIn()) {
                throw new UnauthorizedException();
            }
            var query = entityManager.createQuery(
                    "select d from Document d" +
                            " where d.creatorId = :userId " +
                            " and d.isAbandoned = true")
                    .setParameter("userId", auth.userId());
            results = query.getResultList();
        } else {
            var query = entityManager.createQuery(
                    "select d from Document d" +
                            " where (:creatorId is null or d.creatorId = :creatorId)" +
                            " and (:teamId is null or d.teamId = :teamId)")
                    .setParameter("creatorId", creatorId)
                    .setParameter("teamId", teamId);
            results = query.getResultList();
        }
        for (var result : results) {
            var document = (Document)result;
            documents.add(QDocument.convert(document, modelMapper));
        }
        return documents;
    }


}
