package playground;

import net.jcip.annotations.Immutable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Class is immutable if:
 * 1)   state/properties can not be modified after construction
 * 2)   this reference is not exposed / escaped during construction time or later
 */
@Immutable
public class ImmutableExample {
    private final List<String> names = new ArrayList<>();

    public ImmutableExample(String... name) {
        names.addAll(Arrays.asList(name));
        // Immutable way, create a local ref to result of getCountOfNames
        final int local = getCountOfNames();
        // Exposing 'this' reference => Not immutable anymore.
        Executors.newCachedThreadPool().submit(() -> {
            // It would translate the lambda to: this.getCountOfNames()
            // System.out.println("Number of names is: " + getCountOfNames());
            // Immutable way, create a local ref to result of getCountOfNames
            System.out.println("Number of names is: " + local);
        });

    }

    public List<String> getNames() {
        return Collections.unmodifiableList(names);
    }

    public int getCountOfNames() {
        return names.size();
    }
}
