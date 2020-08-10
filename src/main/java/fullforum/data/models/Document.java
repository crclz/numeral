package fullforum.data.models;

import fullforum.data.RootEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class Document extends RootEntity {

    @Getter
    private Long creatorId;

    @Getter
    @Setter
    private Long teamId;// 可空

    @Getter
    @Setter
    private String title;

    @Getter
    @Setter
    private String description;

    @Getter
    @Setter
    private boolean isAbandoned = false;

    @Getter
    @Setter
    private Access publicDocumentAccess = Access.None;

    @Getter
    @Setter
    private Access publicCommentAccess = Access.None;

    @Getter
    @Setter
    private boolean publicCanShare = false;

    @Getter
    @Setter
    private Access teamDocumentAccess = Access.None;

    @Getter
    @Setter
    private Access teamCommentAccess = Access.None;

    @Getter
    @Setter
    private boolean teamCanShare = false;

    @Getter
    private Long lastModifierId;

    @Getter
    @Setter
    @Column(columnDefinition = "text", length = 65535)
    private String data;

    @Getter
    private int modifyCount = 0;


    protected Document() {
        // Required by jpa
    }

    public Document(long id, long creatorId, String title, String description, String data) {
        super(id);
        this.creatorId = creatorId;
        this.title = title;
        this.description = description;
        this.data = data;
    }

    public void setModifyCountAndModifier(long modifierId) {
        modifyCount++;
        this.lastModifierId = modifierId;
    }
}


