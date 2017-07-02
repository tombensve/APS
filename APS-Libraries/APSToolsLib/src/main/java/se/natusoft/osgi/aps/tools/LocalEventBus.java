package se.natusoft.osgi.aps.tools;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This is an in local VM event bus.
 */
public class LocalEventBus {

    public interface Caller<T> {
        void call(T arg);
    }

    //
    // Private Members
    //

    /** The subscribes per address. */
    private Map<String/*subject*/, List<Caller<Map<String, Object>/*event*/>>/*Subscriber*/> subscribers = new LinkedHashMap<>();

    /** This error handler will be called on exceptions during publish. */
    private Caller<Exception> errorHandler;

    /** This handler will be called on warnings. */
    private Caller<String> warningHandler;

    //
    // Methods
    //

    /**
     * Subscribes to a subject.
     *
     * @param subject The subject to subscribe to.
     * @param subscriber The subscriber to call with messages to the address.
     */
    public LocalEventBus subscribe(String subject, Caller<Map<String, Object>> subscriber) {
        this.subscribers.computeIfAbsent(subject, key -> new LinkedList<>()).add(subscriber);

        return this;
    }

    /**
     * Publish a message on the bus.
     *
     * @param subject The subject to publish to.
     * @param message The message to publish.
     */
    public LocalEventBus publish(String subject, Map<String, Object> message) {
        List<Caller<Map<String, Object>>> subs = this.subscribers.get(subject);

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
                this.warningHandler.call("There are no subscribers for '" + subject + "'!");
            }
        }

        return this;
    }

    /**
     * Sets an error handler.
     *
     * @param errorHandler The error handler to set.
     */
    public LocalEventBus onError(Caller<Exception> errorHandler) {
        this.errorHandler = errorHandler;
        return this;
    }

    /**
     * Sets a warning handler.
     *
     * @param warningHandler The warning handler to set.
     */
    public LocalEventBus onWarning(Caller<String> warningHandler) {
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
