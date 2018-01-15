package se.natusoft.osgi.aps.api.pubsub;

import se.natusoft.osgi.aps.api.reactive.APSValue;
import se.natusoft.osgi.aps.api.reactive.APSHandler;

import java.util.Map;

/**
 * This API is influenced by Vertx.
 *
 * @param <Message> The data type published or subscribed to.
 */
public interface APSPubSubService<Message> {

    //
    // Constants
    //

    /** A topic meta key. For when topics of conversation is relevant */
    String TOPIC = "topic";

    /** An address meta key. For when an address is needed to communicate.  */
    String ADDRESS = "address";


    //
    // Methods
    //

    /**
     * Returns a publisher to publish with.
     *
     * The handler will receive an implementation of APSPublisher from which several publishings
     * can be done using the same params.
     *
     * @param properties Properties for the publisher. These are implementation specific.
     * @param handler Will be called with the APSPublisher to use for publishing messages.
     */
    void publisher(Map<String, String> properties, APSHandler<APSPublisher<Message>> handler);

    /**
     * Returns a sender to send with. Depending on implementation the APSSender instance returned can possibly
     * be an APSReplyableSender that allows for providing a subscriber for a reply to the sent message.
     *
     * The handler will receive an implementation of APSSender from which several messages can be sent
     * using the same params.
     *
     * @param properties Properties for the sender. These are implementation specific.
     * @param handler will be called with the APSSender to use for sending messages.
     */
    void sender(Map<String, String> properties, APSHandler<APSSender<Message>> handler);

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
