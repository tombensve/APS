/*
 *
 * PROJECT
 *     Name
 *         APS Web Manager
 *
 *     Code Version
 *         1.0.0
 *
 *     Description
 *         This project contains 2 parts:
 *
 *         1. A frontend React web app.
 *         2. A Vert.x based backend that serves the frontend web app using Vert.x http router.
 *
 *         Vert.x eventbus is used to communicate between frontend and backend.
 *
 *         This build thereby also builds the frontend by using maven-exec-plugin to run a bash
 *         script that builds the frontend. The catch to that is that it will probably only build
 *         on a unix machine.
 *
 * COPYRIGHTS
 *     Copyright (C) 2012 by Natusoft AB All rights reserved.
 *
 * LICENSE
 *     Apache 2.0 (Open Source)
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 * AUTHORS
 *     tommy ()
 *         Changes:
 *         2019-04-17: Created!
 *
 */
package se.natusoft.osgi.aps.web

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router as WebRouter
import io.vertx.ext.web.handler.StaticHandler
import org.osgi.framework.ServiceReference
import se.natusoft.osgi.aps.activator.annotation.Initializer
import se.natusoft.osgi.aps.activator.annotation.Managed
import se.natusoft.osgi.aps.activator.annotation.OSGiService
import se.natusoft.osgi.aps.tracker.APSServiceTracker
import se.natusoft.osgi.aps.util.APSLogger

/**
 * Serve our web using an Vert.x HTTP Router. I currently use the default http server
 * configuration rather than configuring a separate server on a different port.
 */
@SuppressWarnings( "unused" ) // Instantiated, injected, etc by APSActivator. IDE can't see that.
@CompileStatic
@TypeChecked
class APSWebManagerBoot {
    private static final boolean DEVELOPMENT = false
    private static final boolean PRODUCTION = true

    /** Our _logger. */
    @Managed(loggingFor = "APSWebManagerBoot")
    private APSLogger logger

    /** Tracks aps-vertx-provider provided Router instance. */
    @OSGiService( additionalSearchCriteria = "(&(vertx-object=Router)(vertx-router=default))" )
    private APSServiceTracker<WebRouter> webRouterTracker

    private Route route

    /**
     * This is called after all injections are done.
     */
    @Initializer
    setup() {

        this.webRouterTracker.onActiveServiceAvailable = { WebRouter router, ServiceReference sr ->

            // This works due to "homepage": "/apsweb" in package.json.
            // Found this tip at: https://github.com/facebook/create-react-app/issues/165
            // Note that if /apsweb below changes the path in package.json must also change!
            // TODO: Try changing this to "/*" and "./*" in package.json.
            this.route = router.route( "/apsweb/*" )
                    // Note that the src/main/js frontend build will be copied to src/main/resources/webContent,
                    // by maven-antrun-plugin job, and StaticHandler will serve the files as resources of the jar.
                    .handler( StaticHandler.create( "webContent", this.class.classLoader )
                    .setCachingEnabled( DEVELOPMENT ) )

            this.logger.info "Started web content server for /apsweb!"
        }

        this.webRouterTracker.onActiveServiceLeaving = { ServiceReference ref, Class api ->
            if (this.route != null) this.route.remove(  )

            this.logger.info "/apsweb route is going down!"
        }
    }
}
