package se.natusoft.osgi.aps.discovery.service.net;

/**
 * A simple wrap around an object whose value cannot be changed like a string. This object wrapper
 * can be passed around and the contents of it changed at any time.
 *
 * @param <Type>
 */
public class ObjectContainer<Type> {
    private Type object = null;

    public synchronized void set(Type object) {
        this.object = object;
    }

    public synchronized Type get() {
        return this.object;
    }
}
