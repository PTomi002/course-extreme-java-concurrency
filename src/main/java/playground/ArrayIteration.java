package playground;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Example code for not using the advantage of cache lines in array iterations.
 *
 * Iteration in a row -> columns:
 * time = 33ms
 * rowsCols: -1588282770
 *
 * is much better than iterating in column -> row:
 *
 * time = 1021ms
 * colsRows: -1588282770
 *
 */
public class ArrayIteration {
    private final static int SIZE = 10_000;
    private final static int MAX_VALUE = 1000;

    private final static int[] array =
            ThreadLocalRandom.current()
                    .ints(SIZE * SIZE, 0, MAX_VALUE)
                    .toArray();

    public static void main(String... args) {
        for (int i = 0; i < 30; i++) {
            System.out.println("rowsCols: " + rowsCols());
            System.out.println("colsRows: " + colsRows());
        }
    }

    private static int rowsCols() {
        long time = System.nanoTime();
        try {
            int total = 0;
            for (int row = 0; row < SIZE; row++) {
                for (int col = 0; col < SIZE; col++) {
                    total += array[row * SIZE + col];
                }
            }
            return total;
        } finally {
            time = System.nanoTime() - time;
            System.out.printf("time = %dms%n", (time / 1_000_000));
        }
    }

    private static int colsRows() {
        long time = System.nanoTime();
        try {
            int total = 0;
            for (int col = 0; col < SIZE; col++) {
                for (int row = 0; row < SIZE; row++) {
                    total += array[row * SIZE + col];
                }
            }
            return total;
        } finally {
            time = System.nanoTime() - time;
            System.out.printf("time = %dms%n", (time / 1_000_000));
        }
    }
}