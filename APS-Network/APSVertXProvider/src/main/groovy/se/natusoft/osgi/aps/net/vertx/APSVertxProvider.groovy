/*
 *
 * PROJECT
 *     Name
 *         APS VertX Provider
 *
 *     Code Version
 *         1.0.0
 *
 *     Description
 *         This service provides configured Vertx instances allowing multiple services to use the same Vertx instance.
 *
 *         This service also provides for multiple instances of VertX by associating an instance with a name. Everyone
 *         asking for the same name will get the same instance.
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
 *         2017-01-01: Created!
 *
 */
package se.natusoft.osgi.aps.net.vertx

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.AsyncResult
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.core.http.HttpServer
import io.vertx.ext.web.Router
import org.osgi.framework.BundleContext
import org.osgi.framework.ServiceRegistration
import se.natusoft.osgi.aps.exceptions.APSStartFailureException
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.annotation.activator.BundleStop
import se.natusoft.osgi.aps.tools.annotation.activator.Initializer
import se.natusoft.osgi.aps.tools.annotation.activator.Managed

// TODO: Make clustering optional.

// @formatter:off
/**
 *
 */
// @formatter:on

@SuppressWarnings([ "GroovyUnusedDeclaration", "PackageAccessibility" ])
@CompileStatic
@TypeChecked
class APSVertxProvider {

    //
    // Constants
    //

    private static final String HTTP_CONF_PREFIX = "vertx_http_service_"

    //
    // Private Members
    //

    /** Our context */
    @Managed
    private BundleContext context

    /** The logger for this service. */
    @Managed(loggingFor = "aps-vertx-provider")
    private APSLogger logger

    /** The vertx instance. */
    Vertx vertx

    /** The vertx service registration. */
    private ServiceRegistration vertxSvcReg

    /** Service registration for event bus. */
    private ServiceRegistration eventBusSvcReg

    /** A map of HTTP servers per service port. These are internal to this bundle. */
    private Map<Integer, HttpServer> httpServerByPort = [ : ]

    /** A map of Routers for HTTP servers per service port. These are provided to those that wants to serve a path. */
    private Map<Integer, Router> httpServerRouterByPort = [ : ]

    /** Service registrations for routers. */
    private Map<Integer, ServiceRegistration> routerRegByPort = [ : ]

    /**
     * Temporary config handling until the APS config overhaul.
     */
    private Map<String, Integer> config = [
            vertx_http_service_default           : 9088,
            "vertx_http_service_aps-admin-web-a2": 9080,
            vertx_http_service_test              : 8888
    ]

    /**
     * This gets called after all injections are done.
     */
    @Initializer
    void init() {
        this.logger.connectToLogService( this.context )

        startVertx()
    }

    @BundleStop
    void shutdown() {
        stopVertx()

        this.logger.disconnectFromLogService( this.context )
    }

    //
    // Methods
    //

    private static String toRealHttpKey( String key ) {
        return key.substring( HTTP_CONF_PREFIX.length() )
    }

    /**
     * Starts the main Vertx service which in turn will start other Vertx services when up.
     */
    private void startVertx() {
        Vertx.clusteredVertx( [ : ] ) { AsyncResult<Vertx> res ->
            if ( res.succeeded() ) {
                logger.info "Vert.x cluster started successfully!"
                this.vertx = res.result()

                this.vertxSvcReg = this.context.registerService( Vertx.class.getName(), vertx, [
                        "service-provider": "aps-vertx-provider",
                        "service-category": "network",
                        "service-function": "client/server",
                        "vertx-object"    : "Vertx"
                ] as Properties )
                this.logger.info( "Registered Vertx as OSGi service!" )

                this.eventBusSvcReg = this.context.registerService( EventBus.class.name, this.vertx.eventBus(), [
                        "service-provider": "aps-vertx-provider",
                        "service-category": "network",
                        "service-function": "client/server",
                        "vertx-object"    : "EventBus"
                ] as Properties )
                this.logger.info( "Registered EventBus as OSGi service!" )

                startVertxServices()

            } else {
                logger.error "Vert.x cluster failed to start!", res.cause()
                throw new APSStartFailureException( "Vert.x cluster failed to start!", res.cause() )
            }
        }
    }

