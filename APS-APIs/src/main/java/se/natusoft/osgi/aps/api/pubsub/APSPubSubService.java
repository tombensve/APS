package se.natusoft.osgi.aps.api.pubsub;

import se.natusoft.docutations.Optional;

import java.util.Map;

/**
 * @param <Message> The data type published or subscribed to.
 */
public interface APSPubSubService<Message> {

    /**
     * Publishes data.
     *
     * @param message The message to publish.
     * @param meta Can be empty. The usage of this is really up to the implementation and users of it.
     */
    void publish(Message message, Map<String, String> meta);

    /**
     * This is inspired by the Vertx event bus. For any under-laying service that supports replying to
     * a specific message can implement this.
     *
     * Implementation of this is optional. So if this is available or not depends on the implementation.
     * By default this will throw an UnsupportedOperationException.
     *
     * @param message The message to send.
     * @param meta Can be empty. The usage of this is really up to the implementation and users of it.
     * @param reply Will be called with reply if implemented.
     */
    @Optional
    default void send(Message message, Map<String, String> meta, APSSubscriber<Message> reply) {
        throw new UnsupportedOperationException("This method is not supported by this implementation.");
    }

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
     */
    void unsubscribe(APSSubscriber<Message> subscriber);

}
