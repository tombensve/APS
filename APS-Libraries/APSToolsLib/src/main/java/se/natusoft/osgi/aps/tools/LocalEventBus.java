package se.natusoft.osgi.aps.tools;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This is an in local VM event bus.
 */
public class LocalEventBus {

    public interface Calle<T> {
        void call(T arg);
    }

    //
    // Private Members
    //

    /** The subscribes per address. */
    private Map<String/*Address*/, List<Calle<Map<String, Object>/*event*/>>/*Subscriber*/> subscribers = new LinkedHashMap<>();

    /** This error handler will be called on exceptions during publish. */
    private Calle<Exception> errorHandler;

    /** This handler will be called on warnings. */
    private Calle<String> warningHandler;

    //
    // Methods
    //

    /**
     * Subscribes to an address.
     *
     * @param address The address to subscribe to.
     * @param subscriber The subscriber to call with messages to the address.
     */
    public LocalEventBus subscribe(String address, Calle<Map<String, Object>> subscriber) {
        List<Calle<Map<String, Object>>> subs = this.subscribers.computeIfAbsent(address, key -> new LinkedList<>());

        subs.add(subscriber);

        return this;
    }

    /**
     * Publish a message on the bus.
     *
     * @param address The address to publish to.
     * @param message The message to publish.
     */
    public LocalEventBus publish(String address, Map<String, Object> message) {
        List<Calle<Map<String, Object>>> subs = this.subscribers.get(address);

        if (subs != null) {
            subs.forEach(subscriber -> {
                try { subscriber.call(message); } catch (Exception e) {
                    if (this.errorHandler != null) {
                        this.errorHandler.call(e);
                    }
                }
            });
        }
        else {
            if (this.warningHandler != null) {
                this.warningHandler.call("There are no subscribers for '" + address + "'!");
            }
        }

        return this;
    }

    /**
     * Sets an error handler.
     *
     * @param errorHandler The error handler to set.
     */
    public LocalEventBus onError(Calle<Exception> errorHandler) {
        this.errorHandler = errorHandler;
        return this;
    }

    /**
     * Sets a warning handler.
     *
     * @param warningHandler The warning handler to set.
     */
    public LocalEventBus onWarning(Calle<String> warningHandler) {
        this.warningHandler = warningHandler;
        return this;
    }

    /**
     * Clears all subscribers.
     */
    public void clearSubscribers() {
        this.subscribers.clear();
    }
}
