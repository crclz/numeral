package fullforum.data.models;

import fullforum.data.RootEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;

@Entity
public class ViewRecord extends RootEntity {
    @Getter
    private long userId;

    @Getter
    private long documentId;

    protected ViewRecord() {

    }

    public ViewRecord(long id, long userId, long documentId) {
        super(id);
        this.userId = userId;
        this.documentId = documentId;
    }
}
