package fullforum.controllers;


import fullforum.data.models.*;
import fullforum.data.repos.*;
import fullforum.dto.in.CreateReplyModel;
import fullforum.dto.out.IdDto;
import fullforum.dto.out.QReply;
import fullforum.dto.out.Quser;
import fullforum.dto.out.UserPermission;
import fullforum.errhand.BadRequestException;
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
@RequestMapping("/api/replies")
@Validated
public class ReplyController {
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
    ThumbRepository thumbRepository;

    @Autowired
    ReplyRepository replyRepository;

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    MembershipRepository membershipRepository;

    @PostMapping
    public IdDto createReply(@RequestBody @Valid CreateReplyModel model) {
        if (!auth.isLoggedIn()) {
            throw new UnauthorizedException();
        }

        var comment = commentRepository.findById(model.commentId).orElse(null);
        if (comment == null) {
            throw new NotFoundException("操作失败，评论不存在");
        }

        var reply = new Reply(snowflake.nextId(), comment.getId(), auth.userId(), model.targetUserId, model.content);
        replyRepository.save(reply);

        if (reply.getUserId() != reply.getTargetUserId()) {//只有来自他人的回复才通知评论/回复作者
            var sender = userRepository.findById(auth.userId()).orElse(null);
            assert sender != null;
            var message = new Message(snowflake.nextId(), auth.userId(), reply.getTargetUserId());
            message.setTitle("评论回复通知");
            message.setContent(sender.getUsername() + " 回复了你");
            message.setLink("/readFile/" + comment.getDocumentId());

            messageRepository.save(message);
        }


        return new IdDto(reply.getId());
    }

    @DeleteMapping("{id}")
    public void deleteReply(@PathVariable long id) {
        if (!auth.isLoggedIn()) {
            throw new UnauthorizedException();
        }

        var reply = replyRepository.findById(id).orElse(null);
        if (reply == null) {
            throw new NotFoundException("操作失败，回复不存在");
        }
        if (auth.userId() != reply.getUserId()) {
            throw new ForbidException("操作失败，你没有权限");
        }

        replyRepository.deleteById(id);
    }

    @GetMapping("{id}")
    public QReply getReplyById(@PathVariable long id) {
        if (!auth.isLoggedIn()) {
            throw new UnauthorizedException();
        }
        var reply = replyRepository.findById(id).orElse(null);
        if (reply == null) {
            throw new NotFoundException("操作失败，回复不存在");
        }

        var user = userRepository.findById(reply.getUserId()).orElse(null);
        assert user != null;
        var qUser = Quser.convert(user, mapper);

        var thumb = thumbRepository.findByUserIdAndTargetId(auth.userId(), reply.getId());
        return QReply.convert(reply, qUser, mapper, thumb);
    }

    @GetMapping()
    public List<QReply> getReplies(@RequestParam(required = false) Long commentId) {
        if (!auth.isLoggedIn()) {
            throw new UnauthorizedException();
        }
        var query = entityManager.createQuery(
                "select r, t from Reply r left join Thumb t " +
                        "on (r.userId = t.userId and r.id = t.targetId) " +
                        "where (:cid is null or r.commentId = :cid) ")
                .setParameter("cid", commentId);
        var results = query.getResultList();
        var replies = new ArrayList<QReply>();
        for (Object result : results) {
            var objs = (Object[]) result;
            var reply = (Reply) (objs)[0];
            Thumb thumb;
            if (objs[1] != null) {
                thumb = (Thumb) objs[1];
            } else {
                thumb = null;
            }
            var user = userRepository.findById(reply.getUserId()).orElse(null);
            assert user != null;
            var qUser = Quser.convert(user, mapper);

            replies.add(QReply.convert(reply, qUser, mapper, thumb));
        }

        return replies;
    }
}
