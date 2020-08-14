package fullforum.controllers;

import fullforum.BaseTest;
import fullforum.data.repos.DocumentRepository;
import fullforum.data.repos.MessageRepository;
import fullforum.data.repos.TeamRepository;
import fullforum.data.repos.UserRepository;
import fullforum.dependency.FakeAuth;
import fullforum.dto.in.CreateMessageModel;
import fullforum.errhand.UnauthorizedException;
import fullforum.services.IAuth;
import fullforum.services.Snowflake;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class MessageControllerTest extends BaseTest {
//    @Autowired
//    FakeAuth auth;
//
//    @Autowired
//    Snowflake snowflake;
//
//    @Autowired
//    MessageController messageController;
//
//    @Autowired
//    TeamRepository teamRepository;
//
//    @Autowired
//    UserRepository userRepository;
//
//    @Autowired
//    DocumentRepository documentRepository;
//
//    @Autowired
//    MessageRepository messageRepository;
//
//    //test createMessage
//
//    @Test
//    void creatMessage_throw_UnauthorizedException_when_user_is_not_log_in() {
//        var model = new CreateMessageModel();
//        assertThrows(UnauthorizedException.class, () -> messageController.createMessage(model));
//    }
//
//
//    @Test
//    void creatMessage_return_message_id_and_update_db_when_all_ok() {
////        assertThrows(UnauthorizedException.class, () -> messageController.createMessage());
//    }
//
//
//    //test readAllMessage
//
//    @Test
//    void readAllMessage_throw_UnauthorizedException_when_user_is_not_log_in() {
//        assertThrows(UnauthorizedException.class, () -> messageController.readAllMessages());
//    }
//
//    @Test
//    void readAllMessage_return_ok_and_update_db_when_all_ok() {
//
//
//
//    }


}
