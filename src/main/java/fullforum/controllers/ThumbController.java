package fullforum.controllers;

import fullforum.data.models.Message;
import fullforum.data.models.TargetType;
import fullforum.data.models.Thumb;
import fullforum.data.repos.*;
import fullforum.dto.in.CreatThumbUpModel;
import fullforum.dto.out.IdDto;
import fullforum.errhand.ForbidException;
import fullforum.errhand.NotFoundException;
import fullforum.errhand.UnauthorizedException;
import fullforum.services.IAuth;
import fullforum.services.Snowflake;
import org.hibernate.cfg.NotYetImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("api/thumb")
public class ThumbController {
    @Autowired
    IAuth auth;

    @Autowired
    Snowflake snowflake;

    @Autowired
    UserRepository userRepository;

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    ThumbRepository thumbRepository;

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    ReplyRepository replyRepository;


    @PostMapping
    public IdDto giveThumbUp(@RequestBody CreatThumbUpModel model) {
        if (!auth.isLoggedIn()) {
            throw new UnauthorizedException();
        }
        var user = userRepository.findById(auth.userId()).orElse(null);
        assert user != null;


        Thumb thumb;
        if (model.targetType == TargetType.Reply) {
            var reply = replyRepository.findById(model.targetId).orElse(null);
            if (reply == null) {
                throw new NotFoundException("回复不存在");
            }
            thumb = new Thumb(snowflake.nextId(), auth.userId(), reply.getId(), TargetType.Reply);
            thumbRepository.save(thumb);
            reply.thumbUp();
            replyRepository.save(reply);

            var message = new Message(snowflake.nextId(), auth.userId(), reply.getUserId());
            message.setTitle("收到点赞通知");
            message.setContent(user.getUsername() + " 赞了你的回复");
            messageRepository.save(message);
        } else {
            var comment = commentRepository.findById(model.targetId).orElse(null);
            if (comment == null) {
                throw new NotFoundException("评论不存在");
            }
            thumb = new Thumb(snowflake.nextId(), auth.userId(), comment.getId(), TargetType.Comment);
            thumbRepository.save(thumb);
            comment.thumbUp();
            commentRepository.save(comment);
            var message = new Message(snowflake.nextId(), auth.userId(), comment.getUserId());
            message.setTitle("收到点赞通知");
            message.setContent(user.getUsername() + " 赞了你的评论");
            messageRepository.save(message);
        }
        return new IdDto(thumb.getId());
    }

    @DeleteMapping("{id}")
    public void deleteThumbUp(@PathVariable Long id) {
        if (!auth.isLoggedIn()) {
            throw new UnauthorizedException();
        }

        var thumb = thumbRepository.findById(id).orElse(null);
        if (thumb == null) {
            throw new NotFoundException("点赞记录不存在");
        }
        if (auth.userId() != thumb.getUserId()) {
            throw new ForbidException("你没有权限");
        }
        thumbRepository.deleteById(id);
    }

    @GetMapping("{id}")
    public Thumb getThumbById(@PathVariable Long id) {
        throw new NotYetImplementedException();
    }



}