    /**
     * Stops the main Vertx service which first will stop other vertx services.
     */
    private void stopVertx() {
        this.vertxSvcReg.unregister()
        this.logger.info( "Unregistered Vertx as OSGi service!" )
        this.eventBusSvcReg.unregister()
        this.logger.info( "Unregistered EventBus as OSGi service!" )

        stopVertxServices()

        this.vertx.close() { AsyncResult<Vertx> res ->
            if ( res.succeeded() ) {
                logger.info "Vert.x cluster stopped successfully!"
            } else {
                logger.error "Vert.x cluster failed to shutdown!: ${res.cause()}!"
            }
        }
    }

    /**
     * Start all Vertx sub services.
     */
    void startVertxServices() {
        startHttpServices()
    }

    /**
     * Stop all Vertx sub services.
     */
    void stopVertxServices() {
        stopHttpServices()
    }

    /**
     * Starts all Vertx Http services and a router for each. It is the router that is published to clients.
     */
    private void startHttpServices() {
        this.config.findAll { String key, Object value -> key.startsWith( HTTP_CONF_PREFIX ) }.each { String key, Object value ->
            startHttpService( key )
        }
    }

    /**
     * Starts a specific Http servcie and router.
     *
     * @param key A key in configuration providing a port to listen to.
     */
    private void startHttpService( String key ) {
        Integer port = this.config[ key ] as Integer

        if ( port != null ) {

            // We keep a server for each listened to port.
            HttpServer httpServer = httpServerByPort[ port ] // TODO: Currently single threaded server!!
            if ( httpServer == null ) {
                httpServer = vertx.createHttpServer( /* TODO: Provide options. */ )
                httpServerByPort[ port ] = httpServer
            }

            // Consumers don't get direct access to the HttpServer, only to its Router.
            Router router = httpServerRouterByPort[ port ]
            if ( router == null ) {
                router = Router.router( vertx )
                this.logger.info("Created router for config '${toRealHttpKey( key )}'!")
                httpServerRouterByPort[ port ] = router
                httpServer.requestHandler( router.&accept ).listen( port )
                this.logger.info( "HTTP server for config '${toRealHttpKey( key )}' now listening on port ${port}!" )
                this.routerRegByPort[ port ] = this.context.registerService( Router.class.name, router, [
                        "service-provider": "aps-vertx-provider",
                        "service-category": "network",
                        "service-function": "client/server",
                        "vertx-object"    : "Router",
                        "vertx-router"    : toRealHttpKey( key )
                ] as Properties )
                this.logger.info( "Registered HTTP service 'Router' for config '${toRealHttpKey( key )}' as OSGi service!" )

            }
        } else {
            this.logger.error( "No port for HTTP service '${key}'!" )
        }

    }

    /**
     * Stops all Http services and their routers.
     */
    private void stopHttpServices() {
        this.config.findAll { String key, Object value -> key.startsWith( HTTP_CONF_PREFIX ) }.each { String key, Object value ->
            stopHttpService( key )
        }
    }

    /**
     * Stops a Http service and its router.
     *
     * @param key A key in configuration providing the service port. This is used to find locally cached service.
     */
    private void stopHttpService( String key ) {
        Integer port = this.config[ key ] as Integer

        this.routerRegByPort.remove( port ).unregister()
        this.logger.info( "Unregistered 'Router' for config '${toRealHttpKey( key )}' as OSGi service!" )

        this.httpServerRouterByPort.remove( port ).delete()
        this.logger.info( "Deleted 'Router' for config '${toRealHttpKey( key )}' as OSGi service!" )

        this.httpServerByPort.remove( port ).close() { AsyncResult<Vertx> res ->
            if ( res.succeeded() ) {
                this.logger.info( "Http service '${toRealHttpKey( key )}' successfully stopped!" )
            } else {
                this.logger.error( "Stopping http service '${key}' failed!", res.cause() )
            }
        }
    }

}
