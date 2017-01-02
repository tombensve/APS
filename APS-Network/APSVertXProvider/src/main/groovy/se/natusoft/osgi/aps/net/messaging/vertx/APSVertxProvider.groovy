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
package se.natusoft.osgi.aps.net.messaging.vertx

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.groovy.core.Vertx
import org.osgi.framework.BundleContext
import se.natusoft.osgi.aps.constants.APS
import se.natusoft.osgi.aps.net.messaging.vertx.api.APSVertXService
import se.natusoft.osgi.aps.net.messaging.vertx.config.VertxConfig
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.annotation.activator.Managed
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiProperty
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiServiceProvider

/**
 * Implements APSVertXService.
 */
@SuppressWarnings( "GroovyUnusedDeclaration" ) // This is never referenced directly, only through APSMessageService API.
@OSGiServiceProvider(
        properties = [
                @OSGiProperty( name = APS.Service.Provider,        value = "aps-vertx-provider" ),
                @OSGiProperty( name = APS.Service.Category,        value = APS.Value.Service.Category.Network ),
                @OSGiProperty( name = APS.Service.Function,        value = APS.Value.Service.Function.Messaging )
        ]
)
@CompileStatic
@TypeChecked
class APSVertxProvider implements APSVertXService {

    //
    // Private Members
    //

    @Managed
    private BundleContext context

    @Managed( loggingFor = "aps-vertx-service" )
    private APSLogger logger

    private Map<String, Vertx> namedInstances = Collections.synchronizedMap( [ : ] )

    private Map<String, Integer> usageCount = [ : ]

    //
    // Methods
    //

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
    @Override
    void useGroovyVertX( String name, Handler<AsyncResult<Vertx>> result ) {
        Vertx vertx = this.namedInstances [ name ]

        if (vertx != null) {
            increaseUsageCount( name )
            result.handle( new AsyncResultProvider( vertx: vertx, succeeded: true ) )
        }
        else {
            createVertxInstance( name , result )
        }
    }

    /** After having called useGroovyVertX(...) in a bundle, call this when shutting down! */
    @Override
    void releaseGroovyVertX( String name ) {
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
