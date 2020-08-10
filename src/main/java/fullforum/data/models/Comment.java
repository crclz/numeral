package fullforum.data.models;

import fullforum.data.RootEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;

@Entity
public class Comment extends RootEntity {
    @Getter
    @Setter
    private Long documentId;

    @Getter
    @Setter
    private Long userId;

    @Getter
    @Setter
    private String content;

    protected Comment() {

    }

    public Comment(long documentId, long userId, String content) {
        setDocumentId(documentId);
        setUserId(userId);
        setContent(content);
    }
}
