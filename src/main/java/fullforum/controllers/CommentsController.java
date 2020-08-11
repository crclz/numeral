package fullforum.controllers;

import fullforum.data.models.Access;
import fullforum.data.models.Comment;
import fullforum.data.models.Document;
import fullforum.data.repos.CommentRepository;
import fullforum.data.repos.DocumentRepository;
import fullforum.data.repos.UserRepository;
import fullforum.dto.in.CreateCommentModel;
import fullforum.dto.out.IdDto;
import fullforum.dto.out.QComment;
import fullforum.dto.out.QDocument;
import fullforum.dto.out.Quser;
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
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Transactional
@RestController
@RequestMapping("/api/comments")
@Validated
public class CommentsController {
    @Autowired
    IAuth auth;

    @Autowired
    Snowflake snowflake;

    @Autowired
    ModelMapper mapper;

    @Autowired
    EntityManager entityManager;
    @Autowired
    DocumentRepository documentRepository;

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    UserRepository userRepository;

    @PostMapping
    public IdDto createComment(@RequestBody CreateCommentModel model) {
        if (!auth.isLoggedIn()) {
            throw new UnauthorizedException();
        }
        var document = documentRepository.findById(model.documentId).orElse(null);
        if (document == null) {
            throw new NotFoundException();
        }
        if (!document.getPublicCommentAccess().equals(Access.ReadWrite)) {
            throw new ForbidException();
        }
        var comment = new Comment(snowflake.nextId(), model.documentId, auth.userId(), model.content);
        commentRepository.save(comment);
        return new IdDto(comment.getId());
    }

    @DeleteMapping("{id}")
    public void removeComment(@PathVariable long id) {
        if (!auth.isLoggedIn()) {
            throw new UnauthorizedException();
        }
        var comment = commentRepository.findById(id).orElse(null);
        if (comment == null) {
            throw new NotFoundException();
        }
        if (comment.getUserId() != auth.userId()) {
            throw new ForbidException();
        }
        commentRepository.deleteById(id);
    }

    @GetMapping("{id}")
    public QComment getCommentById(@PathVariable long id) {
        var comment = commentRepository.findById(id).orElse(null);
        if (comment == null) {
            return null;
        }
        var qUser = Quser.convert(userRepository.findById(comment.getUserId()).orElse(null), mapper);
        return QComment.convert(comment, qUser, mapper);
    }

    @GetMapping
    public List<QComment> getComments(
            @RequestParam(required = false) Long documentId,
            @RequestParam(required = false) Long userId
    ) {
        List<QComment> comments = new ArrayList<>();

        var query = entityManager.createQuery(
                "select c from Comment c" +
                        " where (:documentId is null or c.documentId = :documentId)" +
                        " and (:userId is null or c.userId = :userId)")
                .setParameter("documentId", documentId)
                .setParameter("userId", userId);

        var results = query.getResultList();
        for (var result : results) {
            var comment = (Comment)result;
            var qUser = Quser.convert(userRepository.findById(comment.getUserId()).orElse(null), mapper);
            comments.add(QComment.convert(comment, qUser, mapper));
        }
        return comments;
    }
}
