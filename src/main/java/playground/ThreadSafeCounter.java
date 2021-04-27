package playground;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.Immutable;

/**
 * Design a counter class to be thread safe and with invanriant: counter is positive.
 */
@Immutable
public class ThreadSafeCounter {
    // INVARIANT: value >= 0
    @GuardedBy("this")
    private long counter = 0;

    private synchronized long getValue() {
        return counter;
    }

    // PRE_CONDITION: value < Long.MAX
    // POST_CONDITION: new.value = old.value + 1
    private synchronized long increment() {
        if (counter == Long.MAX_VALUE) throw new IllegalStateException("Overflow");
        long oldValue = counter;
        long newValue = ++counter;
        assert counter == oldValue + 1 : "Post condition fails";
        return newValue;
    }

}
