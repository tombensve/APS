package se.natusoft.osgi.aps.api.net.messaging.types;

import org.joda.time.DateTime;
import se.natusoft.osgi.aps.api.net.messaging.exception.APSMessagingException;
import se.natusoft.osgi.aps.codedoc.Optional;

import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

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
    DateTime getDateTime();

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
     * @param group The group listening to.
     * @param listener The listener to remove.
     */
    void removeMessageListener(String group, Listener listener);

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

}
