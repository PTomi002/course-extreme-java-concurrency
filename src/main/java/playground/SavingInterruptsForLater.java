package playground;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

// Coding idiom to save thread interrupted status after computation on a collection.
public class SavingInterruptsForLater<E> {
    private final BlockingQueue<E> queue;

    public SavingInterruptsForLater() {
        this.queue = new LinkedBlockingQueue<>();
    }

    public E takeUnInterruptibly() {
        boolean interrupted = Thread.interrupted(); // Clears the interrupted flag status.
        E result;
        while (true) {
            try {
                result = queue.take();
                // IF it was interrupted or ex happened we should set back the interrupted status.
                if (interrupted) Thread.currentThread().interrupt();
                return result;
            } catch (InterruptedException ex) {
                // Exception clears the interrupted flag status of the thread.
                interrupted = true;
            }
        }
    }

    public void putUnInterruptibly(E element) {
        boolean interrupted = Thread.interrupted(); // Clears the interrupted flag status.
        while (true) {
            try {
                queue.put(element);
                // IF it was interrupted or ex happened we should set back the interrupted status.
                if (interrupted) Thread.currentThread().interrupt();
                return;
            } catch (InterruptedException ex) {
                // Exception clears the interrupted flag status of the thread.
                interrupted = true;
            }
        }
    }

}
