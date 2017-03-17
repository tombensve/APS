package se.natusoft.osgi.aps.net.vertx.api

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.groovy.core.Vertx
import io.vertx.groovy.ext.web.Router
import se.natusoft.osgi.aps.api.reactive.Consumer

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

    //
    // Private Members
    //

    /** Set this to get notified of errors. */
    protected Closure onError

    /** Potential consumer requirements. */
    private Properties requirements = new Properties()

    //
    // Methods
    //

    /**
     * Provide the name of the Vertx instance to consume.
     *
     * @param instanceName The Vertx instance name.
     */
    void setVertxInstanceName(String instanceName) {
        this.requirements[APSVertxService.NAMED_INSTANCE] = instanceName
    }

    /**
     * Provide a port to use for HTTP service.
     * @param port
     */
    void setHttpSericePort(int port) {
        this.requirements[APSVertxService.HTTP_SERVICE_PORT] = "${port}"
    }

    /**
     * Handles onConsumed and forwards to closures if provided. Basically cosmetics ...
     * Or the subclass overrides onConsumed(...).
     *
     * @param status The status of this call.
     * @param vertx The Vertx holder received.
     */
    @SuppressWarnings("PackageAccessibility")
    @Override
    void consume(Consumer.Status status, Consumer.Consumed<Object> consumed) {
        if (status == Consumer.Status.AVAILABLE) {
            if (consumed.get().class == Vertx.class) {
                if (this.onVertxAvailable != null) this.onVertxAvailable.call(consumed)
            }
            else if (consumed.get().class == Router.class) {
                if (this.onRouterAvailable != null) this.onRouterAvailable.call(consumed)
            }
            else {
                if (this.onError != null) this.onError.call("Unknown object consumed! [${consumed.get()}]")
            }
        }
        else if (status == Consumer.Status.UNAVAILABLE) {
            if (this.onVertxUnavilable != null) this.onVertxUnavilable.call()
        }
        else if (status == Consumer.Status.REVOKED) {
            if (this.onVertxRevoked != null) this.onVertxRevoked.call()
        }
    }

    /**
     * Specific options for the consumer.
     */
    @Override
    Properties getConsumerRequirements() {
        return this.requirements
    }

}
