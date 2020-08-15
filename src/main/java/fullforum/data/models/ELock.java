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

    @Getter
    private final int LOCK_TIME_OUT = 3000;

    protected ELock() {
    }

    public ELock(long id, long documentId) {
        super(id);
        this.documentId = documentId;
        this.lastAcquiredAt = 0;
    }

    public Long getRealOwnerId() {
        var now = System.currentTimeMillis();
        if (now - lastAcquiredAt > LOCK_TIME_OUT) {
            return null;
        }
        return lastOwnerId;
    }

    public boolean tryAcquire(long userId) {
        var now = System.currentTimeMillis();

        if (userId == lastOwnerId) {
            lastAcquiredAt = now;
            return true;
        }

        if (now - lastAcquiredAt > LOCK_TIME_OUT) {
            lastOwnerId = userId;
            lastAcquiredAt = now;
            return true;
        }

        return false;
    }

    public boolean tryRelease(long userId) {
        var ownerId = getRealOwnerId();
        if (ownerId == null) {
            // 锁空闲
            return false;
        }
        if (ownerId != userId) {
            // 无权限释放
            return false;
        }
        // release (expire)
        lastAcquiredAt = 0;
        return true;
    }
}
