package playground;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.*;

public class ExplicitLocks {

    static class AutoCloseableLock implements AutoCloseable {

        private final Lock lock;

        AutoCloseableLock(Lock lock) {
            this.lock = lock;
        }

        public AutoCloseableLock lock() {
            lock.lock();
            return this;
        }

        @Override
        public void close() throws Exception {
            lock.unlock();
        }
    }

    static class SharedData {
        private int shared = 0;

        public void increment() {
            shared++;
        }

        public int getShared() {
            return shared;
        }
    }

    public static void main0(String... args) throws Exception {
        int nThreads = 5;
        Lock lock = new ReentrantLock();
        ExecutorService pool = Executors.newFixedThreadPool(nThreads);

        for (int i = 0; i < nThreads; i++) {
            pool.submit(() -> {
                System.out.println("Started running task: " + Thread.currentThread().getName());
                try {
                    lock.lock();
                    System.out.println("Running my task: " + Thread.currentThread().getName());
                    Thread.sleep(2000);
                    System.out.println("Finished my task:" + Thread.currentThread().getName());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }
            });
        }

        pool.shutdown();
        pool.awaitTermination(20, TimeUnit.SECONDS);
    }

    public static void main1(String... args) throws Exception {
        AutoCloseableLock autoLock = new AutoCloseableLock(new ReentrantLock());
        try (final AutoCloseableLock al = autoLock.lock()) {
            System.out.println("Do my thing...");
        }
    }

    public static void main2(String... args) throws Exception {
        int nThreads = 5;
        ExecutorService pool = Executors.newFixedThreadPool(nThreads + 1);
        final ReadWriteLock rwLock = new ReentrantReadWriteLock();
        final SharedData shared = new SharedData();

        final Thread[] readers = new Thread[nThreads];
        for (int i = 0; i < nThreads; i++) {
            readers[i] = new Thread(() -> {
                for (int j = 0; j < 20; j++) {
                    rwLock.readLock().lock();
                    System.out.println("Holding lock: " + Thread.currentThread().getName());
                    System.out.println(shared.getShared());
                    rwLock.readLock().unlock();
                    System.out.println("Releasing lock: " + Thread.currentThread().getName());
                }
            });
        }
        Thread writer = new Thread(() -> {
            for (int i = 0; i < 20; i++) {
                rwLock.writeLock().lock();
                System.out.println("Writing lock: " + Thread.currentThread().getName());
                shared.increment();
                rwLock.writeLock().unlock();
                System.out.println("Releasing writing lock: " + Thread.currentThread().getName());
            }
        });

        pool.submit(writer);
        for (int i = 0; i < nThreads; i++) {
            pool.submit(readers[i]);
        }

        pool.shutdown();
        pool.awaitTermination(30, TimeUnit.SECONDS);
    }

    public static void main3(String... args) throws Exception {
        int nThreads = 5;
        ExecutorService pool = Executors.newFixedThreadPool(nThreads + 1);
        final StampedLock stampedLock = new StampedLock();
        final SharedData shared = new SharedData();

        final Thread[] readers = new Thread[nThreads];
        for (int i = 0; i < nThreads; i++) {
            readers[i] = new Thread(() -> {
                for (int j = 0; j < 20; j++) {
                    // Code idiom for stamped lock.
                    long stamp = stampedLock.tryOptimisticRead(); // Stamp for optimistic read.
                    int state = shared.getShared(); // Save state in local variable, we are optimistic as this shared variable is not under writing.
                    if (!stampedLock.validate(stamp)) { // Validate we does not issued write lock since the stamp.
                        try {
                            stamp = stampedLock.readLock();
                            state = shared.getShared();
                        } finally {
                            stampedLock.unlock(stamp);
                        }
                        System.out.println("Read pessimistically: " + state);
                    } else {
                        // We know this branch is consistent now, due to validate check.
                        System.out.println("Read optimistically: " + state);
                    }
                }
            });
        }
        Thread writer = new Thread(() -> {
            for (int i = 0; i < 20; i++) {
                long stamp = stampedLock.writeLock();
                System.out.println("Writing lock: " + Thread.currentThread().getName());
                shared.increment();
                stampedLock.unlock(stamp);
                System.out.println("Released writing lock: " + Thread.currentThread().getName());
            }
        });

        pool.submit(writer);
        for (int i = 0; i < nThreads; i++) {
            pool.submit(readers[i]);
        }

        pool.shutdown();
        pool.awaitTermination(30, TimeUnit.SECONDS);
    }

    public static void main4(String... args) throws Exception {
        ReadWriteLock lock = new ReentrantReadWriteLock();

        lock.readLock().lock();

        if (lock.writeLock().tryLock()) {
            System.out.println("Locking successful");
            lock.writeLock().unlock();
        } else {
            System.out.println("Locking unsuccessful");
        }

        lock.readLock().unlock();
    }

    public static void main(String... args) throws Exception {
        ReentrantLock lock = new ReentrantLock();

        lock.lock();
        AtomicInteger tries = new AtomicInteger();

        Thread t = new Thread(() -> {
            do tries.getAndIncrement(); while (!lock.tryLock());
            System.out.println("Tries: " + tries.get());
            lock.unlock();
        });
        t.start();

        Thread.sleep(400);

        lock.unlock();
        t.join();

    }

}
