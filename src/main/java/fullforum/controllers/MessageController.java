package fullforum.controllers;

import fullforum.data.repos.DocumentRepository;
import fullforum.data.repos.MessageRepository;
import fullforum.data.repos.TeamRepository;
import fullforum.data.repos.UserRepository;
import fullforum.dto.in.CreateMessageModel;
import fullforum.dto.out.IdDto;
import fullforum.dto.out.QMessage;
import fullforum.services.IAuth;
import fullforum.services.Snowflake;
import org.hibernate.cfg.NotYetImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.List;

@Transactional
@RestController
@RequestMapping("/api/message")
public class MessageController {
    @Autowired
    IAuth auth;

    @Autowired
    Snowflake snowflake;

    @Autowired
    TeamRepository teamRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    DocumentRepository documentRepository;

    @Autowired
    MessageRepository messageRepository;


    @PostMapping
    public IdDto createMessage(@RequestBody CreateMessageModel model){
        throw new NotYetImplementedException();

    }

    @DeleteMapping("{id}")
    public void deleteMessage(@PathVariable Long id) {
        throw new NotYetImplementedException();
    }

    @PatchMapping()//传入用户id设置用户当前所有消息为已读
    public void readAllMessages() {
        throw new NotYetImplementedException();
    }



    @GetMapping("{id}")
    public QMessage getMessageById(Long id) {
        throw new NotYetImplementedException();
    }

    @GetMapping
    public List<QMessage> getMessages(
            @RequestParam(required = false) Long senderId,
            @RequestParam(required = false) Long receiverId,
            @RequestParam(required = false) String titleKeyword,
            @RequestParam(required = false) Boolean haveRead
    ) {
        throw new NotYetImplementedException();
    }






}
