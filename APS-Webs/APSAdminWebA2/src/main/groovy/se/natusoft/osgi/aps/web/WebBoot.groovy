package se.natusoft.osgi.aps.web

import io.vertx.ext.web.Router
import org.osgi.framework.BundleContext
import org.osgi.framework.ServiceReference
import se.natusoft.osgi.aps.activator.annotation.Initializer
import se.natusoft.osgi.aps.activator.annotation.Managed
import se.natusoft.osgi.aps.activator.annotation.OSGiService
import se.natusoft.osgi.aps.net.vertx.tools.APSWebBooter
import se.natusoft.osgi.aps.tracker.APSServiceTracker
import se.natusoft.osgi.aps.util.APSLogger

/**
 * Serve our web using an HTTP Router published by aps-vertx-provider. We currently use
 * the default http server configuration rather than configuring a separate server
 * on a different port.
 */
// Instantiated, injected, etc by APSActivator. IDE can't see that.
@SuppressWarnings( "unused" )
class WebBoot {

    /** Tracks aps-vertx-provider provided Router instance. */
    @OSGiService( additionalSearchCriteria = "(&(vertx-object=Router)(vertx-router=default))" )
    private APSServiceTracker<Router> routerTracker

    /** We need this to pass to WebContentServer so that it can serve files from this bundle. */
    @Managed
    private BundleContext context

    /** The current router. */
    private Router router

    /** An instance of APSWebBooter provided by aps-vertx-provider that uses Vertx to serve /webContent files. */
    private APSWebBooter awb

    /** Our logger. */
    @Managed
    private APSLogger logger

    /**
     * This is called after all injections are done.
     */
    @Initializer
    setup() {
        this.routerTracker.onActiveServiceAvailable = { Router router, ServiceReference sr ->

            this.router = router
            this.awb = new APSWebBooter(
                    context: this.context,
                    router: this.router,
                    servePath: "/aps",
                    serveFilter:"*",
                    resourcePath: "webContent",
                    logger: this.logger
            ).serve()

            this.logger.info( "Started web content server for /aps!" )
        }
        this.routerTracker.onActiveServiceLeaving = { ServiceReference ref, Class api ->

            this.router = null
            this.awb = null
            this.logger.info( "/aps route is going down!" )
        }
    }
}
