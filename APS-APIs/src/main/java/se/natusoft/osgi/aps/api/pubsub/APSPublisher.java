package se.natusoft.osgi.aps.api.pubsub;

public interface APSPublisher<Message> {

    /**
     * Publishes a message.
     *
     * @param message The message to publish.
     *
     * @throws APSPubSubException on any failure. Note that this is a RuntimeException!
     */
    APSPublisher<Message> publish(Message message) throws APSPubSubException;
}
