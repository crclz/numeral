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
    @Setter(AccessLevel.PRIVATE)
    private Long creatorId;

    @Getter
    @Setter(AccessLevel.PRIVATE)
    private Long teamId;// 可空

    @Getter
    @Setter
    private boolean isAbandoned;

    @Getter
    @Setter
    private Access publicDocumentAccess;

    @Getter
    @Setter
    private Access publicCommentAccess;

    @Getter
    @Setter
    private boolean publicCanShare;

    @Getter
    @Setter
    private Access teamDocumentAccess;

    @Getter
    @Setter
    private Access teamCommentAccess;

    @Getter
    @Setter
    private boolean teamCanShare;

    @Getter
    @Setter(AccessLevel.PRIVATE)
    public Long lastModifierId;

    @Getter
    @Column(columnDefinition = "text", length = 65535)
    private String data;


    protected Document() {
        // Required by jpa
    }

    public Document(long id) {
        super(id);
    }

    public void setData(String data, Long lastModifierId) {
        this.data = data;
        setLastModifierId(lastModifierId);
    }
}


