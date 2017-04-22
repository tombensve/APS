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
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServer
import io.vertx.ext.web.Router
import org.osgi.framework.BundleContext
import org.osgi.framework.ServiceReference
import se.natusoft.docutations.NotUsed
import se.natusoft.osgi.aps.constants.APS
import se.natusoft.osgi.aps.net.vertx.api.APSVertxService
import se.natusoft.osgi.aps.net.vertx.config.VertxConfig
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.APSServiceTracker
import se.natusoft.osgi.aps.tools.annotation.activator.*
import se.natusoft.osgi.aps.tools.reactive.Consumer

import java.util.concurrent.ExecutorService

// TODO: Make clustering optional.
/**
 * Implements APSVertXService and also calls all DataConsumer<Vertx> services found with an Vertx instance.
 *
 * Do consider using the DataConsumer.DataConsumerProvider utility implementation for callback delivery of Vertx.
 */
@SuppressWarnings(["GroovyUnusedDeclaration", "PackageAccessibility"])
@OSGiServiceProvider(
        properties = [
                @OSGiProperty(name = APS.Service.Provider, value = "aps-vertx-provider"),
                @OSGiProperty(name = APS.Service.Category, value = APS.Value.Service.Category.Network),
                @OSGiProperty(name = APS.Service.Function, value = APS.Value.Service.Function.Messaging)
        ]
)
@CompileStatic
@TypeChecked
class APSVertxProvider implements APSVertxService {

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

    /** For sequential one threaded executions. */
    @Managed
    @ExecutorSvc(type = ExecutorSvc.ExecutorType.Single, unConfigurable = true)
    private ExecutorService sequentialExecutor

    /**
     * This tracks DataConsumer<Vertx> OSGi services which will be called with a Vertx instance to use.
     * This is a reversed reactive API variant. Also called the Hollywood principle: Don't call us, we
     * call you!
     */
    @OSGiService(additionalSearchCriteria = "(consumed=vertx)")
    private APSServiceTracker<Consumer<Vertx>> apsVertxConsumers

    /**
     * This associates a name with each Vertx instance. This is to allow multiple clients to share the same
     * Vertx instance with the default name being "default". But if some bundle want to use an app/service/bundle
     * specific instance of Vertx then they can provide a unique name to get their own instance.
     *
     * __DO NOTE:__ Vertx can handle multiple servers listening on the same port, but multiple Vertx instances is a
     * different thing and will most probably conflict when services are bound to same hosts and ports as a different
     * Vertx instance on the same host. But for different services on different ports it should be OK, and thereby
     * multiple Vertx instances are allowed by this service.
     */
    private Map<String, NamedVertxInstance> namedInstances = Collections.synchronizedMap([:])

    /** A map of HTTP servers per service port. These are internal to this bundle. */
    private Map<Integer, HttpServer> httpServerByPort = Collections.synchronizedMap([:])

    /** A map of Routers for HTTP servers per service port. These are provided to those that wants to serve a path. */
    private Map<Integer, Router> httpServerRouterByPort = Collections.synchronizedMap([:])

    /**
     * This keeps track of how many are using a specific instance of Vertx. useGroovyVertx(...) will increase
     * the count, and releaseGroovyVertx(...) will decrease the count. If the count reaches 0 the Vertx instance
     * will be shut down.
     */
    private Map<String, Integer> usageCount = [:]

    /**
     * This tracks DataConsumer<Vertx> services and saves them using the ServiceReference of the service.
     * The service will only be called back with the DataConsumer<Vertx> once when the service reference is
     * not in this map. If the service itself does release() on the DataConsumer<Vertx> then the service
     * will be removed from this map and the next run if the service is still upp and running will call
     * the service again and add the service reference to this map. If the service itself calls release()
     * it usually means that the service is going down.
     */
    private Map<ServiceReference, Consumer.Consumed<Vertx>> callbackInstances = Collections.synchronizedMap([:])

    /**
     * This is used when a Consumer.Consumed<Vertx> service is leaving to get the name of the service
     * for calling releaseGroovyVertx(name) with.
     */
    private Map<ServiceReference, String> svcRefNamedInst = Collections.synchronizedMap([:])

    /**
     * Temporary config handling until the APS config overhaul.
     */
    private Map<String, Object> config = [
            vertx_http_service_default: 9088,
            "vertx_http_service_aps-admin-web-a2": 9080,
            vertx_http_service_test: 8888
    ] as Map<String, Object>

