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
    private Boolean isAbandoned = false;

    @Getter
    @Setter
    private Access publicDocumentAccess = Access.ReadWrite;

    @Getter
    @Setter
    private Access publicCommentAccess = Access.ReadWrite;

    @Getter
    @Setter
    private Boolean publicCanShare = false;

    @Getter
    @Setter
    private Access teamDocumentAccess = Access.ReadWrite;

    @Getter
    @Setter
    private Access teamCommentAccess = Access.ReadWrite;

    @Getter
    @Setter
    private Boolean teamCanShare = false;

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
        if (!(title.length() >= 1 && title.length() <= 25)) {
            throw new IllegalArgumentException("title length should in [1,25]");
        }
        if (description.length() > 140) {
            throw new IllegalArgumentException("description length should in [1,25]");
        }
        this.creatorId = creatorId;
        this.title = title;
        this.description = description;
        this.data = data;
    }

    public void setModifyCountAndModifier(long modifierId) {
        modifyCount++;
        this.lastModifierId = modifierId;
    }

    @Override
    public String toString() {
        return "Document{" +
                "creatorId=" + creatorId +
                ", teamId=" + teamId +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", isAbandoned=" + isAbandoned +
                ", publicDocumentAccess=" + publicDocumentAccess +
                ", publicCommentAccess=" + publicCommentAccess +
                ", publicCanShare=" + publicCanShare +
                ", teamDocumentAccess=" + teamDocumentAccess +
                ", teamCommentAccess=" + teamCommentAccess +
                ", teamCanShare=" + teamCanShare +
                ", lastModifierId=" + lastModifierId +
                ", data='" + data + '\'' +
                ", modifyCount=" + modifyCount +
                '}';
    }
}


