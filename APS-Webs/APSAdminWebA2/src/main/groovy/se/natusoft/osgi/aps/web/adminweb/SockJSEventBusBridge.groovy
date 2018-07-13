package se.natusoft.osgi.aps.web.adminweb

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.sockjs.BridgeEvent
import io.vertx.ext.web.handler.sockjs.SockJSHandler
import org.osgi.framework.BundleContext
import se.natusoft.osgi.aps.activator.annotation.*
import se.natusoft.osgi.aps.tracker.APSServiceTracker
import se.natusoft.osgi.aps.util.APSLogger

/**
 * Provides a Vertx EventBus bridge.
 */
@SuppressWarnings( [ "GroovyUnusedDeclaration" ] )
@CompileStatic
@TypeChecked
@OSGiServiceProvider( properties = [
        @OSGiProperty( name = "consumed", value = "vertx" ),
        @OSGiProperty( name = "provides", value = "vertx-eventbus-http-bridge" )
] )
class SockJSEventBusBridge implements Constants {
    //
    // Private Members
    //

    @Managed
    private BundleContext context

    @Managed( name = "sockjs-evenbus-bridge", loggingFor = "aps-admin-web-a2:sockjs-eventbus-bridge" )
    private APSLogger logger

    @OSGiService( additionalSearchCriteria = "(vertx-object=Vertx)", timeout = "10 sec" )
    private APSServiceTracker<Vertx> vertxTracker

    @OSGiService( additionalSearchCriteria = "(vertx-object=Router)", timeout = "10 sec" )
    private APSServiceTracker<Router> routerTracker

    /** The Vertx instance. */
    private Vertx vertx

    /** A BusRouter for an HTTP server. */
    private Router router

    //
    // Constructors
    //

    SockJSEventBusBridge() {
        this.vertxTracker.onActiveServiceAvailable  = { Vertx vertx ->
            this.logger.info( "######## SockJSEventBusBridge.onVertxAvailable" )
            this.vertx = vertx
        }

        this.routerTracker.onActiveServiceAvailable = { Router router ->
            if (this.vertx != null) {
                this.logger.info( "######## SockJSEventBusBridge.onRouterAvailable" )
                this.router = router

                // Currently no more detailed permissions than on target address. Might add limits on message contents
                // later.
                def inboundPermitted = [ address: NODE_ADDRESS ]
                def outboundPermitted = [ address: NODE_ADDRESS ]
                def options = [
                        inboundPermitteds : [ inboundPermitted ],
                        outboundPermitteds: [ outboundPermitted ]
                ] as Map<String, Object>

//            SockJSHandler sockJSHandler = SockJSHandler.create( vertx() )
//            BridgeOptions options = new BridgeOptions()
//            sockJSHandler.bridge( options )

                // Note that this router is already bound to an HTTP server!
                SockJSHandler sockJSHandler = SockJSHandler.create( this.vertx )
                sockJSHandler.bridge( options ) { BridgeEvent be ->
                    this.logger.info( "SockJSBridge - Type: ${be.type()}" )
                    be.complete()
                }
                this.router.route( "/eventbus/*" ).handler( sockJSHandler )

                this.logger.info "Vert.x SockJSHandler for event bus bridging started successfully!"
            }
            else {
                this.logger.error( "Vertx no longer available!" )
            }
        }

        this.routerTracker.onActiveServiceLeaving = {
            this.logger.info( "Router was revoked. This bridge will be down until new Router arrives." )
            this.router = null
        }

        this.vertxTracker.onActiveServiceLeaving = {
            this.logger.info( "Vertx was revoked. This bridge will be down until new Vertx arrives." )
            this.vertx = null
        }
    }

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

    @BundleStop
    void shutdown() {
        if ( this.router != null ) {
            this.router.get( "/eventbus/*" ).remove()
        }
        this.logger.disconnectFromLogService( this.context )
    }
}
