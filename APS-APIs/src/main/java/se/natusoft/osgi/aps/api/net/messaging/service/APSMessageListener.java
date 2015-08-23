package se.natusoft.osgi.aps.api.net.messaging.service;

/**
 * Listener for APSMessage.
 */
public interface APSMessageListener {

    /**
     * This is called when a message is received.
     *
     * @param message The received message.
     */
    void messageReceived(APSBox message);
}
