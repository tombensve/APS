package se.natusoft.osgi.aps.net.vertx

import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import se.natusoft.osgi.aps.api.pubsub.APSPubSubException
import se.natusoft.osgi.aps.api.pubsub.APSPubSubService
import se.natusoft.osgi.aps.api.pubsub.APSPublisher

class Publisher implements APSPublisher<Map<String, Object>> {

    //
    // Properties
    //

    /** Meta data for the publisher. */
    Map<String, String> meta

    /** Access to the EventBus. */
    Closure<EventBus> getEventBus

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
    APSPublisher publish( Map<String, Object> message ) throws APSPubSubException {
        String address = this.meta[ APSPubSubService.ADDRESS ]
        getEventBus().publish( address, new JsonObject( message ) )

        this
    }

    /**
     * Returns a read only view of the meta data.
     */
    @Override
    Map<String, String> getMetaView() {
        return Collections.unmodifiableMap( this.meta )
    }
}
