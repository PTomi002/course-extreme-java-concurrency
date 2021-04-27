package playground;

public class UnstoppableThread {
    public static void main(String... args) throws InterruptedException {
        Thread undead = new Thread() {
            @Override
            public void run() {
                try {
                    while (true) {
                        try {
                            System.out.println("Not dead yet!");
                        } catch (Throwable t) {
                            System.out.println("We saw " + t.getMessage() + " this already!");
                        }
                    }
                } finally {
                    // ThreadDeath exception is ignored here and recursively recalled itself to run.
                    run();
                }
            }
        };

        undead.start();

        Thread.sleep(5000);

        while (undead.isAlive()) {
            undead.interrupt();
            undead.stop();
        }
    }
}
