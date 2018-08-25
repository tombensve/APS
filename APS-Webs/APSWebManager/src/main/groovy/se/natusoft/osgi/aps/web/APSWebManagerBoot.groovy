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
 * Serve our web using an HTTP Router published by aps-vertx-provider. I currently use
 * the default http server configuration rather than configuring a separate server
 * on a different port.
 */
// Instantiated, injected, etc by APSActivator. IDE can't see that.
@SuppressWarnings( "unused" )
@CompileStatic
@TypeChecked
class APSWebManagerBoot {

    /** Our _logger. */
    @Managed
    private APSLogger logger

    /** Tracks aps-vertx-provider provided Router instance. */
    @OSGiService( additionalSearchCriteria = "(&(vertx-object=Router)(vertx-router=default))" )
    private APSServiceTracker<Router> routerTracker

    /**
     * This is called after all injections are done.
     */
    @Initializer
    setup() {
        this.routerTracker.onActiveServiceAvailable = { Router router, ServiceReference sr ->

            // This works due to "homepage": "/apsweb" in package.json.
            // Found this tip at: https://github.com/facebook/create-react-app/issues/165
            // Note that if /apsweb below changes the path in package.json must also change!
            //
            // Also note that since this is OSGi and vertx is provided by another bundle
            // it has another ClassLoader and thus also another classpath and will not see
            // our files. Thereby we need to provide our ClassLoader for StaticHandler to
            // be able to load our files.
            router.route( "/apsweb/*" )
                    .handler( StaticHandler.create( "webContent", this.class.classLoader )
                    .setCachingEnabled( false ) )

            this.logger.info "Started web content server for /apsweb!"
        }

        this.routerTracker.onActiveServiceLeaving = { ServiceReference ref, Class api ->

            this.logger.info "/apsweb route is going down!"
        }
    }
}
