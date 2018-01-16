package se.natusoft.osgi.aps.tools.util;

/**
 * A very simple wrapper of an object whose access is synchronized.
 * @param <T>
 */
public class Synchronized<T> {
    private T value;

    public synchronized void setValue(T value) {
        this.value = value;
    }

    public synchronized T getValue() {
        return this.value;
    }

    public boolean isEmpty() {
        return this.value == null;
    }
}
