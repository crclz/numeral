package fullforum.controllers;

import fullforum.data.models.Message;
import fullforum.data.repos.*;
import fullforum.dto.in.CreateMessageModel;
import fullforum.dto.out.IdDto;
import fullforum.dto.out.QMessage;
import fullforum.dto.out.Quser;
import fullforum.errhand.ForbidException;
import fullforum.errhand.NotFoundException;
import fullforum.errhand.UnauthorizedException;
import fullforum.services.IAuth;
import fullforum.services.Snowflake;
import org.hibernate.cfg.NotYetImplementedException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Transactional
@RestController
@RequestMapping("/api/message")
@Validated
public class MessageController {
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
    DocumentRepository documentRepository;

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    MembershipRepository membershipRepository;


    @PostMapping
    public IdDto createMessage(@RequestBody CreateMessageModel model) {
        if (!auth.isLoggedIn()) {
            throw new UnauthorizedException();
        }
        var receiver = userRepository.findById(model.receiverId).orElse(null);

        if (receiver == null) {
            throw new NotFoundException();
        }
        var message = new Message(snowflake.nextId(), model.sendId, model.receiverId);
        message.setTitle(model.title);
        message.setContent(model.content);
        messageRepository.save(message);

        return new IdDto(message.getId());
    }

    @DeleteMapping("{id}")
    public void deleteMessage(@PathVariable Long id) {
        if (!auth.isLoggedIn()) {
            throw new UnauthorizedException();
        }
        var message = messageRepository.findById(id).orElse(null);
        if (message == null) {
            throw new NotFoundException();
        }
        if (auth.userId() != message.getReceiverId()) {
            throw new ForbidException();
        }
        messageRepository.deleteById(id);
    }



    @PatchMapping()//设置用户当前所有消息为已读
    public void readAllMessages() {
        if (!auth.isLoggedIn()) {
            throw new UnauthorizedException();
        }
        var messages = messageRepository.findAllByReceiverIdAndHaveRead(auth.userId(), false);
        for (var message : messages) {
            message.setHaveRead(true);
            messageRepository.save(message);
        }
    }


    @GetMapping("{id}")
    public QMessage getMessageById(@PathVariable Long id) {
        if (!auth.isLoggedIn()) {
            throw new UnauthorizedException();
        }
        var message = messageRepository.findById(id).orElse(null);
        if (message == null) {
            throw new NotFoundException();
        }

        Quser quser;
        if (message.getSenderId() != -1) {
            var sender = userRepository.findById(message.getSenderId()).orElse(null);
            assert sender != null;
            quser = Quser.convert(sender, mapper);
        } else { //若为系统通知则对应的用户信息为null
            quser = null;
        }

        return QMessage.convert(message, quser, mapper);
    }


    @GetMapping
    public List<QMessage> getMessages(
            @RequestParam(required = false) Long senderId,
            @RequestParam(required = false) Long receiverId,
            @RequestParam(required = false) String titleKeyword,
            @RequestParam(required = false) Boolean haveRead
    ) {
        if (!auth.isLoggedIn()) {
            throw new UnauthorizedException();
        }
        var query = entityManager.createQuery(
                "select m from Message m" +
                    " where (:senderId is null or m.senderId = :senderId)" +
                    " and (:receiverId is null or m.receiverId = :receiverId)" +
                    " and (:titleKeyword is null or m.title like :titleExpr)" +
                    " and (:haveRead is null or m.haveRead = :haveRead)")
                .setParameter("senderId", senderId)
                .setParameter("receiverId", receiverId)
                .setParameter("titleKeyword", titleKeyword)
                .setParameter("titleExpr","%" + titleKeyword + "%")
                .setParameter("haveRead", haveRead);

        var results = query.getResultList();
        var qMessages = new ArrayList<QMessage>();
        for (var result : results) {
            var message = (Message) result;
            Quser quser;
            if (message.getSenderId() != -1) {
                var sender = userRepository.findById(message.getSenderId()).orElse(null);
                assert sender != null;
                quser = Quser.convert(sender, mapper);
            } else { //若为系统通知则对应的用户信息为null
                quser = null;
            }
            var qMessage = QMessage.convert(message, quser, mapper);

            qMessages.add(qMessage);
        }
        return qMessages;
    }






}
