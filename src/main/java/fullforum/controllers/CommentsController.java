package fullforum.controllers;

import fullforum.data.models.*;
import fullforum.data.repos.*;
import fullforum.dto.in.CreateCommentModel;
import fullforum.dto.out.IdDto;
import fullforum.dto.out.QComment;
import fullforum.dto.out.Quser;
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
    ThumbRepository thumbRepository;

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
        var userPermission = getCurrentUserPermission(document.getId());

        if (userPermission.commentAccess.equals(Access.ReadWrite)) {
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
        var document = documentRepository.findById(comment.getDocumentId()).orElse(null);
        assert document != null;
        if (comment.getUserId() != auth.userId() && document.getCreatorId() != auth.userId()) {
            throw new ForbidException("操作失败，你没有权限");
        }

        commentRepository.deleteById(id);
        replyRepository.deleteAllByCommentId(id);
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
        var thumb = thumbRepository.findByUserIdAndTargetId(auth.userId(), comment.getId());

        return QComment.convert(comment, qUser, mapper, thumb);
    }

    @GetMapping
    public List<QComment> getComments(
            @RequestParam(required = false) Long documentId,
            @RequestParam(required = false) Long userId
    ) {
        if (!auth.isLoggedIn()) {
            throw new UnauthorizedException();
        }

        if (documentId != null) {
            var document = documentRepository.findById(documentId).orElse(null);
            if (document == null) {
                return new ArrayList<>();
            }

            var userPermission = getCurrentUserPermission(documentId);
            if (userPermission.commentAccess == Access.None) {
                return new ArrayList<>();
            }

        }
        List<QComment> comments = new ArrayList<>();

        var query = entityManager.createQuery(
                "select c, t from Comment c left join Thumb t " +
                        " on (c.userId = t.userId and c.id = t.targetId)" +
                        " where (:documentId is null or c.documentId = :documentId)" +
                        " and (:userId is null or c.userId = :userId)")
                .setParameter("documentId", documentId)
                .setParameter("userId", userId);

        var results = query.getResultList();

        for (var result : results) {
            var objs = (Object[])result;
            var comment = (Comment) (objs)[0];
            Thumb thumb;
            if (objs[1] != null) {
                thumb = (Thumb)objs[1];
            } else {
                thumb = null;
            }
            var qUser = Quser.convert(userRepository.findById(comment.getUserId()).orElse(null), mapper);
            comments.add(QComment.convert(comment, qUser, mapper, thumb));
        }
        return comments;
    }


    private AccessorLevel getAccessorLevel(Document document, long accessorId) {
        if (accessorId == document.getCreatorId()) {
            return AccessorLevel.self;
        }

        if (document.getTeamId() == null) {
            return AccessorLevel.publicLevel;
        }

        var membership = membershipRepository.findByUserIdAndTeamId(accessorId, document.getTeamId());
        if (membership != null) {
            return AccessorLevel.teamMember;
        } else {
            return AccessorLevel.publicLevel;
        }
    }

    @GetMapping("/permission/{id}")
    public UserPermission getCurrentUserPermission(@PathVariable Long id) {
        if (!auth.isLoggedIn()) {
            throw new UnauthorizedException();
        }
        var document = documentRepository.findById(id).orElse(null);
        if (document == null) {
            throw new NotFoundException("文档不存在");
        }

        // 获取当前用户与文章的关系：(AccesserLevel)
        var level = getAccessorLevel(document, auth.userId());

        if (level == AccessorLevel.self) {
            return new UserPermission(auth.userId(), Access.ReadWrite, Access.ReadWrite, true);
        }

        if (level == AccessorLevel.teamMember) {
            return new UserPermission(auth.userId(), document.getTeamDocumentAccess(),
                    document.getTeamCommentAccess(), document.getTeamCanShare());
        }

        // public
        return new UserPermission(auth.userId(), document.getPublicDocumentAccess(),
                document.getPublicCommentAccess(), document.getPublicCanShare());
    }

    private enum AccessorLevel {
        publicLevel, teamMember, self
    }

}
