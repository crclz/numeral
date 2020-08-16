package fullforum.data.models;

import fullforum.data.RootEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class Comment extends RootEntity {
    @Getter
    private long documentId;

    @Getter
    private long userId;

    @Getter
    @Column(length = 1024)
    private String content;

    protected Comment() {

    }

    public Comment(long id, long documentId, long userId, String content) {
        super(id);
        this.documentId = documentId;
        this.userId = userId;
        this.content = content;
    }
}
