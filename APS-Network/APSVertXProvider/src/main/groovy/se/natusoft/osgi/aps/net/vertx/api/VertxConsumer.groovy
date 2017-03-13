package se.natusoft.osgi.aps.net.vertx.api

import io.vertx.groovy.core.Vertx
import se.natusoft.osgi.aps.api.reactive.Consumer

/**
 * This actually implements the Consumer<Vertx> method onConsumed(...) and forwards to 3 closures if
 * made available.
 *
 * NOTE: My first attempt was to make this a trait rather than a class. This however fails with OSGi since
 *       traits seem to produce code in the default package, which is an OSGi no no!
 */
class VertxConsumer implements Consumer<Vertx> {

    //
    // Properties
    //

    /** Called when a Vertx instance becomes available. */
    Closure onVertxAvailable

    /** Called if it was not possible to make a Vertx instance available. */
    Closure onVertxUnavilable

    /** Called if the Vertx object is revoked. */
    Closure onVertxRevoked

    //
    // Private Members
    //

    /** Potential consumer requirements. */
    private Properties requirements

    //
    // Methods
    //

    /**
     * Provide the name of the Vertx instance to consume.
     *
     * @param instanceName The Vertx instance name.
     */
    void setVertxInstanceName(String instanceName) {
        this.requirements = new Properties()
        properties[ APSVertxService.NAMED_INSTANCE ] = instanceName
    }

    /**
     * Handles onConsumed and forwards to closures if provided. Basically cosmetics ...
     * Or the subclass overrides onConsumed(...).
     *
     * @param status The status of this call.
     * @param vertx The Vertx holder received.
     */
    @Override
    void consume(Consumer.Status status, Consumer.Consumed<Vertx> vertx) {
        if (status == Consumer.Status.AVAILABLE) {
            if (this.onVertxAvailable != null) this.onVertxAvailable.call(vertx)
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
