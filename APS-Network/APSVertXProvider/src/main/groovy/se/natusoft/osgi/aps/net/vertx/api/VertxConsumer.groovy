package se.natusoft.osgi.aps.net.vertx.api

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.ext.web.Router
import se.natusoft.docutations.NotNull
import se.natusoft.osgi.aps.tools.reactive.Consumer

/**
 * This actually implements the Consumer<Vertx> method onConsumed(...) and forwards to 3 closures if
 * made available.
 *
 * NOTE: My first attempt was to make this a trait rather than a class. This however fails with OSGi since
 *       traits seem to produce code in the default package, which is an OSGi no no!
 */
@CompileStatic
@TypeChecked
class VertxConsumer implements Consumer<Object> {

    //
    // Properties
    //

    /** Called when a Vertx instance becomes available. */
    Closure onVertxAvailable

    /** Called if it was not possible to make a Vertx instance available. */
    Closure onVertxUnavilable

    /** Called if the Vertx object is revoked. */
    Closure onVertxRevoked

    /** Called when a HTTP Router is available. */
    Closure onRouterAvailable

    /** Set this to get notified of errors. */
    Closure onError

    //
    // Private Members
    //

    /** We save this so that we can release it on shutdown. */
    private Consumer.Consumed<Vertx> vertx

    //
    // Methods
    //

    /**
     * Make Vertx instance available to subclasses.
     */
    protected Vertx vertx() {
        this.vertx.get()
    }

    /**
     * Handles onConsumed and forwards to closures if provided. Basically cosmetics ...
     * Or the subclass overrides onConsumed(...).
     *
     * @param status The status of this call.
     * @param vertx The Vertx holder received.
     */
    @SuppressWarnings( "PackageAccessibility" )
    @Override
    void consume( @NotNull Consumer.Status status, @NotNull Consumer.Consumed<Object> consumed ) {
        if ( status == Consumer.Status.AVAILABLE ) {
            if ( Vertx.class.isAssignableFrom( consumed.get().class ) ) {
                this.vertx = consumed as Consumer.Consumed<Vertx>
                if ( this.onVertxAvailable != null ) this.onVertxAvailable.call( consumed )
            }
            else if ( Router.class.isAssignableFrom( consumed.get().class ) ) {
                if ( this.onRouterAvailable != null ) this.onRouterAvailable.call( consumed )
            }
            else if ( this.onError != null ) {
                this.onError.call( "Unknown object consumed! [${ consumed.get() }]" )
            }
        }
        else if ( status == Consumer.Status.UNAVAILABLE ) {
            if ( this.onVertxUnavilable != null ) this.onVertxUnavilable.call()
        }
        else if ( status == Consumer.Status.REVOKED ) {
            if ( this.onVertxRevoked != null ) this.onVertxRevoked.call()
            this.vertx = null
        }
    }

    protected void cleanup() {
        if ( this.vertx != null ) this.vertx.release()
        this.vertx = null
        this.onRouterAvailable = null
        this.onVertxUnavilable = null
        this.onVertxRevoked = null
        this.onRouterAvailable = null
        this.onError = null
    }
}
