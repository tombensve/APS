package se.natusoft.osgi.aps.api.messaging;

import se.natusoft.docutations.NotNull;
import se.natusoft.docutations.Nullable;
import se.natusoft.osgi.aps.model.APSHandler;
import se.natusoft.osgi.aps.model.APSResult;
import se.natusoft.osgi.aps.model.ID;

/**
 * Provides functionality for clients wanting to receive messages.
 *
 * @param <Message> The message type.
 */
public interface APSMessageSubscriber<Message> {

    /**
     * Adds a subscriber.
     *
     * @param destination    The destination to subscribe to.
     *                       This is up to the implementation, but it is strongly recommended that
     *                       this is a name that will be looked up in some configuration for the real
     *                       destination, by the service rather than have the client pass a value from
     *                       its configuration.
     * @param subscriptionId A unique ID used to later cancel the subscription. Use UUID or some other ID
     *                       implementation that is always unique.
     * @param handler        The subscription handler.
     */
    void subscribe(@NotNull String destination, @NotNull ID subscriptionId, @NotNull APSHandler<APSMessage<Message>> handler);

    /**
     * Cancel a subscription.
     *
     * @param subscriptionId The same id as passed to subscribe.
     */
    void unsubscribe(@NotNull ID subscriptionId, @Nullable APSHandler<APSResult> result);

}
