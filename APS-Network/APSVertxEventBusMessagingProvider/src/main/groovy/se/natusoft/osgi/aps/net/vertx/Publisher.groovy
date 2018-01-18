package se.natusoft.osgi.aps.net.vertx

import io.vertx.core.eventbus.EventBus
import se.natusoft.osgi.aps.api.messaging.APSMessageService
import se.natusoft.osgi.aps.api.messaging.APSMessagingException
import se.natusoft.osgi.aps.api.messaging.APSPublisher
import se.natusoft.osgi.aps.api.reactive.APSHandler
import se.natusoft.osgi.aps.api.reactive.APSResult
import se.natusoft.osgi.aps.json.JSON
import se.natusoft.osgi.aps.json.JSONObject
import se.natusoft.osgi.aps.tools.APSLogger

/**
 * Handles publishing. This to publish multiple messages using same meta.
 */
// MessageType will always be Object!
class Publisher<MessageType> implements APSPublisher<MessageType> {

    //
    // Properties
    //

    /** Properties for the publisher. */
    Map<String, String> properties

    /** Access to the EventBus. */
    Closure<EventBus> getEventBus

    /** Our logger. */
    APSLogger logger

    //
    // Methods
    //

    /**
     * Publishes a message.
     *
     * @param message The message to publish.
     *
     * @throws APSMessagingException on any failure. Note that this is a RuntimeException!
     */
    @Override
    APSPublisher<MessageType> publish( MessageType message ) throws APSMessagingException {
        String address = this.properties[ APSMessageService.TARGET ]

        getEventBus().publish( address, TypeConv.apsToVertx( message ) )

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
    APSPublisher<MessageType> publish( MessageType message, APSHandler<APSResult<MessageType>> result ) {
        String address = this.properties[ APSMessageService.TARGET ]
        try {

            getEventBus().publish( address, TypeConv.apsToVertx( message ) )

            if ( result != null ) {
                result.handle( APSResult.success( null ) )
            }
            else {
                this.logger.warn( "Call made to publish(message, resultHandler) without a result handler!" )
            }
        }
        catch ( Exception e ) {

            if ( result != null ) {
                result.handle( APSResult.failure( e ) )
            }
            else {
                this.logger.error( e.message, e )
            }
        }
        this
    }
}
