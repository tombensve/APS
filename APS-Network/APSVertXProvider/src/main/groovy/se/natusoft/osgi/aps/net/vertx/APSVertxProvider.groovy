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
import io.vertx.groovy.core.Vertx
import org.osgi.framework.ServiceReference
import se.natusoft.osgi.aps.api.reactive.ObjectConsumer
import se.natusoft.osgi.aps.constants.APS
import se.natusoft.osgi.aps.net.vertx.api.APSVertxService
import se.natusoft.osgi.aps.net.vertx.config.VertxConfig
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.APSServiceTracker
import se.natusoft.osgi.aps.tools.annotation.activator.*

import java.util.concurrent.TimeUnit

/**
 * Implements APSVertXService and also calls all DataConsumer<Vertx> services found with an Vertx instance.
 *
 * Do consider using the DataConsumer.DataConsumerProvider utility implementation for callback delivery of Vertx.
 */
@SuppressWarnings(["GroovyUnusedDeclaration", "PackageAccessibility"])
@OSGiServiceProvider(
        properties = [
                @OSGiProperty( name = APS.Service.Provider,        value = "aps-vertx-provider" ),
                @OSGiProperty( name = APS.Service.Category,        value = APS.Value.Service.Category.Network ),
                @OSGiProperty( name = APS.Service.Function,        value = APS.Value.Service.Function.Messaging )
        ]
)
@CompileStatic
@TypeChecked
class APSVertxProvider implements APSVertxService {

    //
    // Private Members
    //

    /** The logger for this service. */
    @Managed( loggingFor = "aps-vertx-service" )
    private APSLogger logger

    /**
     * This tracks DataConsumer<Vertx> OSGi services which will be called with a Vertx instance to use.
     * This is a reversed reactive API variant. Also called the Hollywood principle: Don't call us, we
     * call you!
     */
    @OSGiService(additionalSearchCriteria = "(consumed=vertx)")
    private APSServiceTracker<ObjectConsumer<Vertx>> apsVertxConsumers

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
    private Map<String, Vertx> namedInstances = Collections.synchronizedMap( [ : ] )

    /**
     * This keeps track of how many are using a specific instance of Vertx. useGroovyVertx(...) will increase
     * the count, and releaseGroovyVertx(...) will decrease the count. If the count reaches 0 the Vertx instance
     * will be shut down.
     */
    private Map<String, Integer> usageCount = [ : ]

    /**
     * This tracks DataConsumer<Vertx> services and saves them using the ServiceReference of the service.
     * The service will only be called back with the DataConsumer<Vertx> once when the service reference is
     * not in this map. If the service itself does release() on the DataConsumer<Vertx> then the service
     * will be removed from this map and the next run if the service is still upp and running will call
     * the service again and add the service reference to this map. If the service itself calls release()
     * it usually means that the service is going down.
     */
    private Map<ServiceReference, ObjectConsumer.ObjectHolder<Vertx>> callbackInstances = Collections.synchronizedMap( [: ] )

    /**
     * This does the reverse of APSVertxService: It listens to InstanceConsumer services and calls them with
     * Vertx instance when available.
     *
     * If anything else than the default Vertx instance is wanted a Properties object containing named-instance: "name"
     * should be provided.
     */
    @SuppressWarnings("PackageAccessibility")
    @Schedule(delay = 0L, repeat = 10L, timeUnit = TimeUnit.SECONDS)
    private Runnable updateVertxConsumers = {
        Map<ServiceReference, ServiceReference> currentServices = [ : ]

        this.apsVertxConsumers.onServiceAvailable { ObjectConsumer<Vertx> dataConsumer, ServiceReference serviceReference ->
            currentServices.put serviceReference, serviceReference

            String name = DEFAULT_INST
            if (dataConsumer.options() != null && dataConsumer.options().containsKey("named-instance")) {
                name = dataConsumer.options() [ "named-instance" ]
            }

            // Check for new service
            if ( !this.callbackInstances.containsKey(serviceReference) ) {

                useGroovyVertX(name, { AsyncResult<Vertx> result ->
                    if (result.succeeded()) {

                        ObjectConsumer.ObjectHolder<Vertx> vertxProvider = new ObjectConsumer.ObjectHolder.ObjectHolderProvider<Vertx>(result.result()) {
                            @Override
                            void release() {
                                callbackInstances.remove(serviceReference)
                                releaseGroovyVertX(name)
                                logger.info("Released '${name}'!")
                            }
                        }

                        this.callbackInstances.put( serviceReference , vertxProvider )

                        dataConsumer.onObjectAvailable(vertxProvider)
                    } else {
                        dataConsumer.onObjectUnavailable()
                    }
                })
            }
        }
    }

