package se.natusoft.osgi.aps.api.net.messaging.service;

/**
 * Extends the APSMessageService providing callback functionality for received messages.
 */
public interface APSExtendedMessageService extends APSMessageService {

    /**
     * Adds a listener for messages.
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
     * Listener for messages.
     */
    interface Listener {

        /**
         * This is called when a messaging is received.
         *
         * @param group The messaging group.
         * @param message The messaging.
         */
        void messageReceived(String group, byte[] message);
    }
}
