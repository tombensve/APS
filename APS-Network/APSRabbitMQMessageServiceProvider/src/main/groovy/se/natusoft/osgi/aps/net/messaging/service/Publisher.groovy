package se.natusoft.osgi.aps.net.messaging.service

import se.natusoft.osgi.aps.api.pubsub.APSPubSubException
import se.natusoft.osgi.aps.api.pubsub.APSPublisher

class Publisher implements APSPublisher<byte[]> {

    //
    // Properties
    //

    /** For sending messages. */
    APSRabbitMQMessageProvider messageProvider

    //
    // Methods
    //

    /**
     * Publishes a message.
     *
     * @param message The message to publish.
     *
     * @throws APSPubSubException on any failure. Note that this is a RuntimeException!
     */
    @Override
    APSPublisher<byte[]> publish( byte[] message ) throws APSPubSubException {
        this.messageProvider.sendMessage( message )
        return this
    }

}
