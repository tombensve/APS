package se.natusoft.osgi.aps.api.messaging;

import se.natusoft.osgi.aps.api.reactive.APSHandler;
import se.natusoft.osgi.aps.api.reactive.APSValue;

import java.util.Map;

/**
 * Provides functionality for clients wanting to receive messages.
 *
 * @param <Message> The message type.
 */
public interface APSSubscriber<Message> {
    /**
     * Adds a subscriber.
     *
     * @param properties Properties. This depends on the implementation. Can possibly be null when not used. For example
     *                   if there is a need for an address or topic put it in the properties. This is also unique for
     *                   each call to subscribe(...)! Don't reuse. The call might add information to this, which is
     *                   later needed by unsubscribe(...).
     * @param handler    The subscription handler.
     */
    void subscribe(Map<String, String> properties, APSHandler<APSValue<Message>> handler);

    /**
     * Cancels a subscription.
     *
     * @param properties The same instance as passed to subscribe!
     */
    void unsubscribe(Map<String, String> properties);

}
