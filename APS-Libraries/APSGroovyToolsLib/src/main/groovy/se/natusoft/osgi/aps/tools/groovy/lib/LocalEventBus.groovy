package se.natusoft.osgi.aps.tools.groovy.lib

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

/**
 * This is an in local VM event bus.
 */
@CompileStatic
@TypeChecked
class LocalEventBus {

    //
    // Private Members
    //

    /** The subscribes per address. */
    private Map<String/*Address*/, List<Closure<Map<String, Object>/*event*/>>/*Subscriber*/> subscribers = [:]

    /** This error handler will be called on exceptions during publish. */
    private Closure<Exception> errorHandler

    /** This handler will be called on warnings. */
    private Closure<String> warningHandler

    //
    // Methods
    //

    /**
     * Subscribes to an address.
     *
     * @param address The address to subscribe to.
     * @param subscriber The subscriber to call with messages to the address.
     */
    LocalEventBus subscribe(String address, Closure subscriber) {
        List<Closure<Map<String, Object>>> subs = this.subscribers[address]

        if (subs == null) {
            subs = new LinkedList<Closure<Map<String,Object>>>()
            this.subscribers[address] = subs
        }

        subs << subscriber

        this
    }

    /**
     * Publish a message on the bus.
     *
     * @param address The address to publish to.
     * @param message The message to publish.
     */
    LocalEventBus publish(String address, Map<String, Object> message) {
        List<Closure<Map<String, Object>>> subs = this.subscribers[address]

        if (subs != null) {
            subs.each { Closure<Map<String, Object>> subscriber ->
                try { subscriber.call(message) } catch (Exception e) {
                    if (this.errorHandler != null) {
                        this.errorHandler.call(e)
                    }
                }
            }
        }
        else {
            if (this.warningHandler != null) {
                this.warningHandler.call("There are no subscribers for '${address}'!")
            }
        }

        this
    }

    /**
     * Sets an error handler.
     *
     * @param errorHandler The error handler to set.
     */
    LocalEventBus onError(Closure errorHandler) {
        this.errorHandler = errorHandler
        this
    }

    /**
     * Sets a warning handler.
     *
     * @param warningHandler The warning handler to set.
     */
    LocalEventBus onWarning(Closure warningHandler) {
        this.warningHandler = warningHandler
        this
    }

    /**
     * Clears all subscribers.
     */
    void clearSubscribers() {
        this.subscribers.clear()
    }
}

