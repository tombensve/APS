package se.natusoft.osgi.aps.net.messaging.netty

import se.natusoft.osgi.aps.api.net.messaging.model.APSMessage
import se.natusoft.osgi.aps.api.net.messaging.service.APSMessageProtocol
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiServiceProvider

/**
 * Provides messaging using netty.
 */
@OSGiServiceProvider
class APSNettyMessaginProvider implements APSMessageProtocol {

    /**
     * Sends a message to the destination.
     *
     * @param endpoint The destination to send message.
     * @param message The message to send.
     */
    @Override
    void sendMessage(URI endpoint, APSMessage message) {

    }

    /**
     * Adds a listener for messages arriving on a specific source.
     *
     * @param sourcepoint The source to listen to.
     * @param listener The listener to call with received messages.
     */
    @Override
    void addMessageListener(URI sourcepoint, APSMessageProtocol.Listener listener) {

    }

    /**
     * Removes a listener for a source.
     *
     * @param sourcepoint The source to remove listener for.
     * @param listener The listener to remove.
     */
    @Override
    void removeMessageListener(URI sourcepoint, APSMessageProtocol.Listener listener) {

    }
}
