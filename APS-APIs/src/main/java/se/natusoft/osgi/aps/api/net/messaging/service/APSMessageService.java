package se.natusoft.osgi.aps.api.net.messaging.service;

import se.natusoft.docutations.NotNull;
import se.natusoft.docutations.Nullable;

import java.util.Properties;

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
     * Sends a message to one destination.
     *
     * @param topic The destination to send message.
     * @param message The message to send. What is allowed here depends on the provider.
     * @param properties Implementation specific properties. Can be null.
     */
    void publish(@NotNull String topic, @NotNull Object message, @Nullable Properties properties);

    /**
     * Sends a message to one destination.
     *
     * @param topic The destination to send message.
     * @param message The message to send. What is allowed here depends on the provider.
     */
    void publish(@NotNull String topic, @NotNull Object message);

    /**
     * Adds a listener for messages arriving on a specific source.
     *
     * @param topic The endpoint to listen to.
     * @param subscriber The subscriber to call with received messages.
     * @param properties Implementation specific properties. Can be null.
     */
    void subscribe(@NotNull String topic, @NotNull Subscriber subscriber, @Nullable Properties properties);

    /**
     * Adds a listener for messages arriving on a specific source.
     *
     * @param topic The endpoint to listen to.
     * @param subscriber The subscriber to call with received messages.
     */
    void subscribe(@NotNull String topic, @NotNull Subscriber subscriber);

    /**
     * Removes a listener for a source.
     *
     * @param topic The endpoint to remove listener for.
     * @param subscriber The subscriber to remove.
     */
    void unsubscribe(@NotNull String topic, @NotNull Subscriber subscriber);

    /**
     * This should be implemented by those wanting to receive messages from a source.
     */
    interface Subscriber {

        /**
         * Called on received message.
         *
         * @param message The message received.
         */
        void subscription(@NotNull Object message);
    }

    /**
     * An abstract base class to make implementation cleaner when no properties are supported.
     */
    abstract class AbstractAPSMessageService implements APSMessageService {

        /**
         * Sends a message to one destination.
         *
         * @param topic The destination to send message.
         * @param message The message to send. What is allowed here depends on the provider.
         */
        public void publish(@NotNull String topic, @NotNull Object message) {
            publish(topic, message, null);
        }

        /**
         * Adds a listener for messages arriving on a specific source.
         *
         * @param topic The endpoint to listen to.
         * @param subscriber The subscriber to call with received messages.
         */
        public void subscribe(@NotNull String topic, @NotNull Subscriber subscriber) {
            subscribe(topic, subscriber, null);
        }
    }
}
