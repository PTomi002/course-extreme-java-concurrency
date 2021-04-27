package playground;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.concurrent.locks.StampedLock;

public class Coordinates {

    static class CoordinatesVarHandler {
        // Compare-And-Swap like AtomicInteger, etc...
        private static VarHandle XY_HANDLER;

        static {
            try {
                XY_HANDLER = MethodHandles.lookup().findVarHandle(CoordinatesVarHandler.class, "xy", double[].class);
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
        }

        // We need a holder object for these values to handle variance, thats why the array.
        private volatile double[] xy = {0.0, 0.0};

        public void move(double x, double y) {
            // Safely save them to local variables.
            // We construct the array outside of the loop, because we want to be as fast as possible.
            double[] current, next = new double[2];
            do {
                // Coding idiom: do it in a loop.
                // Locks are pessimistic way, this is optimistic way, hopefully it will work for the first time.
                current = xy;
                next[0] = current[0] + x;
                next[1] = current[1] + y;
            } while (!XY_HANDLER.compareAndSet(this, current, next));
        }

        public double distanceFromZero() {
            // Safe operation
            double[] tmp = xy;
            return Math.hypot(tmp[0], tmp[1]);
        }
    }

    static class CoordinatesStampedLock {
        private double x = 0.0, y = 0.0;
        private final StampedLock lock = new StampedLock();

        public void move(double dX, double dY) {
            long stamp = lock.writeLock();// Pessimistic way, exclusive lock.
            try {
                x += dX;
                y += dY;
            } finally {
                lock.unlockWrite(stamp);
            }
        }

        public double distanceFromZero() {
            long stamp = lock.tryOptimisticRead();
            double tmpX = x, tmpY = y;
            if (!lock.validate(stamp)) {
                stamp = lock.readLock();
                try {
                    tmpX = x;
                    tmpY = y;
                } finally {
                    lock.unlockRead(stamp);
                }
            }
            return Math.hypot(tmpX, tmpY);
        }
    }

    public static void main(String... args) {

    }
}
