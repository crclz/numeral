package fullforum.data.models;

import fullforum.data.RootEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;

@Entity
public class Message extends RootEntity {
    @Getter
    @Setter
    private long senderId;//若为-1则为系统通知

    @Getter
    @Setter
    private long receiverId;

    @Getter
    @Setter
    private String title;

    @Getter
    @Setter
    private String content;

    @Getter
    @Setter
    private String link;

    @Getter
    @Setter
    private Boolean haveRead = false;

    protected Message() {}

    public Message(Long id, Long senderId, Long receiverId) {
        super(id);
        this.senderId = senderId;
        this.receiverId = receiverId;
    }







}
