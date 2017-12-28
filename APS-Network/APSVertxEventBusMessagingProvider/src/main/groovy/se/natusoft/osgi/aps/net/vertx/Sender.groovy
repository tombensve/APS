package se.natusoft.osgi.aps.net.vertx

import io.vertx.core.AsyncResult
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import se.natusoft.osgi.aps.api.pubsub.APSPubSubService
import se.natusoft.osgi.aps.api.pubsub.APSReplyableSender
import se.natusoft.osgi.aps.api.pubsub.APSSender
import se.natusoft.osgi.aps.api.pubsub.APSSubscriber
import se.natusoft.osgi.aps.core.lib.Actions

class Sender implements APSReplyableSender<Map<String, Object>, Map<String, Object>> {

    //
    // Properties
    //

    /** Meta data for the publisher. */
    Map<String, String> meta

    /** Access to the EventBus. */
    Closure<EventBus> getEventBus

    /** The current action list. */
    Actions actions

    //
    // Private Members
    //

    /** The current subscriber. */
    private APSSubscriber<Map<String, Object>> reply

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
//        this.actions.addAction {
            if ( message[ "meta" ] == null ) {
                message[ "meta" ] = this.meta
            }
            String address = this.meta[ APSPubSubService.ADDRESS ]
            if ( reply != null ) {
                getEventBus().send( address, message, { AsyncResult<Message> res ->
                    if ( res.succeeded() ) {
                        Map<String, Object> msg = ( res.result().body() as JsonObject ).map
                        Map<String, String> meta = msg[ "meta" ] as Map<String, String>
                        if ( meta == null ) meta = [ : ]
                        this.reply.apsSubscription( msg, meta )
                    }
                } )
            } else {
                getEventBus().send( address, message )
            }
//        }
//        if ( getEventBus() != null ) this.actions.run()

        return this
    }

    /**
     * This must be called before send(...). send will use the last supplied reply subscriber.
     *
     * @param reply the subscriber to receive reply.
     */
    @Override
    APSSender<Map<String, Object>> replyTo( APSSubscriber<Map<String, Object>> reply ) {
        this.reply = reply
        return this
    }

    /**
     * Returns a read only view of the meta data.
     */
    @Override
    Map<String, String> getMetaView() {
        return Collections.unmodifiableMap( this.meta )
    }

}
