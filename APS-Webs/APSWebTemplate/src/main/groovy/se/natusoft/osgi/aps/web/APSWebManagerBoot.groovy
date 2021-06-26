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
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.StaticHandler
import org.osgi.framework.ServiceReference
import se.natusoft.osgi.aps.activator.annotation.Initializer
import se.natusoft.osgi.aps.activator.annotation.Managed
import se.natusoft.osgi.aps.activator.annotation.APSPlatformService
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
class APSWebManagerBoot {

    /** Our _logger. */
    @Managed
    private APSLogger logger

    /** Tracks aps-vertx-provider provided Router instance. */
    @APSPlatformService( additionalSearchCriteria = "(&(vertx-object=Router)(vertx-router=default))" )
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
            // be able to loadMapJson our files.
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
