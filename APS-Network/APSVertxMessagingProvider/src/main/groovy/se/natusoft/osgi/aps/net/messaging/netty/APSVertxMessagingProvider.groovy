package se.natusoft.osgi.aps.net.messaging.netty

import se.natusoft.docutations.NotNull
import se.natusoft.docutations.Nullable
import se.natusoft.osgi.aps.api.net.messaging.model.APSMessage
import se.natusoft.osgi.aps.api.net.messaging.service.APSMessageService
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiServiceProvider

/**
 * Provides messaging using netty.
 */
@OSGiServiceProvider
class APSVertxMessagingProvider implements APSMessageService {

    /**
     * Sends a message to the destination.
     *
     * @param topic The destination to send message.
     * @param message The message to send.
     * @param reply If the underlying message mechanism supports replies to specific messages such will be delivered to
     *              this listener. Can be null.
     */
    @Override
    void sendMessage(@NotNull String topic, @NotNull APSMessage message, @Nullable APSMessageService.Listener reply) {

    }

    /**
     * Adds a listener for messages arriving on a specific source.
     *
     * @param topic The endpoint to listen to.
     * @param listener The listener to call with received messages.
     */
    @Override
    void addMessageListener(@NotNull String topic, @NotNull APSMessageService.Listener listener) {

    }

    /**
     * Removes a listener for a source.
     *
     * @param topic The endpoint to remove listener for.
     * @param listener The listener to remove.
     */
    @Override
    void removeMessageListener(@NotNull String topic, @NotNull APSMessageService.Listener listener) {

    }
}
