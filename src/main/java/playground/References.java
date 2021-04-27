package playground;

import java.lang.ref.*;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class References {

    private static final ReferenceQueue<TestClass> queue = new ReferenceQueue<>();

    static class TestClass {
        protected final char[] str;

        public TestClass(String str) {
            this.str = str.toCharArray();
        }

        public TestClass(char... str) {
            this.str = str;
        }

        @Override
        public String toString() {
            return "TestClass{" +
                    "str='" + str + '\'' +
                    '}';
        }
    }

    static class FinalizerTestClass extends TestClass {
        public FinalizerTestClass(String str) {
            super(str);
        }

        public FinalizerTestClass(char... str) {
            super(str);
        }

        @Override
        protected void finalize() throws Throwable {
            System.out.println("All resources are released!");
        }
    }

    /**
     * It is automatically created and used by JVM, every time we load a class information via reflection.
     * Can be disabled via: -Dsun.reflect.noCaches=true in older Java versions <= 1.8. https://bugs.openjdk.java.net/browse/JDK-8169880
     * Change the project sdk to java 1.8!
     */
    static class ReflectionCache {
        private static Class s; // Search for: sun.reflect.noCaches in Class.
        private static volatile Method[] methods;

        public void run() {
            // Run with: -Dsun.reflect.noCaches=true and then -Dsun.reflect.noCaches=false to see the difference.
            // true = Took: 5665 ms, false = Took: 261 ms
            long start = System.currentTimeMillis();
            for (int i = 0; i < 100_000; i++) {
                methods = String.class.getMethods();
            }
            System.out.println("Took: " + (System.currentTimeMillis() - start) + " ms");
        }
    }

    static class SoftReferenceTest {
        public void run() {
            TestClass me = new TestClass("Tamás"); // Created in the heap , not in string pool.
            Reference<TestClass> sRef = new SoftReference<>(me, queue);
            me = null;
            System.out.println("sRef: " + sRef.get());
            System.gc(); // Issue full gc.
            System.out.println("sRef: " + sRef.get()); // There is a possibility this comes back as null.
            try {
                byte[][] oom = new byte[1024][1024 * 1024 * 1024]; // 1 TB of memory.
            } catch (OutOfMemoryError ex) {
                System.out.println("sRef: " + sRef.get());
            }
        }
    }

    static class WeakReferenceTest {
        public void run() {
            TestClass me = new TestClass("Tamás");
            Reference<TestClass> wRef = new WeakReference<>(me, queue);
            me = null;
            System.out.println("wRef: " + wRef.get()); // There is a possibility this comes back as null.
            System.gc();
            System.out.println("wRef: " + wRef.get());
        }
    }

    /**
     * Since an object is strongly reachable during the execution of its finalize() method,
     * it takes at least one additional garbage collection cycle to detect that it became phantom reachable.
     * Then, “at that time or at some later time” it will get enqueued.
     */
    static class PhantomReferenceTest {
        public void run() throws InterruptedException {
            TestClass me = new FinalizerTestClass("Tamás");
            new PhantomReference<>(me, queue);
            me = null;
            System.gc();
            Thread.sleep(1000);
            System.gc();
            Thread.sleep(1000);
        }
    }

    /**
     * Better to use Cleaner.create().register(...) when obj gets phantom reachable.
     */
    static class PhantomReferenceNormalUsageTest {
        public void run() throws InterruptedException {
            char[] pwd = new char[]{'P', 'A', '$', '$', 'w', '0', 'r', 'd'};
            TestClass password = new FinalizerTestClass(pwd);
            Cleaner.Cleanable cleanable = Cleaner.create().register(password, () -> {
                Arrays.fill(pwd, '*');
            });
            password = null;
            System.out.println("Before: " + Arrays.toString(pwd));
            System.gc();
            Thread.sleep(1000);
            System.gc(); // Ensure that phantom reference is cleaned.
            Thread.sleep(1000);
            System.out.println("After: " + Arrays.toString(pwd));
        }
    }

    // Lot of algorithm build in JDK is based on knuth 6.4 algorithm r book.
    // Threads hold weak references to its ThreadLocals.
    static class ThreadLocalTest {
        // ThreadLocal does not hold any value (it is just a key), the t.threadLocals ThreadLocalMap holds the values for the keys.
        // If we do not need tl reference anymore (there is no strong ref to it),
        //      it is automatically cleaned in the ThreadLocalMap by the next GC cycle.
        private static final AtomicLong i = new AtomicLong(0);
        private final ThreadLocal<String> tl = ThreadLocal.withInitial(() -> String.valueOf(i.getAndIncrement()));

        public static void run() {
            int nThreads = 10;
            ExecutorService pool = Executors.newFixedThreadPool(nThreads);
            for (int j = 0; j < 1_000; j++) {
                pool.submit(() -> {
                    if (i.get() == 900)
                        // If you see the tl map, you see all the values with the referents as lambda suppliers because of ThreadLocal.withInitial.
                        // If you run a System.gc(), all the referents will be null, but the Entries are in the map until removed programmatically or by "deleting" the Thread object.
                        System.out.println("Debug point!");

                    // This solution does not clean the tl map.
                    // ThreadLocalTest tlTest = new ThreadLocalTest();
                    // System.out.println("t: " + Thread.currentThread().getName() + " getId: " + tlTest.getId());

                    // This solution cleans the tl map.
                    ThreadLocalTest tlTest = new ThreadLocalTest();
                    try {
                        System.out.println("t: " + Thread.currentThread().getName() + " getId: " + tlTest.getId());
                    } finally {
                        // The weak reference still resides within the map, even their referents are cleared by the gc, so we have to delete them manually.
                        tlTest.removeId();
                    }
                });
            }
            pool.shutdown();
        }

        public String getId() {
            return tl.get();
        }

        // This solution cleans the tl map.
        public void removeId() {
            tl.remove();
        }
    }

    public static void main(String... args) throws InterruptedException {

        ExecutorService pool = Executors.newSingleThreadExecutor();
        Future<?> result = pool.submit(() -> {
            while (!Thread.interrupted()) {
                Reference<? extends TestClass> ref = queue.poll();
                if (ref != null) System.out.println("Queue: " + ref);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        ReflectionCache cache = new ReflectionCache();
        cache.run();
        System.out.println();

        SoftReferenceTest sRef = new SoftReferenceTest();
        sRef.run();
        System.out.println();

        WeakReferenceTest wRef = new WeakReferenceTest();
        wRef.run();
        System.out.println();

        PhantomReferenceTest pRef = new PhantomReferenceTest();
        pRef.run();
        System.out.println();

        PhantomReferenceNormalUsageTest p2Ref = new PhantomReferenceNormalUsageTest();
        p2Ref.run();

        // Uncomment this for testing thread locals.
        // ThreadLocalTest.run();

        Thread.sleep(2 * 1_000);
        result.cancel(true);
        pool.shutdown();
        pool.awaitTermination(3, TimeUnit.SECONDS);
        System.out.println("Finished.");
    }

}
