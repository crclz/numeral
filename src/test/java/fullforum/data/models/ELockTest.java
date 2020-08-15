package fullforum.data.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ELockTest {
    @Test
    void acquire_lock_test() {
        var lock = new ELock(1, 1);
        assertTrue(lock.tryAcquire(1070));
        assertTrue(lock.tryAcquire(1070));
        assertFalse(lock.tryAcquire(1071));
    }

    @Test
    void release_lock_test() {
        var lock = new ELock(1, 1);
        assertTrue(lock.tryAcquire(1070));
        assertTrue(lock.tryRelease(1070));
        assertNull(lock.getRealOwnerId());
        assertTrue(lock.tryAcquire(1071));
    }
}