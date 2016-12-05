package se.natusoft.osgi.aps.api.net.messaging.service;

import se.natusoft.osgi.aps.api.net.messaging.exception.APSMessagingException;
import se.natusoft.osgi.aps.api.net.messaging.model.APSMessage;

/**
 * This represents a specific topic for publishing.
 */
public interface APSTopicPublisher {

    /**
     * Publishes a message for a specific topic.
     *
     * The actual topic should be provided in the service properties using APS.Messaging.Topic as key.
     *
     * @param message The actual message.
     *
     * @throws APSMessagingException on a ny failure to publish message.
     */
    void publish(APSMessage message) throws APSMessagingException;

}

