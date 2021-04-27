package playground;

import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;

import java.util.concurrent.atomic.AtomicReference;

@ThreadSafe
public class NonBlockingStack<T> {

    private final AtomicReference<Node<T>> top = new AtomicReference<>();

    @Immutable
    static class Node<T> {
        final T element;
        final Node<T> next;

        Node(T element, Node<T> next) {
            this.element = element;
            this.next = next;
        }
    }

    public void push(T item) {
        Node<T> cHead, nHead;
        do {
            cHead = top.get(); // Witness value.
            nHead = new Node<>(item, cHead);
        } while (!top.compareAndSet(cHead, nHead)); // CAS is false if it lost the race.
    }

    public T pop() {
        Node<T> cHead, nHead;
        do {
            cHead = top.get(); // Witness value.
            if (cHead == null) return null;
            nHead = cHead.next;
        } while (!top.compareAndSet(cHead, nHead));
        return cHead.element;
    }

    public static void main(String... args) {

    }
}
