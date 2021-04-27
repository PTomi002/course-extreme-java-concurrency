package playground;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Vector;

public class DeadLockFreeVector<E> extends Vector<E> {

// WAS:
//    private void writeObject(ObjectOutputStream s)
//            throws IOException {
//        final ObjectOutputStream.PutField fields = s.putFields();
//        final Object[] data;
//        synchronized (this) {
//            fields.put("capacityIncrement", capacityIncrement);
//            fields.put("elementCount", elementCount);
//            data = elementData.clone();
//        }
//        fields.put("elementData", data);
//        s.writeFields();
//    }

    // FIXED:
    private void writeObject(final ObjectOutputStream s) throws IOException {
        ObjectOutputStream.PutField fields = s.putFields();
        synchronized (this) {
            fields.put("capacityIncrement", capacityIncrement);
            fields.put("elementCount", elementCount);
            fields.put("elementData", elementData.clone());
        }
        s.writeFields();
    }
}
