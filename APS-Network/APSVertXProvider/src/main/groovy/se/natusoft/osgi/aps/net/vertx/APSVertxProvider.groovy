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
import io.vertx.core.Context
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServer
import io.vertx.ext.web.Router
import org.osgi.framework.BundleContext
import se.natusoft.osgi.aps.core.lib.APSObjectPublisher
import se.natusoft.osgi.aps.exceptions.APSStartFailureException
import se.natusoft.osgi.aps.net.vertx.api.APSVertx
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.annotation.activator.BundleStop
import se.natusoft.osgi.aps.tools.annotation.activator.Initializer
import se.natusoft.osgi.aps.tools.annotation.activator.Managed

// TODO: Make clustering optional.
/**
 * This is managed by APSActivator. It will start up Vertx, and configured sub services, and then publish
 * those to consumers consuming them. When the bundle is shutdown Vertx will also be shutdown and all consumer
 * will be notified about that with a status "revoked".
 *
 * So in other words, this manages Vertx and its services and provides them to whatever wants to use them.
 * Clients/consumers will have to publish APSConsumer<Object> as an OSGi service with the following service
 * properties:
 *
 * __Vertx only:__ consumed=vertx
 *
 * _Vertx and http router:__ consumed=vertx, http-service-name=_name_
 * where _name_ matches a config entry for http servers for different ports.
 *
 * This class makes use of APSObjectPublisher in aps-core-lib to publish vertx and router instances. APSObjectPublisher
 * handles new consumer services becoming available after an object have been published, up until the published object
 * is revoked.
 *
 * ## Example (from test code) making use of VertxConsumer trait.
 *
 *      \@OSGiServiceProvider(properties = [
 *          @OSGiProperty(name = "consumed", value = "vertx"),
 *          @OSGiProperty(name = APSVertx.HTTP_SERVICE_NAME, value = "test")
 *      ])
 *      \@CompileStatic
 *      \@TypeChecked
 *      class VertxConsumerService implements APSConsumer<Vertx>, VertxConsumer {
 *
 *          @Managed(loggingFor = "Test:VertxConsumerService")
 *          APSLogger logger
 *
 *          // Note that this only registers callbacks! The callbacks themselves will not be called until the
 *          // service have been published. This will not happen util after all injections are done. Thereby
 *          // this.logger.info(...) will always work.
 *
 *          VertxConsumerService() {
 *              this.onVertxAvailable = { Vertx vertx ->
 *                  this.logger.info( "Received Vertx instance! [${vertx}]" )
 *                  APSVertxProviderTest.vertx = vertx
 *              }
 *
 *              this.onVertxRevoked = {
 *                  this.logger.info( "Vertx instance revoked!" )
 *              }
 *
 *              this.onRouterAvailable = { Router router ->
 *                  this.logger.info( "Received Router instance! [${router}]" )
 *                  APSVertxProviderTest.router = router
 *              }
 *
 *              this.onError = { String message ->
 *                  this.logger.error( message )
 *              }
 *          }
 *      }
 *
 * ## OSGi upside-down
 *
 * APS uses OSGi a bit upside-down :-) To get information publish an APSConsumer service and the information will be
 * received when available. This makes it reactive, and does not require as much threading as before. Since APSServiceTracker
 * waits for a service to become available (with a timeout) in normal cases it meant that APS bundle activators had to
 * thread startup since a tracker could be hanging waiting for something. When doing it in reverse like described above
 * that is no longer the case. The code will be called when what it needs is available. The only catch is that it is harder
 * to determine if not everything becomes available.
 */
@SuppressWarnings([ "GroovyUnusedDeclaration", "PackageAccessibility" ])
@CompileStatic
@TypeChecked
class APSVertxProvider {

    /**
     * This is stored in the map of named instances.
     */
    private static class NamedVertxInstance {
        /** The vertx instance. */
        Vertx vertx

        /** The context of the vertx instance. */
        Context vertxContext
    }

    //
    // Private Members
    //

    /** Our context */
    @Managed
    private BundleContext context

    /** The logger for this service. */
    @Managed(loggingFor = "aps-vertx-service")
    private APSLogger logger

    private Vertx vertx

    /** A map of HTTP servers per service port. These are internal to this bundle. */
    private Map<Integer, HttpServer> httpServerByPort = [ : ]

    /** A map of Routers for HTTP servers per service port. These are provided to those that wants to serve a path. */
    private Map<Integer, Router> httpServerRouterByPort = [ : ]

    /** Publishes the vertx instance. */
    private APSObjectPublisher<Vertx> vertxPublisher

    /** One publisher for each router instance (not per http service!). */
    private Map<String, APSObjectPublisher<Router>> httpRouterPublishers = [ : ]

    /**
     * Temporary configold handling until the APS configold overhaul.
     */
    private Map<String, Integer> config = [
            vertx_http_service_default           : 9088,
            "vertx_http_service_aps-admin-web-a2": 9080,
            vertx_http_service_test              : 8888
    ]

    private int confPrefixLen = "vertx_http_service_".length()

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

    /**
     * Starts the main Vertx service which in turn will start other Vertx services when up.
     */
    private void startVertx() {
        Vertx.clusteredVertx( [ : ] ) { AsyncResult<Vertx> res ->
            if ( res.succeeded() ) {
                logger.info "Vert.x cluster started successfully!"
                this.vertx = res.result()

                this.vertxPublisher = new APSObjectPublisher<Vertx>( context: this.context, consumerQuery: "(consumed=vertx)" ).init()
                this.vertxPublisher.publish( this.vertx )

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
        stopVertxServices()

        this.vertxPublisher.revoke()

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
        this.config.each { String key, Object value ->
            if ( key.startsWith( "vertx_http_service" ) ) {
                startHttpService( key )
            }
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
            String serviceName = key.substring( this.confPrefixLen )

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
                httpServerRouterByPort[ port ] = router
                httpServer.requestHandler( router.&accept ).listen( port )
                this.logger.info( "HTTP server now listening on port ${port}!" )

                APSObjectPublisher<Router> routerPublisher = new APSObjectPublisher<Router>(
                        context: this.context,
                        consumerQuery: "(&(consumed=vertx)(${APSVertx.HTTP_SERVICE_NAME}=${serviceName}))"
                ).init()
                routerPublisher.publish( router )
                this.httpRouterPublishers.put( key, routerPublisher )
            }
        } else {
            this.logger.error( "No port for HTTP service '${key}'!" )
        }

    }

    /**
     * Stops all Http services and their routers.
     */
    private void stopHttpServices() {
        this.config.each { String key, Object value ->
            if ( key.startsWith( "vertx_http_service" ) ) {
                stopHttpService( key )
            }
        }
    }

    /**
     * Stops a Http service and its router.
     *
     * @param key A key in configuration providing the service port. This is used to find locally cached service.
     */
    private void stopHttpService( String key ) {
        Integer port = this.config[ key ] as Integer

        APSObjectPublisher<Router> routerPublisher = this.httpRouterPublishers.remove( key )
        routerPublisher.revoke()

        Router router = this.httpServerRouterByPort.remove( port )
        router.delete()

        HttpServer httpServer = this.httpServerByPort[ port ]
        httpServer.close() { AsyncResult<Vertx> res ->
            if ( res.succeeded() ) {
                this.logger.info( "Http service '${key}' successfully stopped!" )
            } else {
                this.logger.error( "Stopping http service '${key}' failed!", res.cause() )
            }
        }
    }
}
