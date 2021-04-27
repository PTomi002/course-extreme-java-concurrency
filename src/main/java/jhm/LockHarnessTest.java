package jhm;

import java.util.concurrent.locks.ReentrantLock;

public class LockHarnessTest {

    private final ReentrantLock lock = new ReentrantLock();
    private final int i;

    public LockHarnessTest(int i) {
        this.i = i;
    }

    public int computeWithLock(int x) {
        lock.lock();
        int result = 0;
        try {
            for (int j = 0; j < i; j++) {
                result += x;
            }
        } finally {
            lock.unlock();
        }
        return result;
    }

    public synchronized int computeWithIntrinsics(int x) {
        int result = 0;
        for (int j = 0; j < i; j++) {
            result += x;
        }
        return result;
    }

}
