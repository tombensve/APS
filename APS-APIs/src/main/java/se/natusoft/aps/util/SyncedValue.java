package se.natusoft.aps.util;

/**
 * Utility to make a value thread synchronized.
 *
 * @param <T> Value type.
 */
public class SyncedValue<T> {

    private T value;

    public SyncedValue() {}

    public SyncedValue(T value) {
        this.value = value;
    }

    public synchronized void setValue(T value) {
        this.value = value;
    }

    public synchronized T getValue() {
        return this.value;
    }
}
