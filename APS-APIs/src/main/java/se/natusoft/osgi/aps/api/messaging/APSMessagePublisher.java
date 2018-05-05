package se.natusoft.osgi.aps.api.messaging;

import se.natusoft.docutations.NotNull;
import se.natusoft.osgi.aps.model.APSHandler;
import se.natusoft.osgi.aps.model.APSResult;

/**
 * This is a publisher which means that it publishes messages to multiple subscribers. See it as a
 * kind of broadcast.
 *
 * @param <Message> The type of the message being published.
 */
public interface APSMessagePublisher<Message> {

    /**
     * Publishes a message.
     *
     * @param destination The destination of the message. Preferably this is something that the
     *                    service looks up to get a real destination, rather than an absolute
     *                    destination.
     * @param message The message to publish.
     *
     * @throws APSMessagingException on any failure. Note that this is a RuntimeException!
     */
    void publish(@NotNull String destination, @NotNull Message message) throws APSMessagingException;

    /**
     * Publishes a message receiving a result of success or failure. On Success there
     * can be a result value and on failure there is an Exception describing the failure
     * available. This variant never throws an Exception.
     *
     * @param destination The destination of the message. Preferably this is something that the
     *                    service looks up to get a real destination, rather than an absolute
     *                    destination.
     * @param message The message to publish.
     * @param result Callback providing the success or failure of the call.
     */
    void publish(@NotNull String destination, @NotNull Message message, @NotNull APSHandler<APSResult<Message>> result);
}
