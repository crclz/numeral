package fullforum.data.models;

import fullforum.data.RootEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;

@Entity
public class Reply extends RootEntity {
    @Getter
    private long commentId;

    @Getter
    long userId;

    @Getter
    private long targetUserId;

    @Getter
    private String content;

    @Getter
    @Setter
    private int thumbCount = 0;

    protected Reply() {}

    public Reply(long id, long commentId,long userId,  long targetUserId, String content) {
        super(id);
        this.commentId = commentId;
        this.userId = userId;
        this.targetUserId = targetUserId;
        this.content = content;
    }

    public void thumbUp() {
        this.thumbCount++;
    }

    public void cancelThumbUp() {
        this.thumbCount--;
    }

}
