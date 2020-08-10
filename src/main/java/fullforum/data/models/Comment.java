package fullforum.data.models;

import fullforum.data.RootEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;

@Entity
public class Comment extends RootEntity {
    @Getter
    private Long documentId;

    @Getter
    private Long userId;

    @Getter
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
