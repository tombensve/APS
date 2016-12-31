package se.natusoft.osgi.aps.api.net.messaging.service;

import se.natusoft.docutations.NotNull;

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

    enum Receivers {
        /**
         * Publish to only one subscriber if such is supported by the messaging provider. How and if this
         * is implemented depends on the provider.
         */
        ONE,

        /** Publish to all subscribers. */
        ALL
    }

    /**
     * Sends a message to one destination.
     *
     * @param topic The destination to send message.
     * @param message The message to send. What is allowed here depends on the provider.
     * @param receivers Receivers.ONE or Receivers.ALL. For ONE it is up to the implementation which receiver receives the message.
     */
    void publish(@NotNull String topic, @NotNull Object message, Receivers receivers);

    /**
     * Adds a listener for messages arriving on a specific source.
     *
     * @param topic The endpoint to listen to.
     * @param listener The listener to call with received messages.
     */
    void subscribe(@NotNull String topic, @NotNull Listener listener);

    /**
     * Removes a listener for a source.
     *
     * @param topic The endpoint to remove listener for.
     * @param listener The listener to remove.
     */
    void unsubscribe(@NotNull String topic, @NotNull Listener listener);

    /**
     * This should be implemented by those wanting to receive messages from a source.
     */
    interface Listener {

        /**
         * Called on received message.
         *
         * @param message The message received. This will always be in UTF-8.
         */
        void messageReceived(@NotNull Object message);
    }
}
