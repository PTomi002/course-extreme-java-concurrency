package playground;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Run with: -XX:+UnlockDiagnosticVMOptions -XX:+PrintAssembly
 * JITWatch: Log analyser and visualiser for the HotSpot JIT compiler.
 * Then run with: -XX:+PrintTieredEvents
 */
public class PrintDiagnosticVMData {

    private static final int REPEATS = 10;

    public static void main(String... args) throws InterruptedException {
        for (int i = 0; i < REPEATS; i++) {
            System.out.println("Gaussian is: " + ThreadLocalRandom.current().nextGaussian());
            Thread.sleep(1000);
        }
    }
}
