package fullforum.controllers;


import fullforum.data.models.Message;
import fullforum.data.models.Reply;
import fullforum.data.repos.*;
import fullforum.dto.in.CreateReplyModel;
import fullforum.dto.out.IdDto;
import fullforum.dto.out.QReply;
import fullforum.dto.out.Quser;
import fullforum.errhand.ForbidException;
import fullforum.errhand.NotFoundException;
import fullforum.errhand.UnauthorizedException;
import fullforum.services.IAuth;
import fullforum.services.Snowflake;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("api/reply")
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

        var sender = userRepository.findById(auth.userId()).orElse(null);
        assert sender != null;
        var message = new Message(snowflake.nextId(), auth.userId(), reply.getTargetUserId());
        message.setTitle("评论回复通知");
        message.setContent(sender.getUsername() + " 回复了你的评论");
        messageRepository.save(message);

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
        return QReply.convert(reply, qUser, mapper);
    }

    @GetMapping()
    public List<QReply> getReplies(
            @RequestParam(required = false) Long commentId,
            @RequestParam(required = false) Long userId
    ) {
        if (!auth.isLoggedIn()) {
            throw new UnauthorizedException();
        }
        var query = entityManager.createQuery(
                "select r from Reply r " +
                    "where (:cid is null or r.commentId = :cid) " +
                    "and (:uid is null or r.userId = :uid)")
                .setParameter("cid", commentId)
                .setParameter("uid", userId);
        var results = query.getResultList();
        var replies = new ArrayList<QReply>();
        for (Object result : results) {
            var reply = (Reply) result;
            var user = userRepository.findById(reply.getUserId()).orElse(null);
            assert user != null;
            var qUser = Quser.convert(user, mapper);

            replies.add(QReply.convert(reply, qUser, mapper));
        }

        return replies;
    }
}
