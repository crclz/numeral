package fullforum.controllers;

import fullforum.BaseTest;
import fullforum.data.models.Message;
import fullforum.data.models.User;
import fullforum.data.repos.DocumentRepository;
import fullforum.data.repos.MessageRepository;
import fullforum.data.repos.TeamRepository;
import fullforum.data.repos.UserRepository;
import fullforum.dependency.FakeAuth;
import fullforum.dto.in.BatchMarkReadModel;
import fullforum.dto.in.CreateMessageModel;
import fullforum.errhand.ForbidException;
import fullforum.errhand.NotFoundException;
import fullforum.errhand.UnauthorizedException;
import fullforum.services.Snowflake;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class MessageControllerTest extends BaseTest {
    @Autowired
    FakeAuth auth;

    @Autowired
    Snowflake snowflake;

    @Autowired
    MessageController messageController;

    @Autowired
    TeamRepository teamRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    DocumentRepository documentRepository;

    @Autowired
    MessageRepository messageRepository;

    //test createMessage

    @Test
    void creatMessage_throw_UnauthorizedException_when_user_is_not_log_in() {
        var model = new CreateMessageModel();
        assertThrows(UnauthorizedException.class, () -> messageController.createMessage(model));
    }

    @Test
    void createMessage_throw_NotFoundException_when_receiver_is_not_exist() {
        auth.setRealUserId(1);

        var model = new CreateMessageModel(-1L, 2L, "23131", "2311231", "13312");

        assertThrows(NotFoundException.class, () -> messageController.createMessage(model));
    }


    @Test
    void creatMessage_return_message_id_and_update_db_when_all_ok() {
        auth.setRealUserId(1);
        var user = new User(1L, "dasd", "dasdasdas", "DSadadada", "DASdas");
        userRepository.save(user);

        var model = new CreateMessageModel(-1L, auth.userId(), "23131", "2311231", "13312");
        var mid = messageController.createMessage(model);
        var messageInDb = messageRepository.findById(mid.id).orElse(null);

        assertNotNull(messageInDb);
        assertEquals(messageInDb.getSenderId(), -1);
        assertEquals(messageInDb.getReceiverId(), model.getReceiverId());
        assertEquals(messageInDb.getTitle(), model.getTitle());
    }

    //test deleteMessage

    @Test
    void deleteMessage_throw_UnauthorizedException_when_user_is_not_log_in() {
        assertThrows(UnauthorizedException.class, () -> messageController.deleteMessage(2L));
    }

    @Test
    void deleteMessage_return_ok_and_update_db_when_all_ok() {
        auth.setRealUserId(1);

        var message = new Message(999L, -1L, 1L);
        messageRepository.save(message);

        var messageInDb1 = messageRepository.findById(999L).orElse(null);
        assertNotNull(messageInDb1);

        messageController.deleteMessage(999L);
        var messageInDb2 = messageRepository.findById(999L).orElse(null);
        assertNull(messageInDb2);
    }


    //test batchMarkRead

    @Test
    void batchMarkRead_throw_UnauthorizedException_when_user_is_not_log_in() {
        assertThrows(UnauthorizedException.class, () -> messageController.batchMarkRead(null));
    }

    @Test
    void batchMarkRead_throw_NotFoundException_when_user_is_not_log_in() {
        auth.setRealUserId(1L);

        var ids = new ArrayList<Long>();
        ids.add(2L);
        assertThrows(NotFoundException.class, () -> messageController.batchMarkRead(new BatchMarkReadModel(ids)));
    }

    @Test
    void batchMarkRead_throw_ForbidException_when_user_try_to_mark_others_message() {
        var message1 = new Message(11L, -1L, 2L);
        messageRepository.save(message1);
        auth.setRealUserId(1L);

        var ids = new ArrayList<Long>();
        ids.add(11L);
        assertThrows(ForbidException.class, () -> messageController.batchMarkRead(new BatchMarkReadModel(ids)));
    }


    @Test
    void batchMarkRead_return_ok_and_update_db_when_all_ok() {
        var message1 = new Message(11L, -1L, 1L);
        var message2 = new Message(12L, -1L, 1L);
        messageRepository.save(message1);
        messageRepository.save(message2);

        auth.setRealUserId(1L);

        var ids = new ArrayList<Long>();
        ids.add(11L);
        ids.add(12L);
        messageController.batchMarkRead(new BatchMarkReadModel(ids));

        for (var id : ids) {
            var messageInDb = messageRepository.findById(id).orElse(null);
            assertNotNull(messageInDb);
            assertTrue(messageInDb.getHaveRead());
        }
    }


    //test readAllMessage

    @Test
    void readAllMessage_throw_UnauthorizedException_when_user_is_not_log_in() {
        assertThrows(UnauthorizedException.class, () -> messageController.readAllMessages());
    }

    @Test
    void readAllMessage_return_ok_and_update_db_when_all_ok() {
        auth.setRealUserId(1);
        var message1 = new Message(101L, -1L, 1L);
        var message2 = new Message(102L, -1L, 1L);

        messageRepository.save(message1);
        messageRepository.save(message2);

        messageController.readAllMessages();

        var messagesInDb = messageRepository.findAllByReceiverId(auth.userId());
        for (var message : messagesInDb) {
            assertTrue(message.getHaveRead());
        }
    }

    //test getMessageById

    @Test
    void getMessageById_throw_UnauthorizedException_when_user_is_not_log_in() {
        assertThrows(UnauthorizedException.class, () -> messageController.getMessageById(1L));
    }

    @Test
    void getMessageById_return_message_info_when_all_ok() {
        auth.setRealUserId(1);
        var user = new User(5L, "dasd", "dasdasdas", "DSadadada", "DASdas");
        userRepository.save(user);

        var message1 = new Message(998L, -1L, 1L);
        var message2 = new Message(999L, 5L, 1L);
        messageRepository.save(message1);
        messageRepository.save(message2);

        var messageInDb1 = messageController.getMessageById(998L);
        assertNotNull(messageInDb1);
        assertNull(messageInDb1.getSender());
        assertEquals(messageInDb1.getReceiverId(), 1L);

        var messageInDb2 = messageController.getMessageById(999L);
        assertNotNull(messageInDb2);
        assertNotNull(messageInDb2.getSender());
        assertEquals(messageInDb2.getSender().getId(), 5L);

    }

    //test getMessages

    @Test
    void getMessages_throw_UnauthorizedException_when_user_is_not_log_in() {
        assertThrows(UnauthorizedException.class, () -> messageController.getMessages(null, null, null, null));
    }

    @Test
    void getMessages_return_list_of_message_info_when_all_ok() {
        auth.setRealUserId(1);
        var user1 = new User(5L, "dasd", "dasdasdas", "DSadadada", "DASdas");
        var user2 = new User(1L, "dasd", "dasdasdas", "DSadadada", "DASdas");
        var user3 = new User(2L, "dasd", "dasdasdas", "DSadadada", "DASdas");

        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);

        var message1 = new Message(991L, -1L, 1L);
        var message2 = new Message(992L, 5L, 1L);
        var message3 = new Message(993L, 5L, 2L);
        var message4 = new Message(994L, 5L, 1L);
        var message5 = new Message(995L, 5L, 1L);

        message2.setTitle("dsawwwhaha");
        message4.setTitle("hdsadw");
        message5.setTitle("qqppoow");


        messageRepository.save(message1);
        messageRepository.save(message2);
        messageRepository.save(message3);
        messageRepository.save(message4);


        var messagesInDb = messageController.getMessages(5L, 1L, "dsa", false);
        assertEquals(messagesInDb.size(), 2);
        for (var qMessage : messagesInDb) {
            assertEquals(qMessage.getSender().getId(), 5L);
            assertEquals(qMessage.getReceiverId(), 1L);
            assertThat(qMessage.getTitle()).contains("dsa");
            assertFalse(qMessage.isHaveRead());
        }


    }


}
