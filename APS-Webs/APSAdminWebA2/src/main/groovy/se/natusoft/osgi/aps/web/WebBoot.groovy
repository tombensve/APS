package se.natusoft.osgi.aps.web

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.StaticHandler
import org.osgi.framework.ServiceReference
import se.natusoft.osgi.aps.activator.annotation.Initializer
import se.natusoft.osgi.aps.activator.annotation.Managed
import se.natusoft.osgi.aps.activator.annotation.OSGiService
import se.natusoft.osgi.aps.tracker.APSServiceTracker
import se.natusoft.osgi.aps.util.APSLogger

/**
 * Serve our web using an HTTP Router published by aps-vertx-provider. We currently use
 * the default http server configuration rather than configuring a separate server
 * on a different port.
 */
// Instantiated, injected, etc by APSActivator. IDE can't see that.
@SuppressWarnings( "unused" )
@CompileStatic
@TypeChecked
class WebBoot {

    /** Our logger. */
    @Managed
    private APSLogger logger

    /** Tracks aps-vertx-provider provided Router instance. */
    @OSGiService( additionalSearchCriteria = "(&(vertx-object=Router)(vertx-router=default))" )
    private APSServiceTracker<Router> routerTracker

    /** The current router. */
    private Router router

    /**
     * This is called after all injections are done.
     */
    @Initializer
    setup() {
        this.routerTracker.onActiveServiceAvailable = { Router router, ServiceReference sr ->

            this.router = router

            // Handle the dumbness of root /static/... What the (BEEP) did they think about when
            // they came up with this ? It makes it more or less impossible to serve multiple web
            // apps at different root paths on the same port. When I edited the React files compiled
            // by babel and removed the '/' from '/static/...' the web app worked just fine. Maybe
            // I should write some editing code that does that during build ...
            this.router.route( "/static/*" )
                    .handler( StaticHandler.create( "webContent/static", this.class.classLoader )
                    .setCachingEnabled( false ) )

            // Yeah ... out of words ... (generated and referenced file not in source! Seems to do some caching.)
            this.router.route( "/service-worker.js" )
                    .handler( StaticHandler.create( "webContent", this.class.classLoader )
                    .setCachingEnabled( false ) )

            // And this is the only one that should be relevant! Every single file used by the frontend app
            // is available under 'webContent'. There are just crazy references from other non needed roots!
            this.router.route( "/aps/*" )
                    .handler( StaticHandler.create( "webContent", this.class.classLoader )
                    .setCachingEnabled( false ) )

            this.logger.info "Started web content server for /aps!"
        }

        this.routerTracker.onActiveServiceLeaving = { ServiceReference ref, Class api ->

            this.router = null
            this.logger.info "/aps route is going down!"
        }
    }
}
