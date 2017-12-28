package se.natusoft.osgi.aps.api.pubsub;

import se.natusoft.osgi.aps.api.util.OnReady;

import java.util.Map;

/**
 * This API is influenced by Vertx :-)
 *
 * @param <Message> The data type published or subscribed to.
 */
public interface APSPubSubService<Message> extends OnReady {

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
     * @param meta Meta data for the publisher.
     */
    APSPublisher<Message> publisher(Map<String, String> meta);

    /**
     * Returns a sender to send with. Depending on implementation the APSSender instance returned can possibly
     * be an APSReplyableSender that allows for providing a subscriber for a reply to the sent message.
     *
     * @param meta Meta data for the sender.
     */
    APSSender<Message> sender(Map<String, String> meta);

    /**
     * Adds a subscriber.
     *
     * @param subscriber The subscriber to add.
     * @param meta       Meta data. This depends on the implementation. Can possibly be null when not used. For example
     *                   if there is a need for an address or topic put it in the meta data.
     */
    void subscribe(APSSubscriber<Message> subscriber, Map<String, String> meta);

    /**
     * Removes a subscriber.
     *
     * @param subscriber The consumer to remove.
     * @param meta Meta data. This depends on the implementation. Can possibly be null when not used. For example
     *                  if there is a need for an address or topic put it in the meta data.
     */
    void unsubscribe(APSSubscriber<Message> subscriber, Map<String, String> meta);

}
