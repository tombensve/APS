package se.natusoft.osgi.aps.api.net.messaging.service;

import se.natusoft.osgi.aps.api.net.messaging.exception.APSMessagingException;
import se.natusoft.osgi.aps.api.net.messaging.model.APSMessage;

/**
 * This represents a subscriber of a message. To receive messages register an implementation of this as an OSGi service
 * and supply a service property of APS.Messaging.Topic and the name of the topic to subscribe to as value.
 */
public interface APSTopicSubscriber {

    /**
     * Gets called when a message of the subscribed to topic arrives.
     *
     * The actual topic should be provided in the service properties using APS.Messaging.Topic as key.
     *
     * @param message The received message.
     *
     * @throws APSMessagingException on any problems with the received data. Consider throwing APSBadMessageException subclass.
     */
    void onSubscribedMessage(APSMessage message) throws APSMessagingException;

}
