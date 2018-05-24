package se.natusoft.osgi.aps.web.adminweb

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.sockjs.BridgeEvent
import io.vertx.ext.web.handler.sockjs.SockJSHandler
import org.osgi.framework.BundleContext
import se.natusoft.osgi.aps.activator.annotation.BundleStop
import se.natusoft.osgi.aps.activator.annotation.Initializer
import se.natusoft.osgi.aps.activator.annotation.Managed
import se.natusoft.osgi.aps.activator.annotation.OSGiProperty
import se.natusoft.osgi.aps.activator.annotation.OSGiServiceProvider
import se.natusoft.osgi.aps.api.messaging.APSMessageSubscriber
import se.natusoft.osgi.aps.net.vertx.api.APSVertxService
import se.natusoft.osgi.aps.net.vertx.api.VertxSubscriber
import se.natusoft.osgi.aps.util.APSLogger

/**
 * Provides a Vertx EventBus bridge.
 *
 * ### Suppressed Warnings
 *
 * __GroovyUnusedDeclaration__
 *
 * There are complains that this class is not used, this because it is managed by APSActivator and the IDE
 * cannot see the code that creates and manages this instance.
 *
 * __PackageAccessibility__
 *
 * This is an OSGi issue. OSGi imports and exports packages, and to be deployable a jar must contain a
 * valid MANIFEST.MF with OSGi keys for imports, exports, etc. Must 3rd party jars do contain a valid
 * OSGi MANIFEST.MF exporting all packages of the jar sp that they can just be dropped into an OSGi
 * container and have their classpath be made available to all other code running in the container.
 *
 * The Groovy Vertx wrapper code does not contain a valid OSGi MANIFEST.MF. I have solved this by having
 * the aps-vertx-provider bundle include the Groovy Vertx wrapper, and export all packages of that
 * dependency. So as long as the aps-vertx-provider is deployed the Groovy Vertx wrapper code will
 * also be available runtime. IDEA however does not understand this. It does not figure out the
 * exported dependency from aps-vertx-provider either. So it sees code that is not OSGi compatible
 * and used in the code without including the dependency jar in the bundle, and complains about
 * that. But since in reality this code will be available at runtime I just hide these incorrect
 * warnings.
 *
 * __UnnecessaryQualifiedReference__
 *
 * IDEA does not seem to resolve that the usage of Constants.APP_NAME is outside of the
 * class implementing Constants! It thereby needs to be fully qualified.
 */
@SuppressWarnings( [ "GroovyUnusedDeclaration", "PackageAccessibility", "UnnecessaryQualifiedReference" ] )
@CompileStatic
@TypeChecked
@OSGiServiceProvider( properties = [
        @OSGiProperty( name = "consumed", value = "vertx" ),
        @OSGiProperty( name = APSVertxService.HTTP_SERVICE_NAME, value = Constants.APP_NAME )
] )
class SockJSEventBusBridge extends VertxSubscriber implements APSMessageSubscriber<Vertx>, Constants {
    //
    // Private Members
    //

    @Managed
    private BundleContext context

    @Managed( name = "sockjs-evenbus-bridge", loggingFor = "aps-admin-web-a2:sockjs-eventbus-bridge" )
    private APSLogger logger

    /** A Router for an HTTP server. */
    private Router router

    //
    // Constructors
    //

    SockJSEventBusBridge() {

        this.onVertxAvailable = { Vertx vertx ->
            this.logger.info( "######## SockJSEventBusBridge.onVertxAvailable" )
        }

        this.onRouterAvailable = { Router router ->
            if (vertx() != null) {
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
                SockJSHandler sockJSHandler = SockJSHandler.create( vertx() )
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

        this.onVertxRevoked = {
            this.logger.info( "Vertx was revoked. This bridge will be down until new Vertx arrives." )
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
        cleanup()
        this.logger.disconnectFromLogService( this.context )
    }
}