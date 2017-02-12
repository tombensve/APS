package se.natusoft.osgi.aps.api.net.messaging.service;

import se.natusoft.docutations.NotNull;

/**
 * This should be implemented by those wanting to receive messages from a source.
 */
public interface APSSubscriber {

    /**
     * Called on received message.
     *
     * @param topic the topic of the subscribed message.
     * @param message The message received.
     */
    void subscription(@NotNull String topic, @NotNull Object message);

}
