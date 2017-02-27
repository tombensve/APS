package se.natusoft.osgi.aps.web.adminweb

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.groovy.core.Vertx
import io.vertx.groovy.ext.web.Router
import io.vertx.groovy.ext.web.handler.sockjs.SockJSHandler
import org.osgi.framework.BundleContext
import se.natusoft.osgi.aps.api.reactive.ObjectConsumer
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.annotation.activator.BundleStop
import se.natusoft.osgi.aps.tools.annotation.activator.Initializer
import se.natusoft.osgi.aps.tools.annotation.activator.Managed
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiProperty
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiServiceProvider
import static Constants.*

/**
 * Provides a Vertx EventBus bridge.
 */
@SuppressWarnings(["GroovyUnusedDeclaration", "PackageAccessibility"])
@CompileStatic
@TypeChecked
@OSGiServiceProvider( properties = [ @OSGiProperty( name = "consumed", value = "vertx" ) ] )
class SockJSEventBusBridge implements ObjectConsumer<Vertx> {
    //
    // Private Members
    //

    @Managed
    private BundleContext context

    @Managed( loggingFor = "aps-admin-web-a2:sockjs-eventbus-bridge" )
    private APSLogger logger

    /** A Vertx instance. Received in onObjectAvailable(...). */
    private ObjectConsumer.ObjectHolder<Vertx> vertx

    //
    // Methods
    //

    /**
     * Called after all injections are done.
     */
    @Initializer
    void init() {
        this.logger.connectToLogService( this.context )
    }

    /**
     * Specific options for the consumer.
     */
    @Override
    Properties consumerOptions() { return null }

    /**
     * Called with requested object type when available.
     *
     * @param object The received object.
     */
    @SuppressWarnings("PackageAccessibility")
    @Override
    void onObjectAvailable( ObjectConsumer.ObjectHolder<Vertx> vertx ) {
        this.vertx = vertx

        Router router = Router.router( this.vertx.use() )

        // Currently no more detailed permissions than on target address. Might add limits on message contents
        // later.
        def twowaysPermitted1 = [ address: EVENT.ADDRESS ]

        SockJSHandler sockJSHandler = SockJSHandler.create( this.vertx.use() ).bridge( [
                inboundPermitteds: [ twowaysPermitted1 ] as Object,
                outboundPermitteds: [ twowaysPermitted1 ] as Object
        ] )

        router.route( "/eventbus/*" ).handler( sockJSHandler )

        this.logger.info "Vert.x SockJSHandler for event bus bridging started successfully!"

    }

    /**
     * Called when there is a failure to deliver requested object.
     */
    @Override
    void onObjectUnavailable() {
        this.logger.error "Failed to setup SockJSHandler due to no Vertx instance available!"
    }

    /**
     * Called if/when a previously made available object is no longer valid.
     */
    @Override
    void onObjectRevoked() {
        this.logger.error "Vertx instance have been revoked! Until new becomes available there will be no server access!"
        this.vertx = null
    }

    @BundleStop
    void shutdown() {
        if ( this.vertx != null ) this.vertx.release()
    }
}
