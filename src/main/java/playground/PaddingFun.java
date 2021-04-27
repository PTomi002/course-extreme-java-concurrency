package playground;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Example jol-cli:
 * /c/Program\ Files/Java/jdk-11.0.1/bin/java -jar jol-cli-latest.jar internals playground. -cp /c/Users/Paulin\ Tamás/Workplace/demo/extreme_java/build/libs/extreme_java.jar
 */
public class PaddingFun {

    public static void main(String... args) {
        // In Java 1.7 you see
        // private long pad0, pad1, pad2, pad3, pad4, pad5, pad6,p ad7;
        // This was used to prevent the variables to be on the same cache lines.
        ThreadLocalRandom.current().nextInt();
        // In Java 1.8 the class does not contains any long paddings.
    }
}
