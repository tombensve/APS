package se.natusoft.osgi.aps.api.net.messaging.types;

import se.natusoft.osgi.aps.api.net.messaging.exception.APSMessagingException;

import java.util.Properties;

/**
 * This represents a specific cluster.
 */
public interface APSCluster {

    /**
     * Returns the name of this cluster.
     */
    String getName();

    /**
     * Returns the read only properties of this cluster.
     */
    Properties getProperties();

    /**
     * Returns the Clusters common DateTime that is independent of local machine times.
     *
     * Always returns now time.
     */
    APSClusterDateTime getClusterDateTime();

    /**
     * Sets the resolver to use for resolving received messages.
     *
     * @param messageResolver The MessageResolver to set.
     */
    void setMessageResolver(MessageResolver messageResolver);

    /**
     * Sends a messaging.
     *
     * @param message The message to send.
     *
     * @throws APSMessagingException on failure.
     *
     * @return true if the messaging was sent.
     */
    boolean sendMessage(APSMessage message) throws APSMessagingException;

    /**
     * Adds a listener for types.
     *
     * @param listener The listener to add.
     */
    void addMessageListener(Listener listener);

    /**
     * Removes a messaging listener.
     *
     * @param listener The listener to remove.
     */
    void removeMessageListener(Listener listener);

    /**
     * Listener for types.
     */
    interface Listener {

        /**
         * This is called when a messaging is received.
         *
         * @param message The received message.
         */
        void messageReceived(APSMessage message);
    }

    /**
     * This resolves received messages.
     */
    interface MessageResolver {

        /**
         * Returns an APSMessage implementation based on the message data.
         *
         * @param messageData The message data.
         */
        APSMessage resolveMessage(byte[] messageData);
    }
}
