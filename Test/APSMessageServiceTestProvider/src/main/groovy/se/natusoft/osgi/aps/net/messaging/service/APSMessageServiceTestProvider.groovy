package se.natusoft.osgi.aps.net.messaging.service

import se.natusoft.osgi.aps.api.net.messaging.exception.APSMessagingException
import se.natusoft.osgi.aps.api.net.messaging.service.APSMessageService
import se.natusoft.osgi.aps.api.net.messaging.types.APSMessage
import se.natusoft.osgi.aps.api.net.messaging.types.APSMessageListener
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiServiceProvider

/**
 * This is an implementation to be used for running unit tests. All parts that need to communicate with
 * each other should work with the same instance of this service. No networking is involved here!
 * Messages will be sent directly to listeners in memory.
 *
 * This allows running unit tests of services that work on top of APSMessageService.
 */
@OSGiServiceProvider
class APSMessageServiceTestProvider implements APSMessageService {

    //
    // Private Members
    //

    /** The registered listeners. */
    private List<APSMessageListener> listeners = new LinkedList<>()

    //
    // Methods
    //

    /**
     * Returns the name of this instance.
     */
    @Override
    String getName() {
        return "test"
    }

    /**
     * Returns general informative information about this instance. Implementations can return "" if they want.
     */
    @Override
    String getMessagingProviderInfo() {
        return null
    }

    /**
     * Adds a listener for types.
     *
     * @param listener The listener to add.
     */
    @Override
    synchronized void addMessageListener(APSMessageListener listener) {
        this.listeners.add(listener)
    }

    /**
     * Removes a messaging listener.
     *
     * @param listener The listener to remove.
     */
    @Override
    synchronized void removeMessageListener(APSMessageListener listener) {
        this.listeners.remove(listener)
    }

    /**
     * Sends a message.
     *
     * @param message The message to send.
     *
     * @throws se.natusoft.osgi.aps.api.net.messaging.exception.APSMessagingException on failure.
     */
    @Override
    synchronized void sendMessage(APSMessage message) throws APSMessagingException {
        this.listeners.each { APSMessageListener listener ->
            listener.messageReceived(message)
        }
    }
}
