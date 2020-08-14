package fullforum.data.models;

import fullforum.data.RootEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;

@Data
@AllArgsConstructor
@Entity
public class Message extends RootEntity {
    private Long senderId;

    private Long receiverId;

    private String title;

    private String content;

    private boolean haveRead = false;

    protected Message() {}






}
