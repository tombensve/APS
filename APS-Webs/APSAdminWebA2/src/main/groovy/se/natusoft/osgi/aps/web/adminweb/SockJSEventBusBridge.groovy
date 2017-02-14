package se.natusoft.osgi.aps.web.adminweb

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.AsyncResult
import io.vertx.groovy.core.Vertx
import io.vertx.groovy.ext.web.Router
import io.vertx.groovy.ext.web.handler.sockjs.SockJSHandler
import org.osgi.framework.BundleContext
import se.natusoft.osgi.aps.net.messaging.vertx.api.APSVertxService
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.annotation.activator.BundleStop
import se.natusoft.osgi.aps.tools.annotation.activator.Initializer
import se.natusoft.osgi.aps.tools.annotation.activator.Managed
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiService

// If you are using IntelliJ IDEA then there will be a lot of red in this project since there
// are 2 types of sources in this probject, TypeScript and Groovy. Client, and server. IDEA
// however have serious problems with this. Maven does not.

/**
 * This will provide the SockJS eventbus bridge to the client side.
 */
@SuppressWarnings("PackageAccessibility") // Due to IDEA BS!!
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

    //
    // Methods
    //

    @Initializer
    void init() {
        this.logger.connectToLogService( this.context ) // Connect to OSGi log service if available. APSLogger does not use a timeout when
        // tracking the LogService so it will fail immediately if service is not available,
        // so there is no risk of blocking (which there is when timeout is used).

        this.vertxService.useGroovyVertX( APSVertxService.DEFAULT_INST, { AsyncResult<Vertx> result ->
            if ( result.succeeded() ) {
                this.vertx = result.result()

                Router router = Router.router(this.vertx)

                // Currently no more detailed permissions than on target address. Might add limits on message contents
                // later.
                def twowaysPermitted1 = [ address: "aps.admin.web.event" ]

                this.sockJSHandler = SockJSHandler.create(vertx).bridge( [
                        inboundPermitteds: [ twowaysPermitted1 ],
                        outboundPermitteds: [ twowaysPermitted1 ]
                ] )

                router.route("/eventbus/*").handler(this.sockJSHandler);

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
        this.vertxService.releaseGroovyVertX(APSVertxService.DEFAULT_INST)
    }

}
