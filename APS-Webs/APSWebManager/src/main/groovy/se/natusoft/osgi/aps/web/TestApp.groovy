package se.natusoft.osgi.aps.web

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import org.osgi.framework.ServiceReference
import se.natusoft.osgi.aps.activator.annotation.Initializer
import se.natusoft.osgi.aps.activator.annotation.Managed
import se.natusoft.osgi.aps.activator.annotation.OSGiService
import se.natusoft.osgi.aps.json.JSON
import se.natusoft.osgi.aps.tracker.APSServiceTracker
import se.natusoft.osgi.aps.util.APSLogger

// Instantiated, injected, etc by APSActivator. IDE can't see that.
@SuppressWarnings( "unused" )
@CompileStatic
@TypeChecked
class TestApp implements Constants {

    @Managed
    private APSLogger logger

    @OSGiService( additionalSearchCriteria = "(vertx-object=EventBus)" )
    private APSServiceTracker<EventBus> eventBusTracker

    @Initializer
    void init() {

        this.eventBusTracker.onActiveServiceAvailable = { EventBus eventBus, ServiceReference sref ->

            this.logger.info( "An EventBus just became available!" )

            eventBus.consumer( ADDR_NEW_CLIENT ) { Message message ->

                Map<String, Object> received = (message.body(  ) as JsonObject).getMap(  ) //JSON.stringToMap( message.body().toString() )

                this.logger.debug( "Received: ${ received }" )

                if ( received[ "op" ] == "init" ) {
                    provideGui( eventBus, received[ "client" ] as String )
                }

            }

            this.logger.debug( "Waiting for messages ..." )
        }

        this.eventBusTracker.onActiveServiceLeaving = { ServiceReference sref, Class api ->

            this.logger.info( "EventBus going away!" )
        }

    }

    private void provideGui( EventBus eventBus, String address ) {

        def testGui = [
                id      : "top",
                name    : "top",
                type    : "layout",
                layout  : "horizontal",
                children: [
                        [
                                id       : "name",
                                name     : "name-field",
                                group    : "gpoc",
                                type     : "textField",
                                width    : 20,
                                value    : "",
                                class    : "form-control",
                                listenTo : "aps:test-gui",
                                publishTo: "aps:test-gui",
                                headers  : [routing: "local"]
                        ],
                        [
                                id       : "description",
                                name     : "descriptionField",
                                group    : "gpoc",
                                type     : "textArea",
                                cols     : 30,
                                rows     : 4,
                                value    : "",
                                class    : "form-control",
                                listenTo : "aps:test-gui",
                                publishTo: "aps:test-gui",
                                headers  : [routing: "local"]
                        ],
                        [
                                id           : "submit",
                                name         : "submitButton",
                                group        : "gpoc",
                                type         : "button",
                                label        : "Save",
                                class        : "btn btn-success",
                                disabled     : true,
                                collectGroups: "gpoc",
                                enabled      : "groupNotEmpty:gpoc",
                                listenTo     : "aps:test-gui",
                                publishTo    : "aps:test-gui",
                                headers      : [routing: "local,external"]
                        ]
                ]
        ]

        JsonObject reply = new JsonObject([ msgType: "gui", msgData: testGui ] as Map<String, Object> )

        eventBus.send( address, reply)

        this.logger.debug("Sent: ${reply } to ${address}")

    }
}
