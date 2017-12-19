package se.natusoft.osgi.aps.net.vertx.api

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import se.natusoft.osgi.aps.api.pubcon.APSConsumer
import se.natusoft.osgi.aps.api.util.APSMeta
import se.natusoft.osgi.aps.tools.APSLogger

/**
 * This actually implements the Consumer<Vertx> method onConsumed(...) and forwards to 3 closures if
 * made available.
 *
 * NOTE1: APSVertxProvider will use different APSObjectPublishers for each type published so why are we consuming
 *        Object rather than Vertx and Router ? Because generics is a compile time feature, and the compiler can't
 *        tell the difference between APSConsumer<Vertx> and APSConsumer<Router> when both are implemented by this
 *        class. It complains about duplicate interfaces. Thereby we implement APSConsumer<Object> and check what
 *        we got.
 *
 * NOTE2: My first attempt was to make this a trait rather than a class. This however fails with OSGi since
 *        traits seem to produce code in the default package, which is an OSGi no no!
 */
@CompileStatic
@TypeChecked
class VertxConsumer implements APSConsumer<Object> {

    //
    // Properties
    //

    /** Called when a Vertx instance becomes available. */
    Closure onVertxAvailable

    /** Called if the Vertx object is revoked. */
    Closure onVertxRevoked

    /** Called when a HTTP Router is available. */
    Closure onRouterAvailable

    /** Called when a router is revoked. */
    Closure onRouterRevoked

    /** Set this to get notified of errors. */
    Closure onError

    /** Subclasses can provide a logger here if logs from this class is wanted. */
    APSLogger useLogger

    //
    // Private Members
    //

    /** We save this so that we can release it on shutdown. */
    private Vertx vertx

    //
    // Methods
    //

    /**
     * Make Vertx instance available to subclasses.
     */
    protected Vertx vertx() {
        this.vertx
    }

    /**
     * Handles apsConsume and forwards to closures if provided.
     *
     * @param consumed The object consumed. This will be delegated based on type.
     * @param meta Meta data about the consumed object. Delegate method looks at 'status'.
     */
    @SuppressWarnings("PackageAccessibility")
    @Override
    void apsConsume( Object consumed, Map<String, String> meta ) {

        if ( Vertx.class.isAssignableFrom( consumed.class ) ) {
            apsConsumeVertx( consumed as Vertx, meta )
        } else if ( Router.class.isAssignableFrom( consumed.class ) ) {
            apsConsumeRouter( consumed as Router, meta )
        } else {
            String msg = "Unknown object received! [${consumed.class}]"
            this.onError?.call( msg )
            this.useLogger?.error( msg )
        }
    }

    /**
     * Handles consumed Vertx instance.
     *
     * @param vertx The consumed instance.
     * @param meta Meta data about the consumed object. This method looks at 'status'.
     */
    void apsConsumeVertx( Vertx vertx, Map<String, String> meta ) {
        switch ( meta[ "status" ] ) {
            case APSMeta.OBJECT_PUBLISHED_STATE:
                this.vertx = vertx
                this.onVertxAvailable?.call( vertx )
                break
            case APSMeta.OBJECT_REVOKED_STATE:
                this.onVertxRevoked?.call()
                break
            default:
                String msg = "Unknown status for consumed vertx! [${meta[ "status" ]}]"
                this.onError?.call( msg )
                this.useLogger?.error( msg )
        }
    }

    /**
     * Handles consumed Router instance.
     *
     * @param router The consumed Router.
     * @param meta Meta data about the consumed object. This method looks at 'status'.
     */
    void apsConsumeRouter( Router router, Map<String, String> meta ) {
        switch ( meta[ "status" ] ) {
            case APSMeta.OBJECT_PUBLISHED_STATE:
                this.onRouterAvailable?.call( router )
                break
            case APSMeta.OBJECT_REVOKED_STATE:
                this.onRouterRevoked?.call()
                break
            default:
                String msg = "Unknown status for consumed router! [${meta[ "status" ]}]"
                this.onError?.call( msg )
                this.useLogger?.error( msg )
        }
    }

    /**
     * Call this when shutting down. This will release the Vertx instance.
     */
    protected void cleanup() {
        this.vertx = null
        this.onRouterAvailable = null
        this.onVertxRevoked = null
        this.onRouterAvailable = null
        this.onError = null
    }

    //
    // EventBus utilities
    //

    /**
     * Returns the Vert.x event bus.
     */
    protected EventBus eventBus() {
        this.vertx.eventBus()
    }

    /**
     * Extracts the body of a Vertx 'Message'. It accepts both an JsonObject and a
     * string as body content. If the latter it expects the string to contain JSON
     * and wraps it in a JsonObject.
     *
     * Independent of how the JsonMap was created the result of getMap() is returned.
     * Since this is Groovy code and Groovy handles Maps much like JS handles JSON
     * I decided to use the Map format rather than the JsonObject API.
     *
     * @param message The Vertx message to extract body from.
     *
     * @return the body as a Map or null if none where found.
     */
    protected static Map<String, Object> getBody( Message message ) {

        Map<String, Object> event = null

        if ( JsonObject.class.isAssignableFrom( message.body().class ) ) {
            event = ( message.body() as JsonObject ).map
        } else if ( String.class.isAssignableFrom( message.body().class ) ) {
            event = new JsonObject( message.body().toString() ).map
        }

        event
    }

    /**
     * Utility to send a reply to received message.
     *
     * @param receivedMessage The received message to use for reply.
     * @param reply The reply to send.
     */
    protected void eventBusReply( Message receivedMessage, JsonObject reply ) {
        if ( receivedMessage.replyAddress() != null && !receivedMessage.replyAddress().isEmpty() ) {
            receivedMessage.reply( reply )
        } else {
            this.useLogger?.error( "(eventBusReply): Provided 'Message' has not reply address!" )
        }
    }
}
