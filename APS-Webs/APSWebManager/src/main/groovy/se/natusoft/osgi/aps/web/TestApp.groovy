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
class TestApp {

    @Managed
    private APSLogger logger

    @OSGiService( additionalSearchCriteria = "(vertx-object=EventBus)" )
    private APSServiceTracker<EventBus> eventBusTracker

    private MessageConsumer newClientConsumer

    @Initializer
    void init() {

        this.eventBusTracker.onActiveServiceAvailable = { EventBus eventBus, ServiceReference sref ->

            this.logger.info( "An EventBus just became available!" )

            this.newClientConsumer = eventBus.consumer( "aps:aps-web-manager:backend" ) { Message message ->

                // Since JsonObject from Vert.x only maps the current values to a Map, not the whole tree
                // I'm currently producing a JSON string from JsonObject and then parse it again with
                // Jackson Jr, which gives a full Map structure.
                //Map<String, Object> received = APSJson.readObject( message.body(  ).toString(  ) )
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

            this.logger.debug( "Waiting for messages ..." )

            eventBus.consumer( "aps:aps-web-manager:backend:all" ) { Message message ->

                Map<String, Object> testGuiMsg = ( message.body() as JsonObject ).getMap()
                this.logger.debug( "aps:test-gui ==> ${ testGuiMsg }" )

            }
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
