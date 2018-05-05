package se.natusoft.osgi.aps.api.messaging;

import se.natusoft.docutations.NotNull;
import se.natusoft.docutations.Nullable;
import se.natusoft.osgi.aps.model.APSHandler;
import se.natusoft.osgi.aps.model.APSResult;

/**
 * This is a sender that sends a message to zero or one subscribers. If there are more than one subscriber
 * to the destination then it is up to the implementation who gets the message.
 *
 * For a Vertx eventbus based implementation it would do a round robin when there are more than one
 * subscriber for example. But this is entirely up to an implementation to failure.
 *
 * @param <Message> The type of the message being sent.
 */
public interface APSMessageSender<Message> {

    /**
     * Sends a message. This usually goes to one receiver. See implementaion documentation for more information.
     *
     * @param destination The destination of the message. Preferably this is something that the
     *                    service looks up to get a real destination, rather than an absolute
     *                    destination.
     * @param message The message to send.
     */
    void send(@NotNull String destination, @NotNull Message message);

    /**
     * Sends a message receiving a result of success or failure. On Success there
     * _can_ be a result value and on failure there is an Exception describing the failure
     * available. This variant never throws an Exception.
     *
     * @param destination The destination of the message. Preferably this is something that the
     *                    service looks up to get a real destination, rather than an absolute
     *                    destination.
     * @param message The message to send.
     * @param result  The result of the send. If null an APSMessagingException will be thrown on failure.
     */
    void send(@NotNull String destination, @NotNull Message message, @Nullable APSHandler<APSResult> result);

}
