package playground;

public class StringAppend {

    private static volatile String capture;

    public static void main(String... args) {
        // Test 1
        testStringComplexityOne();

        // Test 2
        testStringComplexityTwo();
    }

    private static void testStringComplexityTwo() {
        for (int j = 1; j < 5; j++) {
            for (int i = 1; i < 2 * 1024 * 1024; i *= 2) {
                long time = System.currentTimeMillis();
                capture = appendAppend(i);
                System.out.println("Append: " + i + " - " + (System.currentTimeMillis() - time) + " ms");
            }
            for (int i = 1; i < 65536; i *= 2) {
                long time = System.currentTimeMillis();
                capture = appendPlusEquals(i);
                System.out.println("PlusEquals: " + i + " - " + (System.currentTimeMillis() - time) + " ms");
            }
        }
    }

    private static void testStringComplexityOne() {
        for (int i = 1; i < 1_000_000; i *= 2) {
            long time = System.currentTimeMillis();
            capture = appendPlusEquals(i);
            System.out.println("PlusEquals: " + i + " - " + (System.currentTimeMillis() - time) + " ms");
            time = System.currentTimeMillis();
            capture = appendAppend(i);
            System.out.println("Append: " + i + " - " + (System.currentTimeMillis() - time) + " ms");
        }
    }

    /**
     * append(...) --> O(1) complexity, in a loop 'n' it is O(n)
     */
    private static String appendAppend(int i) {
        StringBuilder s = new StringBuilder();
        for (int j = 0; j < i; j++) {
            s.append(j);
        }
        return s.toString();
    }

    /**
     * += --> O(n) complexity, in a loop 'n' it is O(n^2)
     * I does not seems quadratic because the HotSpot compiler starts to kick in, profiling the runtime app and optimizing / compiling to native code.
     */
    private static String appendPlusEquals(int i) {
        String s = "";
        for (int j = 0; j < i; j++) {
            s += j;
        }
        return s;
    }
}

/**
 * 1st round:
 * Append: 1 - 0 ms
 * Append: 2 - 0 ms
 * Append: 4 - 0 ms
 * ......
 * Append: 524288 - 12 ms
 * Append: 1048576 - 24 ms
 * <p>
 * 2nd round:
 * Append: 1 - 0 ms
 * Append: 2 - 0 ms
 * Append: 4 - 0 ms
 * .....
 * Append: 524288 - 7 ms // HotSpot compiler had a chance to optimize a little.
 * Append: 1048576 - 16 ms // Two times more the input so the computation time, linear complexity!
 * <p>
 * 3rd round:
 * .....
 * PlusEquals: 8192 - 16 ms
 * PlusEquals: 16384 - 57 ms // Approximately 4 times more than 16 ms.
 * PlusEquals: 32768 - 302 ms // Quadratic complexity!
 */

/**
 * Java8 vs Java9 strings speed:
 * + the complexity is the same, quadratic, but we have a 2-3 times speedup in Java9 compared to Java8.
 */
