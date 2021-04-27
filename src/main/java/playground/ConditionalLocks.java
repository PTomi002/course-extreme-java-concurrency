package playground;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConditionalLocks {

    @ThreadSafe
    static class ConditionalQueue<T> {
        private static final int LIMIT = 10;
        private final Lock lock = new ReentrantLock();
        private final Condition notEmpty = lock.newCondition();
        private final Condition notFull = lock.newCondition();
        @GuardedBy("lock")
        private final List<T> items;
        private volatile int size = 0;

        ConditionalQueue() {
            this.items = new ArrayList<>(LIMIT);
        }

        public T take() throws InterruptedException {
            lock.lock();
            try {
                while (items.isEmpty()) notEmpty.await();
                notFull.signal();
                size--;
                return items.remove(0);
            } finally {
                lock.unlock();
            }
        }

        public boolean put(T item) throws InterruptedException {
            lock.lock();
            try {
                while (items.size() == LIMIT) notFull.await();
                notEmpty.signal();
                size++;
                return items.add(item);
            } finally {
                lock.unlock();
            }
        }

        public int size() {
            return size;
        }
    }

    public static void main(String... args) throws InterruptedException {
        int nThreads = 6;
        int loop = 30;
        ConditionalQueue<Integer> q = new ConditionalQueue<>();
        ExecutorService pool = Executors.newFixedThreadPool(nThreads);

        for (int i = 0; i < nThreads / 2; i++) {
            pool.submit(() -> {
                try {
                    for (int j = 0; j < loop; j++) {
                        int value = ThreadLocalRandom.current().nextInt();
                        System.out.println("(" + j + ".) Put: " + value + " (" + q.put(value) + ") (" + q.size() + ") from thread: " + Thread.currentThread().getName());
                    }
                } catch (InterruptedException e) {
                    throw new CancellationException(e.getMessage());
                }
            });
        }

        Thread.sleep(5000);
        for (int i = 0; i < nThreads / 2; i++) {
            pool.submit(() -> {
                try {
                    for (int j = 0; j < loop; j++) {
                        System.out.println("(" + j + ".) Took: " + q.take() + " (" + q.size() + ") from thread: " + Thread.currentThread().getName());
                    }
                } catch (InterruptedException e) {
                    throw new CancellationException(e.getMessage());
                }
            });
        }

        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);

        System.out.println("Queue size is zero? " + q.size());
    }
}
