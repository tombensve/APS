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
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.core.eventbus.MessageConsumer
import io.vertx.core.json.JsonObject
import org.osgi.framework.ServiceReference
import se.natusoft.osgi.aps.activator.annotation.Initializer
import se.natusoft.osgi.aps.activator.annotation.Managed
import se.natusoft.osgi.aps.activator.annotation.OSGiService
import se.natusoft.osgi.aps.net.vertx.util.RecursiveJsonObjectMap
import se.natusoft.osgi.aps.tracker.APSServiceTracker
import se.natusoft.osgi.aps.util.APSJson
import se.natusoft.osgi.aps.util.APSLogger

// Instantiated, injected, etc by APSActivator. IDE can't see that.
@SuppressWarnings( "unused" )
@CompileStatic
@TypeChecked
class GUIProvider {

    // In your web app copy of APSWebManager change this to your web app name.
    private final String APS_WEB_APP_NAME="aps-web-manager"

    @Managed
    private APSLogger logger

    @OSGiService( additionalSearchCriteria = "(vertx-object=EventBus)" )
    private APSServiceTracker<EventBus> eventBusTracker

    private MessageConsumer newClientConsumer

    @Initializer
    void init() {

        this.eventBusTracker.onActiveServiceAvailable = { EventBus eventBus, ServiceReference sref ->

            this.logger.info( "An EventBus just became available!" )

            this.newClientConsumer = eventBus.consumer( "aps:${APS_WEB_APP_NAME}:backend" ) { Message message ->

                Map<String, Object> received = new RecursiveJsonObjectMap(message.body(  ) as JsonObject)
                String recv = received.toString(  )
                this.logger.debug( ">>>>>Received from '${received["aps"]["origin"]}': ${ recv }" )

                try {
                    if ( received[ "aps" ][ "type" ] == "avail" ) {
                        provideGui( eventBus, received[ "aps" ][ "origin" ] as String )
                    }
                }
                catch ( Exception e ) {
                    this.logger.error( e.getMessage(), e )
                }

            }

//            this.logger.debug( "Waiting for messages ..." )
//
//            eventBus.consumer( "aps:${APS_WEB_APP_NAME}:backend:all" ) { Message message ->
//
//                Map<String, Object> testGuiMsg = ( message.body() as JsonObject ).getMap()
//                this.logger.debug( "aps:test-gui ==> ${ testGuiMsg }" )
//
//            }
        }

        this.eventBusTracker.onActiveServiceLeaving = { ServiceReference sref, Class api ->

            this.newClientConsumer.unregister()

            this.logger.info( "EventBus going away!" )
        }

    }

    private void provideGui( EventBus eventBus, String address ) {
        this.logger.debug("In provideGui!")

        InputStream jsonStream =
                new BufferedInputStream( this.class.classLoader.getResourceAsStream( "guijson/gui.json" ) )

        Map<String, Object> gui = null

        try {
            gui = APSJson.readObject( jsonStream )
        }
        finally {
            jsonStream.close()
        }

        this.logger.debug( "gui: ${ gui }" )

        JsonObject reply = new JsonObject(
                [
                        aps    : [
                                origin: "aps:aps-web-manager:backend",
                                app   : "aps-web-manager",
                                type  : "gui"
                        ],
                        content: gui
                ] as Map<String, Object>
        )

        println "Thread: ${ Thread.currentThread().getName() }"

        eventBus.send( address, reply )

        this.logger.debug( "Sent: ${ reply } to ${ address }" )

    }
}
