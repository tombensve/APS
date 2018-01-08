package se.natusoft.osgi.aps.api.pubsub;

import se.natusoft.osgi.aps.api.reactive.APSAsyncValue;
import se.natusoft.osgi.aps.api.reactive.APSHandler;
import se.natusoft.osgi.aps.api.util.OnReady;

import java.util.Map;

/**
 * This API is influenced by Vertx :-)
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
     * @param params Parameters for the publisher. These are implementation specific.
     */
    APSPublisher<Message> publisher(Map<String, String> params);

    /**
     * Returns a sender to send with. Depending on implementation the APSSender instance returned can possibly
     * be an APSReplyableSender that allows for providing a subscriber for a reply to the sent message.
     *
     * @param params Parameters for the sender. These are implementation specific.
     */
    APSSender<Message> sender(Map<String, String> params);

    /**
     * Adds a subscriber.
     *
     * @param params     Parameters. This depends on the implementation. Can possibly be null when not used. For example
     *                   if there is a need for an address or topic put it in the params.
     * @param handler    The subscription handler.
     *
     * @return A unique key object representing the subscription. Should be used to cancel the subscription.
     */
    Object subscribe(Map<String, String> params, APSHandler<APSAsyncValue<Message>> handler);

    /**
     * Cancels a subscription.
     *
     * @param id The id received from subscribe(...).
     */
    void unsubscribe(Object id);

}
