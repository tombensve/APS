package se.natusoft.osgi.aps.net.vertx

import io.vertx.core.AsyncResult
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import se.natusoft.osgi.aps.api.messaging.APSMessageService
import se.natusoft.osgi.aps.api.messaging.APSReplyableSender
import se.natusoft.osgi.aps.api.messaging.APSSender
import se.natusoft.osgi.aps.api.reactive.APSHandler
import se.natusoft.osgi.aps.api.reactive.APSResult
import se.natusoft.osgi.aps.api.reactive.APSValue
import se.natusoft.osgi.aps.json.JSON
import se.natusoft.osgi.aps.tools.APSLogger

class Sender<MessageType> implements APSReplyableSender<MessageType, MessageType> {

    //
    // Properties
    //

    /** Properties for the publisher. */
    Map<String, String> properties

    /** Access to the EventBus. */
    Closure<EventBus> getEventBus

    /** Our logger */
    APSLogger logger

    //
    // Private Members
    //

    /** The current subscriber. */
    private APSHandler<APSValue<Map<String, Object>>> reply

    //
    // Methods
    //

    /**
     * Sends a message. This usually goes to one receiver. See implementaion documentation for more information.
     *
     * @param message The message to send.
     */
    @Override
    APSSender<Map<String, Object>> send( MessageType message ) {
        if ( message[ "_properties_" ] == null ) {
            message[ "_properties_" ] = this.properties
        }

        String address = this.properties[ APSMessageService.TARGET ]

        if ( this.reply != null ) {

            // See comment when handling received message in APSVertxEventBusMessagingProvider.subscribe(...).
            getEventBus().send( address, TypeConv.apsToVertx( message ) ) { AsyncResult<Message<String>> reply ->

                if ( reply.succeeded() ) {

                    Map<String, Object> msg = JSON.stringToMap( reply.result().body() )
                    this.reply.handle( new APSValue.Provider<Map<String, Object>>( msg ) )
                }
            }
        }
        else {

            // See comment when handling received message in APSVertxEventBusMessagingProvider.subscribe(...).
            getEventBus().send( address, TypeConv.apsToVertx( message ) )
        }

        this
    }

    /**
     * Sends a message receiving a result of success or failure. On Success there
     * can be a result value and on failure there is an Exception describing the failure
     * available. This variant never throws an Exception.
     *
     * Providing this variant is optional. When not supported an APSResult containing an
     * APSUnsupportedException and a success() value of false should be the result. That
     * this is not supported should also be made very clear in the documentation of the
     * providing implementation.
     *
     * @param message The message to send.
     */
    @Override
    APSSender<MessageType> send( MessageType message, APSHandler<APSResult<MessageType>> result ) {
        try {
            send( message )

            if ( result != null ) {
                result.handle( APSResult.success( null ) )
            }
            else {
                this.logger.warn( "Call to send(message, resultHandler) was made without a result handler!" )
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

    /**
     * This must be called before send(...). send will use the last supplied reply subscriber.
     *
     * @param reply the subscriber to receive reply.
     */
    @Override
    APSSender<MessageType> replyTo( APSHandler<APSValue<MessageType>> reply ) {
        this.reply = reply

        this
    }

}
