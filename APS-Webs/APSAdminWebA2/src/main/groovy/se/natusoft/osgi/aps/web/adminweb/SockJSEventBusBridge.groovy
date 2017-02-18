package se.natusoft.osgi.aps.web.adminweb

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.AsyncResult
import io.vertx.groovy.core.Vertx
import io.vertx.groovy.ext.web.Router
import io.vertx.groovy.ext.web.handler.sockjs.SockJSHandler
import org.osgi.framework.BundleContext
import se.natusoft.osgi.aps.net.vertx.api.APSVertxService
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.annotation.activator.BundleStop
import se.natusoft.osgi.aps.tools.annotation.activator.Initializer
import se.natusoft.osgi.aps.tools.annotation.activator.Managed
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiService

/**
 * This will provide the SockJS eventbus bridge to the client side.
 */ //                       v--- Due to IDEA BS!
@SuppressWarnings(["PackageAccessibility", "GroovyUnusedDeclaration"])
@CompileStatic
@TypeChecked
class SockJSEventBusBridge {

    //
    // Managed Members
    //

    @Managed
    private BundleContext context

    @Managed(loggingFor = "aps-sockjs-eventbus-bridge")
    private APSLogger logger

    @OSGiService
    private APSVertxService vertxService

    //
    // Private Members
    //

    private Vertx vertx

    private SockJSHandler sockJSHandler

    private Router router

    //
    // Methods
    //

    @Initializer
    void init() {
        // Connect to OSGi log service if available. APSLogger does not use a timeout when
        // tracking the LogService so it will fail immediately if service is not available,
        // so there is no risk of blocking (which there is when timeout is used).
        this.logger.connectToLogService( this.context )

        this.vertxService.useGroovyVertX( APSVertxService.DEFAULT_INST, { AsyncResult<Vertx> result ->
            if ( result.succeeded() ) {
                this.vertx = result.result()

                this.router = Router.router(this.vertx)

                // Currently no more detailed permissions than on target address. Might add limits on message contents
                // later.
                def twowaysPermitted1 = [ address: "aps.admin.web.event" ]

                this.sockJSHandler = SockJSHandler.create(vertx).bridge( [
                        inboundPermitteds: [ twowaysPermitted1 ] as Object,
                        outboundPermitteds: [ twowaysPermitted1 ] as Object
                ] )

                router.route("/eventbus/*").handler(this.sockJSHandler)

                this.logger.info "Vert.x SockJSEventBusBridge started successfully!"
            }
            else {
                this.logger.error "Vert.x SockJSEventBusBridge failed to start: ${result.cause()}, shutting down bundle!"
                this.context.bundle.stop()
            }
        })
    }

    @BundleStop
    void shutdown() {
        this.router.clear()
        // There is interestingly no stop/shutdown/unhandle/unregister/... for SockJSHandler!
        this.vertxService.releaseGroovyVertX(APSVertxService.DEFAULT_INST)
    }

}
