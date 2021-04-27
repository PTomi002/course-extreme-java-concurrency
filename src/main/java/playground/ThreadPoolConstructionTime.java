package playground;

import java.util.concurrent.ConcurrentHashMap;

// Run with VM option: -verbose:gc
// Creating and starting a thread is way too many resource and time than constructing a "normal" object.
public class ThreadPoolConstructionTime {

    public static void main(String... args) throws InterruptedException {
        int numOFThreads = 1000;

        // ~ 80 ms construct and start threads in a batch
        Thread[] threads = new Thread[numOFThreads];
        for (int i = 0; i < numOFThreads; i++) {
            long startTime = System.currentTimeMillis();
            for (int j = 0; j < numOFThreads; j++) {
                threads[j] = new Thread();
                threads[j].start();
            }
            System.out.println("Full time: " + (System.currentTimeMillis() - startTime) + " ms.");
            for (int x = 0; x < numOFThreads; x++) {
                threads[x].join();
                threads.wait();
            }
        }

        // ~ 0 or 1 ms construct
        ConcurrentHashMap<Integer, Integer>[] cache = new ConcurrentHashMap[numOFThreads];
        for (int i = 0; i < numOFThreads; i++) {
            long startTime = System.currentTimeMillis();
            for (int j = 0; j < numOFThreads; j++) {
                cache[j] = new ConcurrentHashMap<>();
            }
            System.out.println("Full time: " + (System.currentTimeMillis() - startTime) + " ms.");
            for (int x = 0; x < numOFThreads; x++) {
                cache[x] = null;
            }
        }

    }

}
