package se.natusoft.osgi.aps.api.net.messaging.service;

import se.natusoft.osgi.aps.api.net.messaging.model.APSMessage;

import java.net.URI;

/**
 * Protocol. An implementation of a message protocol should register itself with an APS.Messaging.Protocol property of the
 * protocol name. There should only be one implementation for each protocol deployed. If there are more than one for a
 * protocol it is undetermined which will be used.
 *
 * The protocol name is the first part of the URI, before the ':'. So depending on the URIs on an APSMessageTopcis and implementaion
 * of this service will be looked up to handle that actual communication.
 */
public interface APSMessageProtocol {

    /**
     * Sends a message to the destination.
     *
     * @param endpoint The destination to send message.
     * @param message The message to send.
     */
    void sendMessage(URI endpoint, APSMessage message);

    /**
     * Adds a listener for messages arriving on a specific source.
     *
     * @param sourcepoint The source to listen to.
     * @param listener The listener to call with received messages.
     */
    void addMessageListener(URI sourcepoint, Listener listener);

    /**
     * Removes a listener for a source.
     *
     * @param sourcepoint The source to remove listener for.
     * @param listener The listener to remove.
     */
    void removeMessageListener(URI sourcepoint, Listener listener);

    /**
     * This should be implemented by those wanting to receive messages from a source.
     */
    interface Listener {

        /**
         * Called on received message.
         *
         * @param sourcepoint The source of the message.
         * @param message The message received.
         */
        void messageReceived(URI sourcepoint, APSMessage message);
    }
}
