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
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.eventbus.EventBus
import io.vertx.core.http.HttpServer
import io.vertx.core.shareddata.SharedData
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.sockjs.SockJSHandler
import org.osgi.framework.BundleContext
import org.osgi.framework.ServiceRegistration
import se.natusoft.osgi.aps.activator.annotation.ConfigListener
import se.natusoft.osgi.aps.api.core.config.APSConfig
import se.natusoft.osgi.aps.exceptions.APSException
import se.natusoft.osgi.aps.exceptions.APSStartException
import se.natusoft.osgi.aps.util.APSLogger
import se.natusoft.osgi.aps.activator.annotation.BundleStop
import se.natusoft.osgi.aps.activator.annotation.Initializer
import se.natusoft.osgi.aps.activator.annotation.Managed

/**
 * Provides Vertx by publishing Vertx and other Vertx related objects some by configuration as OSGi services.
 */
@SuppressWarnings( [ "GroovyUnusedDeclaration", "PackageAccessibility" ] )
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
    @Managed( loggingFor = "aps-vertx-provider" )
    private APSLogger logger

    /** The vertx instance. */
    private Vertx vertx

    /** The vertx service registration. */
    private ServiceRegistration vertxSvcReg

    /** Service registration for event bus. */
    private ServiceRegistration eventBusSvcReg

    /** Service registration for shared data. */
    private ServiceRegistration shareDataSvcReg

    /** A map of HTTP servers per service port. These are internal to this bundle. */
    private Map<Integer, HttpServer> httpServerByPort = [:]

    /** A map of Routers for HTTP servers per service port. These are provided to those that wants to serve a path. */
    private Map<Integer, Router> httpServerRouterByPort = [:]

    /** Service registrations for routers. */
    private Map<Integer, ServiceRegistration> routerRegByPort = [:]

    /** */
    private List<Runnable> shutdownNotification = []

    private APSConfig config

    /**
     * This gets called after all injections are done.
     */
    @Initializer
    void init() {
        this.logger.connectToLogService( this.context )
    }

    @ConfigListener( apsConfigId = "apsVertxProvider" )
    void configReceiver( APSConfig config ) {
        this.config = config

        this.logger.info( "#### Got config! : ${ config }" )

        // We wait for config before doing this since it will use config.
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
     * This starts vertx either clustered or not depending on the 'aps.vertx.clustered' system property.
     *
     * __Do note__ that a clustered Vert.x is basically required for APS. APSConfigManager depends on it.
     * The reason for making it possible to start an unclustered Vert.x instance is for tests. You don't
     * want multiple parallel tests in a Jenkins for example to cluster with each other which by default
     * Vert.x / Hazelcast setup, they will do.
     *
     * So what happens with APSConfigManager when not clustered ? Well nothing for tests if they are
     * correctly setup. Tests provide (or rather override) the default configuration values which are
     * used by APSConfigManager when it cannot get the cluster config, nor a local filesystem config.
     *
     * @param options Vert.x options passed to Vertx on creation. can be [ : ]!
     * @param handler The handler to call with result.
     */
    private static void vertxBoot( Map<String, Object> options, Handler<AsyncResult<Vertx>> handler ) {

        boolean apsVertxClustered = true

        String clusterProp = System.getProperty( "aps.vertx.clustered" )

        if ( clusterProp != null && clusterProp.trim().toLowerCase() == "false" ) {

            apsVertxClustered = false
        }

        if ( apsVertxClustered ) {

            // If the arguments are error marked, then you are using IDEA. The only error here is in IDEA.
            Vertx.clusteredVertx( options, handler )
        }
        else {
            // On a non clustered Vertx it just returns the Vertx instance directly, not requiring a handler.
            // But since the caller of this method don't know or care if Vertx is clustered or not it expects
            // a handler callback in either case, so we have to call the handler instead of Vertx.

            // If the arguments are error marked, then you are using IDEA. The only error here is in IDEA.
            Vertx vertx = Vertx.vertx( options )

            handler.handle(
                    new AsyncResult<Vertx>() {

                        Vertx result() {

                            vertx
                        }

                        Throwable cause() {

                            failed() ? new APSException( "Failed to create Vertx instance!" ) : null
                        }

                        boolean succeeded() {

                            vertx != null
                        }

                        boolean failed() {

                            vertx == null
                        }
                    }
            )
        }
    }

    /**
     * Starts the main Vertx service which in turn will start other Vertx services when up.
     *
     * Currently the following are registered as OSGi services in addition to the Vertx instance:
     *
     * - EventBus
     * - SharedData
     *
     * Also note that startHttpService(...) will start and publish HTTP routers depending on configuration.
     */
    private void startVertx() {

        vertxBoot( [:] ) { AsyncResult<Vertx> res ->

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

                this.shareDataSvcReg = this.context.registerService( SharedData.class.name, this.vertx.sharedData(), [
                        "service-provider": "aps-vertx-provider",
                        "service-category": "network",
                        "service-function": "storage",
                        "vertx-object"    : "SharedData"
                ] as Properties )

                startVertxServices()

            }
            else {

                logger.error "Vert.x cluster failed to start!", res.cause()
                throw new APSStartException( "Vert.x cluster failed to start!", res.cause() )
            }
        }
    }

    /**
     * Stops the main Vertx service which first will stop other vertx services.
     */
    private void stopVertx() {

        if ( this.vertxSvcReg != null ) {

            this.vertxSvcReg.unregister()
            this.logger.info( "Unregistered Vertx as OSGi service!" )
        }

        if ( this.eventBusSvcReg != null ) {

            this.eventBusSvcReg.unregister()
            this.logger.info( "Unregistered EventBus as OSGi service!" )
        }

        if ( this.shareDataSvcReg != null ) {

            this.shareDataSvcReg.unregister()
            this.logger.info( "Unregistered SharedData as OSGi service!" )
        }
        stopVertxServices()

        this.vertx.close() { AsyncResult<Vertx> res ->
            if ( res.succeeded() ) {

                logger.info "Vert.x cluster stopped successfully!"
            }
            else {

                logger.error "Vert.x cluster failed to shutdown!: ${ res.cause() }!"
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

        List<Map<String, Object>> httpConfs = ( List<Map<String, Object>> ) this.config[ "http" ]

        httpConfs?.each { Map<String, Object> http ->

            this.logger.debug("#### http: ${http}")

            def name = http[ "name" ] as String
            def port = http[ "port" ] as Integer
            def eventBusBridge = http[ "eventBusBridge" ] as Map<String, Object>

            this.logger.debug( ">>>> ${name} / ${port}: eventBusBridge: ${eventBusBridge}" )

            startHttpService( name, port, eventBusBridge )
        }
    }

    /**
     * Starts a specific Http servcie and router.
     *
     * @param name The http config name for refrerence in logs.
     * @param port The port the server should listen to.
     * @param eventBusBridge A JSON object containing eventbus bridge info.
     */
    private void startHttpService( String name, int port, Map<String, Object> eventBusBridge ) {

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
                this.logger.info( "Created router for config '${ name }'!" )
                httpServerRouterByPort[ port ] = router

                httpServer.requestHandler( router.&accept ).listen( port )
                this.logger.info( "HTTP server for config '${ name }' now listening on port ${ port }!" )

                this.routerRegByPort[ port ] = this.context.registerService( Router.class.name, router, [
                        "service-provider": "aps-vertx-provider",
                        "service-category": "network",
                        "service-function": "client/server",
                        "vertx-object"    : "Router",
                        "vertx-router"    : name
                ] as Properties )

                this.logger.info( "Registered HTTP service 'Router' for config '${ name }' as OSGi service!" )

                if ( eventBusBridge != null && eventBusBridge[ "enabled" ] != null) {
                    startEventBusBridge( router, port, eventBusBridge )
                }
                else {
                    this.logger.info("No eventbus bridge for this service!")
                }
            }
        }
        else {

            this.logger.error( "No port for HTTP service '${ name }'!" )
        }

    }

    private void startEventBusBridge( Router router, int port,  Map<String, Object> eventBusBridge ) {

        def sockJSHandler = SockJSHandler.create( this.vertx )

        def inboundPermitteds = []
        def outboundPermitteds = []

        if ( eventBusBridge[ "allowEventAddresses" ] != null ) {
            ( eventBusBridge[ "allowEventAddresses" ] as String ).split( "," ).each { String addr ->
                if ( addr.startsWith( "in:" ) ) {
                    inboundPermitteds << [address: addr.substring( 3 )]
                }
                else if ( addr.startsWith( "out:" ) ) {
                    outboundPermitteds << [address: addr.substring( 4 )]
                }
                else {
                    inboundPermitteds << [address: addr]
                    outboundPermitteds << [address: addr]
                }
            }
        }
        if ( eventBusBridge[ "allowEventAddressesRegex" ] != null ) {
            ( eventBusBridge[ "allowEventAddressesRegex" ] as String ).split( "," ).each { String addr ->
                if ( addr.startsWith( "in:" ) ) {
                    inboundPermitteds << [addressRegex: addr.substring( 3 )]
                }
                else if ( addr.startsWith( "out:" ) ) {
                    outboundPermitteds << [addressRegex: addr.substring( 4 )]
                }
                else {
                    inboundPermitteds << [addressRegex: addr]
                    outboundPermitteds << [addressRegex: addr]
                }
            }
        }
        if ( eventBusBridge[ "allowEventAddressMatchIn" ] != null ) {
            inboundPermitteds << [match: eventBusBridge[ "allowEventAddressMatchIn" ]]
        }
        if ( eventBusBridge[ "allowEventAddressMatchOut" ] != null ) {
            outboundPermitteds << [match: eventBusBridge[ "allowEventAddressMatchOut" ]]
        }

        // Currently no more detailed permissions than on target address. Might add limits on message contents
        // later.
        def options = [
                inboundPermitteds : inboundPermitteds,
                outboundPermitteds: outboundPermitteds
        ] as Map<String, Object>

        sockJSHandler.bridge( options )

        router.route( "/eventbus/*" ).handler( sockJSHandler )

        this.logger.info("Starded event bus bridge for port: ${port}")
    }

    /**
     * Stops all Http services and their routers.
     */
    private void stopHttpServices() {

        List<Map<String, Object>> httpConfs = ( List<Map<String, Object>> ) this.config[ "http" ]

        httpConfs?.each { Map<String, Object> http ->

            String name = http[ "name" ] as String
            Integer port = http[ "port" ] as Integer
            boolean eventBus = http[ "eventBusBridge" ] as boolean

            stopHttpService( name, port )
        }
    }

    /**
     * Stops a Http service and its router.
     *
     * @param name A key in configuration providing the service port. This is used to find locally cached service.
     */
    private void stopHttpService( String name, int port ) {

        if ( this.routerRegByPort != null ) {

            this.routerRegByPort.remove( port )?.unregister()
            this.logger.info( "Unregistered 'Router' for config '${ name }' as OSGi service!" )
        }

        this.httpServerRouterByPort?.remove( port )?.delete()


        this.httpServerByPort?.remove( port )?.close() { AsyncResult<Vertx> res ->

            if ( res.succeeded() ) {

                this.logger.info( "Http service '${ name }' successfully stopped!" )
            }
            else {

                this.logger.error( "Stopping http service '${ name }' failed!", res.cause() )
            }
        }
    }

}
