package playground;

/**
 * Output can be:
 * 1)   42
 * 2)   no output = reader thread caches the 'ready = false' value into its own cache locally (CPU cache)
 *          after many tries going to the main memory as it is expensive operation
 * 3)   0 = HotSpot compiler might decide that it is cheaper to write the 'ready = true' operation
 *          before the 'number = 42' so the reader thread reads the default 'int = 0' value
 */
public class VisibilityJava {
    private static boolean ready;
    public static int number;

    private static class Reader extends Thread {
        @Override
        public void run() {
            while (!ready) Thread.yield();
            System.out.println("Number is: " + number);
        }
    }

    public static void main(String[] args) {
        new Reader().start();
        number = 42;
        ready = true;
    }
}
