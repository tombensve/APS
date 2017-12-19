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
import se.natusoft.osgi.aps.api.pubcon.APSConsumer
import se.natusoft.osgi.aps.core.lib.APSObjectPublisher
import se.natusoft.osgi.aps.exceptions.APSStartFailureException
import se.natusoft.osgi.aps.net.vertx.api.APSVertx
import se.natusoft.osgi.aps.net.vertx.api.VertxConsumer
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.annotation.activator.BundleStop
import se.natusoft.osgi.aps.tools.annotation.activator.Initializer
import se.natusoft.osgi.aps.tools.annotation.activator.Managed

import java.util.concurrent.ConcurrentHashMap

// TODO: Make clustering optional.
/**
 * Implements APSVertXService and also calls all DataConsumer<Vertx> services found with an Vertx instance.
 *
 * Do consider using the DataConsumer.DataConsumerProvider utility implementation for callback delivery of Vertx.
 */
@SuppressWarnings([ "GroovyUnusedDeclaration", "PackageAccessibility" ])
@CompileStatic
@TypeChecked
class APSVertxProvider extends VertxConsumer implements APSConsumer<Vertx> {

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
    private Map<Integer, HttpServer> httpServerByPort = new ConcurrentHashMap<>()

    /** A map of Routers for HTTP servers per service port. These are provided to those that wants to serve a path. */
    private Map<Integer, Router> httpServerRouterByPort = new ConcurrentHashMap<>()

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

    /**
     * This gets called after all injections are done.
     */
    @Initializer
    void init() {
        this.logger.connectToLogService( this.context )

        this.vertxPublisher = new APSObjectPublisher<Vertx>( context: this.context, consumerQuery: "(consumed=vertx)" )

        startVertx()

        startConfiguredHttpServices(  )
    }

    @BundleStop
    void shutdown() {
        stopConfiguredHttpServices(  )

        stopVertx()

        this.logger.disconnectFromLogService( this.context )
    }

    //
    // Methods
    //

    private void startVertx() {
        Vertx.clusteredVertx( [ : ] ) { AsyncResult<Vertx> res ->
            if ( res.succeeded() ) {
                logger.info "Vert.x cluster started successfully!"
                this.vertx = res.result()
                this.vertxPublisher.publish( this.vertx )
            } else {
                logger.error "Vert.x cluster failed to start!", res.cause()
                throw new APSStartFailureException( "Vert.x cluster failed to start!", res.cause() )
            }
        }
    }

    private void stopVertx() {
        this.vertxPublisher.revoke()

        this.vertx.close() { AsyncResult<Vertx> res ->
            if ( res.succeeded() ) {
                logger.info "Vert.x cluster stopped successfully!"
            } else {
                logger.error "Vert.x cluster failed to shutdown!: ${res.cause()}!"
            }
        }
    }

    private void startConfiguredHttpServices() {
        this.config.each { String key, Object value ->
            if ( key.startsWith( "vertx_http_service" ) ) {
                startHttpService( key )
            }
        }
    }

    private void startHttpService( String key ) {
        // Hmm ... "vertx_http_service_${httpServiceName}" fails here! Null gets returned for a valid name!
        // Not even forcing a GString helps:
        // Integer port = this.configold["""vertx_http_service_${httpServiceName}"""] as Integer
        Integer port = this.config[ "${key}" ] as Integer

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
                httpServerRouterByPort[ port ] = router
                httpServer.requestHandler( router.&accept ).listen( port )
                this.logger.info( "HTTP server now listening on port ${port}!" )

                APSObjectPublisher<Router> routerPublisher = new APSObjectPublisher<Router>(
                        context: this.context,
                        consumerQuery: "(&(consumed=vertx)(${APSVertx.HTTP_SERVICE_NAME}=${key}))",
                        published: router
                )
                this.httpRouterPublishers.put( key, routerPublisher )
            }
        } else {
            this.logger.error( "No port for HTTP service '${key}'!" )
        }

    }

    private void stopConfiguredHttpServices() {
        this.config.each { String key, Object value ->
            if ( key.startsWith( "vertx_http_service" ) ) {
                stopHttpService( key )
            }
        }
    }

    private void stopHttpService( String key ) {
        Integer port = this.config[ "${key}" ] as Integer

        APSObjectPublisher<Router> routerPublisher = this.httpRouterPublishers.remove( key )
        routerPublisher.revoke()

        Router router = httpServerRouterByPort.remove( port )
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

//    /**
//     * For providing result back to called the same way as Vertx does.
//     */
//    @SuppressWarnings("PackageAccessibility")
//    private static class AsyncResultProvider implements AsyncResult<Vertx> {
//        @NotNull
//        Vertx vertx
//        boolean succeeded
//
//        @Override
//        Vertx result() {
//            return this.vertx
//        }
//
//        @Override
//        Throwable cause() {
//            return null
//        }
//
//        @Override
//        boolean succeeded() {
//            return this.succeeded
//        }
//
//        @Override
//        boolean failed() {
//            return !succeeded()
//        }
//    }
}
