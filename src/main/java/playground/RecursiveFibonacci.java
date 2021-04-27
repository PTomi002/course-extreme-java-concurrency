package playground;

import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.function.Supplier;

public class RecursiveFibonacci {

    static int recFibonacci(int nthFibonacci) {
        if (nthFibonacci == 0) return 0;
        if (nthFibonacci == 1) return 1;
        return recFibonacci(nthFibonacci - 1) + recFibonacci(nthFibonacci - 2);
    }

    static int linFibonacci(int nthFibonacci) {
        long n0 = 0, n1 = 1;
        for (int i = 0; i < nthFibonacci; i++) {
            long tmp = n1;
            n1 = n1 + n0;
            n0 = tmp;
        }
        return (int) n0;
    }

    static class FibonacciTask extends RecursiveTask<Integer> {

        private final int nthFibonacci;

        FibonacciTask(int nthFibonacci) {
            this.nthFibonacci = nthFibonacci;
        }

        @Override
        protected Integer compute() {
            if (nthFibonacci == 0) return 0;
            if (nthFibonacci == 1) return 1;
            System.out.println("F/J executing number: " + nthFibonacci + " in thread: " + Thread.currentThread().getName());
            FibonacciTask f1 = new FibonacciTask(nthFibonacci - 1);
            FibonacciTask f2 = new FibonacciTask(nthFibonacci - 2);
            f1.fork();
            return f2.compute() + f1.join();
        }
    }

    // Reserved caching BigInteger fibonacci.
    // Problem with it that most threads go to wait an idle, utilization is low.
    static class ReservedCachingScheme<K, V> {
        private final V reserved;
        private final Map<K, V> cache = new ConcurrentHashMap<>();

        ReservedCachingScheme(Supplier<V> reserved) {
            this.reserved = reserved.get();
        }

        // To fix utilization we introduce these lines.
        private class ReservedBlocker implements ForkJoinPool.ManagedBlocker {

            private final K key;
            private volatile V result;

            public ReservedBlocker(K key) {
                this.key = key;
            }

            @Override
            public boolean block() throws InterruptedException {
                synchronized (cache) {
                    while (!isReleasable()) {
                        cache.wait();
                    }
                }
                return true;
            }

            @Override
            public boolean isReleasable() {
                return (result = cache.get(key)) != reserved;
            }
        }
        // == End ===

        public V calculate(K key, Supplier<V> supplier) {
            V result = cache.putIfAbsent(key, reserved);
            if (result == null) {
                result = supplier.get();
                cache.replace(key, reserved, result);
                synchronized (cache) {
                    // Must hold the objects monitor for these operations.
                    cache.notifyAll();
                }
            } else if (result == reserved) {
                // To fix utilization we introduce these lines.
//                synchronized (cache) {
//                    while ((result = cache.get(key)) == reserved) {
//                        try {
//                            cache.wait();
//                        } catch (InterruptedException e) {
//                            throw new CancellationException("interrupted");
//                        }
//                    }
//                }
                ReservedBlocker blocker = new ReservedBlocker(key);
                try {
                    ForkJoinPool.managedBlock(blocker);
                } catch (InterruptedException e) {
                    throw new CancellationException("interrupted");
                }
                return blocker.result;
                // == End ===
            }
            return result;
        }
    }

    public static void main(String... args) {
        long startTime = 0L;

        // Exponential Big O execution time and resource complexity.
        System.out.println("Start rec fibonacci.");
        startTime = System.currentTimeMillis();
        System.out.println("Rec: " + RecursiveFibonacci.recFibonacci(30));
        System.out.println("Elapsed time: " + (System.currentTimeMillis() - startTime) + " ms.");

        // Linear Big O execution time and resource complexity.
        System.out.println("Start linear fibonacci.");
        startTime = System.currentTimeMillis();
        System.out.println("Lin: " + RecursiveFibonacci.linFibonacci(30));
        System.out.println("Elapsed time: " + (System.currentTimeMillis() - startTime) + " ms.");

        // ForkJoin fibonacci.
        System.out.println("Start F/J fibonacci.");
        startTime = System.currentTimeMillis();
        System.out.println("F/J: " + new FibonacciTask(5).invoke());
        System.out.println("Elapsed time: " + (System.currentTimeMillis() - startTime) + " ms.");
    }
}
