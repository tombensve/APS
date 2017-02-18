package se.natusoft.osgi.aps.web.adminweb

import io.vertx.core.AsyncResult
import io.vertx.groovy.core.Vertx
import org.osgi.framework.BundleContext
import se.natusoft.osgi.aps.net.vertx.api.APSVertxService
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.annotation.activator.Initializer
import se.natusoft.osgi.aps.tools.annotation.activator.Managed
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiService

/**
 *
 */
class EventRouter {

    //
    // Private Members
    //

    @Managed
    private BundleContext context

    @Managed(loggingFor = "aps-admin-web-a2:event-router")
    private APSLogger logger

    @OSGiService
    private APSVertxService vertxService

    /** The root of all Vert.x! */
    private Vertx vertx = null

    @Managed
    private LocalEventBus localBus

    //
    // Initializer
    //

    /**
     * Setup. This is called after all injections are done.
     */
    @Initializer
    void init() {
        this.logger.connectToLogService( this.context )

        this.vertxService.useGroovyVertX( APSVertxService.DEFAULT_INST, { AsyncResult<Vertx> result ->
            if ( result.succeeded() ) {
                this.vertx = result.result()

                this.logger.info "Vert.x cluster started successfully!"
            }
            else {
                this.logger.error "Vert.x cluster failed to start: ${result.cause()}, shutting down bundle!"
                this.context.bundle.stop()
            }
        })
    }


}
