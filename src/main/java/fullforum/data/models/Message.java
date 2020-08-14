package fullforum.data.models;

import fullforum.data.RootEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;

@AllArgsConstructor
@Entity
public class Message extends RootEntity {
    @Getter
    @Setter
    private Long senderId;

    @Getter
    @Setter
    private Long receiverId;

    @Getter
    @Setter
    private String title;

    @Getter
    @Setter
    private String content;

    @Getter
    @Setter
    private Boolean haveRead = false;

    protected Message() {}






}