    @Initializer
    void init() {
        this.logger.connectToLogService(this.context)

        this.apsVertxConsumers.onServiceAvailable this.&onServiceAvailableHandler

        this.apsVertxConsumers.onServiceLeaving this.&onServiceLeavingHandler
    }

    //
    // Methods
    //

    /**
     * This gets called when there is a new VertX consumer available.
     *
     * @param dataConsumer The newly available consumer.
     * @param serviceReference The service reference of the consumer. Used as an id.
     */
    private void onServiceAvailableHandler(Consumer<Vertx> dataConsumer, ServiceReference serviceReference) {

        String name = DEFAULT_INST
        if (serviceReference.getProperty(NAMED_INSTANCE) != null) {
            name = serviceReference.getProperty(NAMED_INSTANCE)
        }

        svcRefNamedInst[serviceReference] = name

        // Check for new service
        if (!callbackInstances.containsKey(serviceReference)) {

            useGroovyVertX(name) { AsyncResult<Vertx> result ->
                if (result.succeeded()) {
                    Vertx vertx = result.result()

                    Consumer.Consumed<Vertx> vertxProvider =
                            new Consumer.Consumed.ConsumedProvider<Vertx>(vertx) {
                                @Override
                                void release() {
                                    callbackInstances.remove(serviceReference)
                                    svcRefNamedInst.remove(serviceReference)
                                    releaseGroovyVertX(name)
                                    logger.info("Released '${name}'!")
                                }
                            }
                    callbackInstances.put(serviceReference, vertxProvider)

                    dataConsumer.consume(Consumer.Status.AVAILABLE, vertxProvider)

                    // if the consumer is also consuming an HTTP service router then pass that on to the consumer.
                    String httpServiceName = serviceReference.getProperty(HTTP_SERVICE_NAME)
                    if (httpServiceName != null) {
                        // Hmm ... "vertx_http_service_${httpServiceName}" fails here! Null gets returned for a valid name!
                        // Not even forcing a GString helps:
                        // Integer port = this.config["""vertx_http_service_${httpServiceName}"""] as Integer
                        Integer port = this.config["vertx_http_service_" + httpServiceName] as Integer

                        if (port != null) {
                            // We keep a server for each listened to port.
                            HttpServer httpServer = httpServerByPort[port]
                            if (httpServer == null) {
                                httpServer = vertx.createHttpServer(/* TODO: Provide options. */)
                                httpServerByPort[port] = httpServer
                            }

                            // Consumers don't get direct access to the HttpServer, only to its Router.
                            Router router = httpServerRouterByPort[port]
                            if (router == null) {
                                router = Router.router(vertx)
                                httpServerRouterByPort[port] = router
                                httpServer.requestHandler(router.&accept).listen(port)
                                this.logger.info("HTTP server now listening on port ${port}!")
                            }

                            (dataConsumer as Consumer<Router>).
                                    consume(Consumer.Status.AVAILABLE, new Consumer.Consumed.ConsumedProvider<Router>(router))
                        }
                        else {
                            this.logger.error("No port for HTTP service '${httpServiceName}'!")
                        }
                    }
                    else {
                        this.logger.error("Tried to use non configured HTTP service!")
                    }

                } else { // Failure
                    dataConsumer.consume(Consumer.Status.UNAVAILABLE, null)
                }
            }
        }
    }

    /**
     * This gets called when a Vertx consumer is leaving.
     *
     * @param serviceReference The service reference of the leaving consumer. Used as an id.
     * @param serviceAPI The class of the leaving service. Not used.
     */
    private synchronized void onServiceLeavingHandler(ServiceReference serviceReference, @NotUsed Class serviceAPI) {
        callbackInstances.remove(serviceReference)
        String name = svcRefNamedInst.remove(serviceReference)
        releaseGroovyVertX(name)
    }

    /**
     * Release Vertx for any existing callback instances.
     */
    @BundleStop
    synchronized void shutdown() {
        this.callbackInstances.each { ServiceReference sr, Consumer.Consumed<Vertx> vertx ->
            vertx.release()
        }
        this.logger.disconnectFromLogService(this.context)
    }

    /**
     * Loads config options into a Vertx options Map.
     *
     * @param options The map to load options into.
     */
    private static void loadOptions(Map<String, Object> options) {
        VertxConfig.managed.get().optionsValues.each { VertxConfig.VertxConfigValue entry ->
            Object value = ""

            // The type value will *always* contain one of these values and nothing else.
            switch (entry.type.string) {
                case "String":
                    value = entry.value.string
                    break

                case "Int":
                    value = Integer.valueOf(entry.value.string)
                    break

                case "Float":
                    value = Float.valueOf(entry.value.string)
                    break

                case "Boolean":
                    value = Boolean.valueOf(entry.value.string)
            }

            options[entry.name.string] = value
        }
    }

