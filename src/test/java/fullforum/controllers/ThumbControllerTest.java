package fullforum.controllers;

import fullforum.BaseTest;
import fullforum.data.models.Comment;
import fullforum.data.models.TargetType;
import fullforum.data.models.Thumb;
import fullforum.data.models.User;
import fullforum.data.repos.CommentRepository;
import fullforum.data.repos.ReplyRepository;
import fullforum.data.repos.ThumbRepository;
import fullforum.data.repos.UserRepository;
import fullforum.dependency.FakeAuth;

import fullforum.dto.in.CreatThumbUpModel;
import fullforum.errhand.ForbidException;
import fullforum.errhand.NotFoundException;
import fullforum.errhand.UnauthorizedException;
import fullforum.services.Snowflake;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.junit.jupiter.api.Assertions.*;


public class ThumbControllerTest extends BaseTest {
    @Autowired
    FakeAuth auth;

    @Autowired
    Snowflake snowflake;

    @Autowired
    ThumbController thumbController;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ThumbRepository thumbRepository;

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    ReplyRepository replyRepository;

    //test giveThumbUp

    @Test
    void giveThumbUp_throw_UnauthorizedException_when_user_is_not_log_in() {
        assertThrows(UnauthorizedException.class, () -> thumbController.giveThumbUp(null));
    }

    @Test
    void giveThumbUp_throw_NotFoundException_when_target_is_not_exist() {
        var user = new User(1L, "Dasdads", "SAddadsa", "Dadsa", "DSdad");
        userRepository.save(user);
        auth.setRealUserId(1);
        var model = new CreatThumbUpModel(10L, TargetType.Comment);
        assertThrows(NotFoundException.class, () -> thumbController.giveThumbUp(model));
    }


    @Test
    void giveThumbUp_return_id_and_update_db_when_all_ok() {
        var user = new User(333, "dsaadsa", "Dsadsdadd","Dsadsdad", "Asdda");
        userRepository.save(user);

        var comment = new Comment(10L, 999, 888, "Dsadad");
        commentRepository.save(comment);

        auth.setRealUserId(333);

        var model = new CreatThumbUpModel(10L, TargetType.Comment);
        var tid = thumbController.giveThumbUp(model);

        var thumbInDb = thumbRepository.findById(tid.id).orElse(null);
        assertNotNull(thumbInDb);
        assertEquals(thumbInDb.getUserId(), auth.userId());
        assertEquals(thumbInDb.getTargetId(), model.targetId);

    }

    //test deleteThumb

    @Test
    void deleteThumb_throw_UnauthorizedException_when_user_is_not_log_in() {
        assertThrows(UnauthorizedException.class, () -> thumbController.deleteThumbUp(9L));
    }

    @Test
    void deleteThumb_throw_NOtFoundException_when_thumb_is_exist() {
        auth.setRealUserId(1);
        assertThrows(NotFoundException.class, () -> thumbController.deleteThumbUp(9L));
    }

    @Test
    void deleteThumb_throw_ForbidException_when_user_is_not_creator() {
        var thumb = new Thumb(9L, 2L, 99L, TargetType.Comment);
        thumbRepository.save(thumb);

        auth.setRealUserId(1);
        assertThrows(ForbidException.class, () -> thumbController.deleteThumbUp(9L));
    }

    @Test
    void deleteThumb_return_ok_and_update_db_when_all_ok() {
        var thumb = new Thumb(9L, 1L, 99L, TargetType.Comment);
        thumbRepository.save(thumb);

        auth.setRealUserId(1);
        thumbController.deleteThumbUp(9L);
        var thumbInDb = thumbRepository.findById(9L).orElse(null);
        assertNull(thumbInDb);
    }


    //test getThumbById
    @Test
    void getThumbById_throw_UnauthorizedException_when_user_is_not_log_in() {
        assertThrows(UnauthorizedException.class, () -> thumbController.getThumbById(9L));
    }

    @Test
    void getThumbById_throw_NOtFoundException_when_thumb_is_exist() {
        auth.setRealUserId(1);
        assertThrows(NotFoundException.class, () -> thumbController.getThumbById(9L));
    }

    @Test
    void getThumbById_return_thumb_when_all_ok() {
        var thumb = new Thumb(9L, 1L, 99L, TargetType.Comment);
        thumbRepository.save(thumb);
        auth.setRealUserId(1);

        var thumbInDb = thumbController.getThumbById(9L);
        assertNotNull(thumbInDb);
        assertEquals(thumbInDb.getTargetId(), thumb.getTargetId());
        assertEquals(thumbInDb.getUserId(), thumb.getUserId());
    }



}
