package fullforum.data.models;

import fullforum.data.RootEntity;
import lombok.Getter;

import javax.persistence.Entity;

@Entity
public class ELock extends RootEntity {

    @Getter
    private long documentId;

    @Getter
    private long lastOwnerId;

    @Getter
    private long lastAcquiredAt;

    protected ELock() {
    }

    public ELock(long id, long documentId) {
        super(id);
        this.documentId = documentId;
        this.lastAcquiredAt = 0;
    }

    public boolean tryAcquire(long userId) {
        var now = System.currentTimeMillis();

        if (userId == lastOwnerId) {
            lastAcquiredAt = now;
            return true;
        }

        if (now - lastAcquiredAt > 5000) {
            lastOwnerId = userId;
            lastAcquiredAt = now;
            return true;
        }

        return false;
    }
}