    /**
     * Creates a new Vertx instance.
     *
     * @param name The name to save this instance as.
     * @param result The handler to forward result to.
     */
    @SuppressWarnings("PackageAccessibility")
    private void createVertxInstance(String name, Handler<AsyncResult<Vertx>> result) {
        Map<String, Object> options = [:]
        loadOptions(options)

        // We have to supply a context classloader for Vertx to init correctly.
        ClassLoader origClassLoader = Thread.currentThread().contextClassLoader
        Thread.currentThread().setContextClassLoader(this.class.classLoader)
        boolean failed = false

        Vertx.clusteredVertx(options) { AsyncResult<Vertx> res ->
            if (res.succeeded()) {
                logger.info "Vert.x cluster started successfully!"
                Vertx vertx = res.result()
                NamedVertxInstance nvi = new NamedVertxInstance(vertx: vertx, vertxContext: Vertx.currentContext())
                namedInstances[name] = nvi
                increaseUsageCount(name)
            } else {
                failed = true
                logger.error "Vert.x cluster failed to start: ${res.cause()}, for '${name}'!"
            }
            result.handle(res)
        }

        Thread.currentThread().setContextClassLoader(origClassLoader)

        // This is called from useGroovyVertX(...) which is run in a single thread by an ExecutionService.
        // We have to wait for a result before returning the thread.
        while (this.namedInstances[name] == null && !failed) {
            Thread.sleep(500)
        }
    }

    /**
     * Returns The Groovy Vert.x instance for the specified name.
     *
     * @param name The name of the instance to get.
     */
    @SuppressWarnings("PackageAccessibility")
    @Override
    void useGroovyVertX(String name, Handler<AsyncResult<Vertx>> result) {
        // We need to do each of these sequentially on one thread otherwise we will create multiple Vertx
        // instances in parallel.
        this.sequentialExecutor.submit {
            NamedVertxInstance nvi = this.namedInstances[name]

            if (nvi != null) {
                increaseUsageCount(name)

                nvi.vertxContext.runOnContext {
                    result.handle new AsyncResultProvider(vertx: nvi.vertx, succeeded: true)
                }

            } else {
                createVertxInstance(name, result)
            }
        }
    }

    /**
     * After having called useGroovyVertX(...) in a bundle, call this when shutting down!
     */
    @SuppressWarnings("PackageAccessibility")
    @Override
    void releaseGroovyVertX(String name) {
        this.sequentialExecutor.submit {
            if (decreaseUsageCount(name) == 0 && this.namedInstances.containsKey(name)) {
                NamedVertxInstance nvi = this.namedInstances.remove name
                this.usageCount.remove name

                this.apsVertxConsumers.onServiceAvailable { Consumer<Vertx> dataConsumer, ServiceReference serviceReference ->
                    try {
                        dataConsumer.consume(Consumer.Status.REVOKED, null)
                    }
                    catch (Exception e) {
                        this.logger.error("Vertx delivery to consumer failed!", e)
                    }
                }

                nvi.vertx.close { AsyncResult res ->

                    if (res.succeeded()) {
                        this.logger.info "Vert.x successfully shut down!"
                    } else {
                        this.logger.error "Vert.x failed to shut down! [${res.cause()}]"
                    }
                }
            }
        }
    }

    private void increaseUsageCount(String name) {
        if (this.usageCount[name] == null) {
            this.usageCount[name] = 1
        } else {
            this.usageCount[name] = this.usageCount[name] + 1
        }
    }

    private int decreaseUsageCount(String name) {
        int result = 0

        if (this.usageCount[name] != null) {
            this.usageCount[name] = this.usageCount[name] - 1
            result = this.usageCount[name]
        }

        return result
    }

    /**
     * For providing result back to called the same way as Vertx does.
     */
    @SuppressWarnings("PackageAccessibility")
    private static class AsyncResultProvider implements AsyncResult<Vertx> {
        Vertx vertx
        boolean succeeded

        @Override
        Vertx result() {
            return this.vertx
        }

        @Override
        Throwable cause() {
            return null
        }

        @Override
        boolean succeeded() {
            return this.succeeded
        }

        @Override
        boolean failed() {
            return !succeeded()
        }
    }
}
