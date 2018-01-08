package se.natusoft.osgi.aps.net.vertx

import io.vertx.core.AsyncResult
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import se.natusoft.osgi.aps.api.pubsub.APSPubSubService
import se.natusoft.osgi.aps.api.pubsub.APSReplyableSender
import se.natusoft.osgi.aps.api.pubsub.APSSender
import se.natusoft.osgi.aps.api.reactive.APSAsyncValue
import se.natusoft.osgi.aps.api.reactive.APSHandler

class Sender implements APSReplyableSender<Map<String, Object>, Map<String, Object>> {

    //
    // Properties
    //

    /** Meta data for the publisher. */
    Map<String, String> meta

    /** Access to the EventBus. */
    Closure<EventBus> getEventBus

    //
    // Private Members
    //

    /** The current subscriber. */
    private APSHandler<APSAsyncValue<Map<String, Object>>> reply

    //
    // Methods
    //

    /**
     * Sends a message. This usually goes to one receiver. See implementaion documentation for more information.
     *
     * @param message The message to send.
     */
    @Override
    APSSender<Map<String, Object>> send( Map<String, Object> message ) {
        if ( message[ "meta" ] == null ) {
            message[ "meta" ] = this.meta
        }
        String address = this.meta[ APSPubSubService.ADDRESS ]
        if ( reply != null ) {
            getEventBus().send( address, message) { AsyncResult<Message> res ->
                if ( res.succeeded() ) {
                    Map<String, Object> msg = ( res.result().body() as JsonObject ).map
                    this.reply.handle( new APSAsyncValue.Provider( msg ) )
                }
            }
        }
        else {
            getEventBus().send( address, message )
        }

        return this
    }

    /**
     * This must be called before send(...). send will use the last supplied reply subscriber.
     *
     * @param reply the subscriber to receive reply.
     */
    @Override
    APSSender<Map<String, Object>> replyTo( APSHandler<APSAsyncValue<Map<String, Object>>> reply ) {
        this.reply = reply
        return this
    }

}
