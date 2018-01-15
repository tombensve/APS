package se.natusoft.osgi.aps.net.messaging.service

import se.natusoft.docutations.Implements
import se.natusoft.osgi.aps.api.pubsub.APSPubSubException
import se.natusoft.osgi.aps.api.pubsub.APSPublisher
import se.natusoft.osgi.aps.api.reactive.APSHandler
import se.natusoft.osgi.aps.api.reactive.APSResult

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
    @Implements(APSPublisher.class)
    APSPublisher<byte[]> publish( byte[] message ) throws APSPubSubException {
        this.messageProvider.sendMessage( message )

        this
    }

    /**
     * Publishes a message receiving a result of success or failure. On Success there
     * can be a result value and on failure there is an Exception describing the failure
     * available. This variant never throws an Exception.
     *
     * Providing this variant is optional. When not supported an APSResult containing an
     * APSUnsupportedException and a success() value of false should be the result. That
     * this is not supported should also be made very clear in the documentation of the
     * providing implementation.
     *
     * @param message The message to publish.
     */
    @Override
    @Implements(APSPublisher.class)
    APSPublisher<byte[]> publish( byte[] message, APSHandler<APSResult<byte[]>> result ) {
        try {
            this.messageProvider.sendMessage( message )
            result.handle( APSResult.success( null ) )
        }
        catch ( Exception e ) {
            result.handle( APSResult.failure( e ) )
        }

        this
    }
}
