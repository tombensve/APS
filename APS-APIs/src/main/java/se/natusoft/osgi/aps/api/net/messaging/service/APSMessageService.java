package se.natusoft.osgi.aps.api.net.messaging.service;

import se.natusoft.docutations.NotNull;
import se.natusoft.docutations.Nullable;
import se.natusoft.osgi.aps.api.net.messaging.model.APSMessage;

import java.net.URI;

/**
 * Implementations of this should register themselves with an APS.Messaging.Protocol.Name property with the
 * implemented protocol name. A protocol name of APS.Value.Messaging.Protocol.ROUTER means that the implementation
 * will use the APSMessageTopics service to map the topic to a protocol and track an implementation of
 * APSMessageService for that protocol and then forward to that.
 *
 * There should only be one implementation for each protocol deployed. If there are more than one for a
 * protocol it is undetermined which will be used.
 */
public interface APSMessageService {

    /**
     * Sends a message to the destination.
     *
     * @param topic The destination to send message.
     * @param message The message to send.
     * @param reply If the underlying message mechanism supports replies to specific messages such will be delivered to
     *              this listener. Can be null.
     */
    void sendMessage(@NotNull String topic, @NotNull APSMessage message, @Nullable Listener reply);

    /**
     * Adds a listener for messages arriving on a specific source.
     *
     * @param topic The endpoint to listen to.
     * @param listener The listener to call with received messages.
     */
    void addMessageListener(@NotNull String topic, @NotNull Listener listener);

    /**
     * Removes a listener for a source.
     *
     * @param topic The endpoint to remove listener for.
     * @param listener The listener to remove.
     */
    void removeMessageListener(@NotNull String topic, @NotNull Listener listener);

    /**
     * This should be implemented by those wanting to receive messages from a source.
     */
    interface Listener {

        /**
         * Called on received message.
         *
         * @param message The message received.
         */
        void messageReceived(APSMessage message);
    }
}
