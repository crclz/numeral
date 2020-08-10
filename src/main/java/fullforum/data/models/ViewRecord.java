package fullforum.data.models;

import fullforum.data.RootEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;

@Entity
public class ViewRecord extends RootEntity {
    @Getter
    @Setter
    private Long userId;

    @Getter
    @Setter
    private Long documentId;

    protected ViewRecord() {

    }

    public ViewRecord(long id, long userId, long documentId) {
        super(id);
        setUserId(userId);
        setDocumentId(documentId);
    }
}
