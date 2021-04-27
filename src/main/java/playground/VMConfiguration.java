package playground;

import java.time.Instant;

/**
 * Start the VM with: -XX:SelfDestructTimer=1
 */
public class VMConfiguration {
    public static void main(String... args) {
        int j = 10;
        System.out.println("Shift left: " + (j << 2));
        j = 10;
        System.out.println("Shift right: " + (j >> 2));
        j = -10;
        System.out.println("Zero fill shift right: " + (j >>> 2));

        Runtime.getRuntime().addShutdownHook(
                new Thread(() -> {
                    for (; ; ) {
                        System.out.println("Hook: " + Instant.now());
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                })
        );

        for (; ; ) {
            System.out.println("Main: " + Instant.now());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
