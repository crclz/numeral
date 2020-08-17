package fullforum.controllers;

import fullforum.data.models.Access;
import fullforum.data.models.Comment;
import fullforum.data.models.Message;
import fullforum.data.repos.*;
import fullforum.dto.in.CreateCommentModel;
import fullforum.dto.out.IdDto;
import fullforum.dto.out.QComment;
import fullforum.dto.out.Quser;
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
    TeamRepository teamRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ReplyRepository replyRepository;

    @Autowired
    DocumentRepository documentRepository;

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    MembershipRepository membershipRepository;

    @PostMapping
    public IdDto createComment(@RequestBody @Valid CreateCommentModel model) {
        if (!auth.isLoggedIn()) {
            throw new UnauthorizedException();
        }
        var document = documentRepository.findById(model.documentId).orElse(null);
        if (document == null) {
            throw new NotFoundException("文档不存在！");
        }

        //检查权限
        boolean havePermission;
        if (auth.userId() == document.getCreatorId()) {
            havePermission = true;
        } else if (document.getTeamId() != null) { //团队文档
            var membership = membershipRepository.findByUserIdAndTeamId(auth.userId(), document.getTeamId());
            havePermission = (membership != null && document.getTeamCommentAccess().equals(Access.ReadWrite));
        } else { //非团队文档
            havePermission = document.getPublicCommentAccess().equals(Access.ReadWrite);
        }

        if (havePermission) {
            var comment = new Comment(snowflake.nextId(), model.documentId, auth.userId(), model.content);
            commentRepository.save(comment);

            if (auth.userId() != document.getCreatorId()) {//只有来自他人的评论才通知文章作者
                var message = new Message(snowflake.nextId(), -1L, document.getCreatorId());
                message.setTitle("新评论通知");
                message.setContent("你的文档 " + document.getTitle() + " 收到一条新评论");
                message.setLink("/readFile/" + document.getId());
                messageRepository.save(message);
            }
            return new IdDto(comment.getId());
        } else {
            throw new ForbidException("评论失败，你没有权限");
        }
    }

    @DeleteMapping("{id}")
    public void removeComment(@PathVariable long id) {
        if (!auth.isLoggedIn()) {
            throw new UnauthorizedException();
        }
        var comment = commentRepository.findById(id).orElse(null);
        if (comment == null) {
            throw new NotFoundException("评论不存在");
        }
        if (comment.getUserId() != auth.userId()) {
            throw new ForbidException("操作失败，你没有权限");
        }

        replyRepository.deleteAllByCommentId(id);

        commentRepository.deleteById(id);
    }

    @GetMapping("{id}")
    public QComment getCommentById(@PathVariable long id) {
        if (!auth.isLoggedIn()) {
            throw new UnauthorizedException();
        }

        var comment = commentRepository.findById(id).orElse(null);
        if (comment == null) {
            throw new NotFoundException("评论不存在");
        }
        var qUser = Quser.convert(userRepository.findById(comment.getUserId()).orElse(null), mapper);
        return QComment.convert(comment, qUser, mapper);
    }

    @GetMapping
    public List<QComment> getComments(
            @RequestParam(required = false) Long documentId,
            @RequestParam(required = false) Long userId
    ) {
        if (!auth.isLoggedIn()) {
            throw new UnauthorizedException();
        }

        boolean havePermission = false;

        if (documentId != null) {
            var document = documentRepository.findById(documentId).orElse(null);
            if (document == null) {
                return new ArrayList<>();
            }
            if (document.getTeamId() != null) { //团队文档则按照团队权限
                var team = teamRepository.findById(document.getTeamId()).orElse(null);
                assert team != null;
                var leaderId = team.getLeaderId();
                var membership = membershipRepository.findByUserIdAndTeamId(auth.userId(), team.getId());
                havePermission = (auth.userId() == document.getCreatorId()
                        || auth.userId() == leaderId
                        || (membership != null && !document.getTeamCommentAccess().equals(Access.None)));
            } else {
                havePermission = (auth.userId() == document.getCreatorId()
                        || !document.getPublicCommentAccess().equals(Access.None));
            }
        }

        if (!havePermission) {
            throw new ForbidException("操作失败，你没有权限");
        }


        List<QComment> comments = new ArrayList<>();

        var query = entityManager.createQuery(
                "select c from Comment c" +
                        " where (:documentId is null or c.documentId = :documentId)" +
                        " and (:userId is null or c.userId = :userId)")
                .setParameter("documentId", documentId)
                .setParameter("userId", userId);

        var results = query.getResultList();

        for (var result : results) {
            var comment = (Comment) result;
            var qUser = Quser.convert(userRepository.findById(comment.getUserId()).orElse(null), mapper);
            comments.add(QComment.convert(comment, qUser, mapper));
        }
        return comments;
    }


}