    //
    // Methods
    //

    /**
     * Release Vertx for any existing callback instances.
     */
    @BundleStop
    void shutdown() {
        this.callbackInstances.keySet().each { ServiceReference sr ->
            this.callbackInstances.get(sr).release()
        }
    }

    /**
     * Loads config options into a Vertx options Map.
     *
     * @param options The map to load options into.
     */
    private static void loadOptions(Map<String, Object> options) {
        VertxConfig.managed.get().optionsValues.each { VertxConfig.VertxConfigValue  entry ->
            Object value = ""

            // The type value will *always* contain one of these values and nothing else.
            switch ( entry.type.string ) {
                case "String":
                    value = entry.value.string
                    break

                case "Int":
                    value = Integer.valueOf( entry.value.string )
                    break

                case "Float":
                    value = Float.valueOf ( entry.value.string )
                    break

                case "Boolean":
                    value = Boolean.valueOf( entry.value.string )
            }

            options [ entry.name.string ] = value
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

        Map<String, Object> options = [ : ]
        loadOptions(options)

        Vertx.clusteredVertx( options, { AsyncResult< Vertx > res ->
            if ( res.succeeded() ) {
                this.logger.info "Vert.x cluster started successfully!"

                Vertx vertx = res.result()
                this.namedInstances.put( name, vertx )
                increaseUsageCount( name )
                result.handle( res )
            }
            else {
                this.logger.error "Vert.x cluster failed to start: ${res.cause()}, for '${name}'!"
                result.handle( res )
            }
        })
    }

    /**
     * Returns The Groovy Vert.x instance for the specified name.
     *
     * @param name The name of the instance to get.
     */
    @SuppressWarnings("PackageAccessibility")
    @Override
    void useGroovyVertX(String name, Handler<AsyncResult<Vertx>> result ) {
        Vertx vertx = this.namedInstances [ name ]

        if (vertx != null) {
            increaseUsageCount( name )
            result.handle( new AsyncResultProvider( vertx: vertx, succeeded: true ) )
        }
        else {
            createVertxInstance( name , result )
        }
    }

    /**
     * After having called useGroovyVertX(...) in a bundle, call this when shutting down!
     */
    @SuppressWarnings("PackageAccessibility")
    @Override
    void releaseGroovyVertX(String name ) {
        if (decreaseUsageCount( name ) == 0) {
            Vertx vertx = this.namedInstances.remove( name )
            this.usageCount.remove( name )

            vertx.close { AsyncResult res ->

                if (res.succeeded()) {
                    this.logger.info "Vert.x successfully shut down!"
                } else {
                    this.logger.error "Vert.x failed to shut down! [${res.cause()}]"
                }
            }
        }
    }

    private void increaseUsageCount( String name ) {
        if ( this.usageCount [ name ] == null ) {
            this.usageCount [ name ] = 1
        }
        else {
            this.usageCount [ name ] = this.usageCount [ name ] + 1
        }
    }

    private int decreaseUsageCount( String name ) {
        int result = 0

        if ( this.usageCount [ name ] != null) {
            this.usageCount [ name ] = this.usageCount [name ] - 1
            result = this.usageCount [ name ]
        }

        return result
    }

    /**
     * For providing result back to called the same way as Vertx does.
     */
    @SuppressWarnings("PackageAccessibility")
    private static class AsyncResultProvider implements AsyncResult< Vertx > {
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
