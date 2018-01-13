package se.natusoft.osgi.aps.api.pubsub;

import se.natusoft.docutations.Optional;
import se.natusoft.osgi.aps.api.reactive.APSHandler;
import se.natusoft.osgi.aps.api.reactive.APSResult;

public interface APSPublisher<Message> {

    /**
     * Publishes a message.
     *
     * @param message The message to publish.
     *
     * @throws APSPubSubException on any failure. Note that this is a RuntimeException!
     */
    APSPublisher<Message> publish(Message message) throws APSPubSubException;

    /**
     * Publishes a message receiving a result of success or failure. On Success there
     * can be a result value and on failure there is an Exception describing the failure
     * available. This variant never throws an Exception.
     *
     * Providing this variant is optional. When not supported an APSResult containing an
     * APSUnsupportedException and a success() value of false should be the result. That
     * this is not supported should also be made very clear in the documentation of the
     * providing implementation.
     *
     * @param message The message to publish.
     */
    @Optional
    APSPublisher<Message> publish(Message message, APSHandler<APSResult<Message>> result);
}
