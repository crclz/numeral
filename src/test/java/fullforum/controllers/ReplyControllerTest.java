package fullforum.controllers;

import fullforum.BaseTest;
import fullforum.data.models.Comment;
import fullforum.data.models.Message;
import fullforum.data.models.Reply;
import fullforum.data.models.User;
import fullforum.data.repos.*;
import fullforum.dependency.FakeAuth;
import fullforum.dto.in.CreateReplyModel;
import fullforum.dto.out.QReply;
import fullforum.errhand.NotFoundException;
import fullforum.errhand.UnauthorizedException;
import fullforum.services.Snowflake;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.*;

public class ReplyControllerTest extends BaseTest {
    @Autowired
    FakeAuth auth;

    @Autowired
    Snowflake snowflake;

    @Autowired
    ModelMapper mapper;

    @Autowired
    EntityManager entityManager;

    @Autowired
    ReplyController replyController;

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


    //test createReply

    @Test
    void createReply_throw_UnauthorizedException_when_user_is_not_log_in() {
        var model = new CreateReplyModel(10L, 100L, "DSa");
        assertThrows(UnauthorizedException.class, () -> replyController.createReply(model));
    }

    @Test
    void createReply_throw_NotFoundException_when_comment_is_not_exist() {
        auth.setRealUserId(1);
        var model = new CreateReplyModel(10L, 100L, "DSa");
        assertThrows(NotFoundException.class, () -> replyController.createReply(model));
    }

    @Test
    void createReply_return_id_and_update_db_when_all_ok() {
        var user = new User(1L, "sdasda", "sadsada", "Dasdad", "Asdd");
        userRepository.save(user);

        auth.setRealUserId(1);
        var comment = new Comment(10L, 222L, 23323L, "12313213");
        commentRepository.save(comment);

        var model = new CreateReplyModel(10L, 100L, "DSa");
        var rid = replyController.createReply(model);
        assertNotNull(rid);

        var replyInDb = replyRepository.findById(rid.id).orElse(null);
        assertNotNull(replyInDb);
        assertEquals(replyInDb.getCommentId(), comment.getId());
        assertEquals(replyInDb.getTargetUserId(), model.targetUserId);
        assertEquals(replyInDb.getUserId(), auth.userId());

        var messages = messageRepository.findAllByReceiverId(100L);
        for (Message message : messages) {
            assertEquals(message.getTitle(), "评论回复通知");
        }
    }

    //test deleteReply

    @Test
    void deleteReply_throw_UnauthorizedException_when_user_is_not_login() {
        assertThrows(UnauthorizedException.class, () -> replyController.deleteReply(2L));
    }

    @Test
    void deleteReply_throw_NotFoundException_when_reply_is_not_exist() {
        auth.setRealUserId(1);
        assertThrows(NotFoundException.class, () -> replyController.deleteReply(2L));
    }


    @Test
    void deleteReply_return_ok_and_update_db_when_all_ok() {
        auth.setRealUserId(1);
        var reply = new Reply(100L, 10L,100L, 200L, "!23213");
        replyRepository.save(reply);

        replyController.deleteReply(100L);

        var replyInDb = replyRepository.findById(100L).orElse(null);
        assertNull(replyInDb);
    }

    //test getReplyById
    @Test
    void getReplyById_throw_NotFoundException_when_user_is_not_login() {
        assertThrows(UnauthorizedException.class, () -> replyController.getReplyById(2L));

    }

    @Test
    void getReplyById_throw_NotFoundException_when_reply_is_not_exist() {
        auth.setRealUserId(1);
        assertThrows(NotFoundException.class, () -> replyController.getReplyById(2L));
    }

    @Test
    void getReplyById_return_reply_info_when_reply_exist() {
        auth.setRealUserId(1);
        var user = new User(100L, "32323", "@13123", "!@#231", "@!312312");
        userRepository.save(user);

        var reply = new Reply(100L, 10L,100L, 200L, "!23213");
        replyRepository.save(reply);

        var replyInDb = replyController.getReplyById(100L);
        assertNotNull(replyInDb);

        assertEquals(reply.getCommentId(), replyInDb.getCommentId());
        assertEquals(reply.getTargetUserId(), replyInDb.getTargetUserId());
    }

    //test getReplies

    @Test
    void getReplies_throw_UnauthorizedException_when_user_is_not_login() {
        assertThrows(UnauthorizedException.class, () -> replyController.getReplies(1L, 2L));
    }


    @Test
    void getReplies_return_list_of_reply_info_when_all_ok() {
        auth.setRealUserId(1);
        var user = new User(102L, "32323", "@13123", "!@#231", "@!312312");
        userRepository.save(user);

        var reply1 = new Reply(100L, 10L,100L, 201L, "!23213");
        var reply2 = new Reply(101L, 12L,102L, 203L, "!23213");
        var reply3 = new Reply(102L, 13L,104L, 201L, "!23213");
        var reply4 = new Reply(103L, 12L,100L, 203L, "!23213");
        var reply5 = new Reply(104L, 12L,102L, 209L, "!23213");


        replyRepository.save(reply1);
        replyRepository.save(reply2);
        replyRepository.save(reply3);
        replyRepository.save(reply4);
        replyRepository.save(reply5);


        var repliesInDb = replyController.getReplies(12L, 102L);
        assertEquals(repliesInDb.size(), 2);
        for (QReply qReply : repliesInDb) {
            assertEquals(qReply.getCommentId(), 12L);
            assertEquals(qReply.getUserId(),102L);
        }

    }

}
