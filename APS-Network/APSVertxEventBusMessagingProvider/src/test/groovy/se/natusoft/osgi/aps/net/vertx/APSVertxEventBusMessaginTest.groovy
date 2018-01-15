package se.natusoft.osgi.aps.net.vertx

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.junit.Test
import se.natusoft.osgi.aps.api.messaging.APSMessageService
import se.natusoft.osgi.aps.api.messaging.APSPublisher
import se.natusoft.osgi.aps.api.reactive.APSValue
import se.natusoft.osgi.aps.api.util.APSProperties
import se.natusoft.osgi.aps.core.lib.StructMap
import se.natusoft.osgi.aps.test.tools.OSGIServiceTestTools
import se.natusoft.osgi.aps.tools.APSActivator
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.annotation.activator.Initializer
import se.natusoft.osgi.aps.tools.annotation.activator.Managed
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiService
import static se.natusoft.osgi.aps.api.util.APSProperties.*

@CompileStatic
@TypeChecked
class APSVertXEventBusMessagingTest extends OSGIServiceTestTools {

    static final String UNIQUE_MESSAGE = UUID.randomUUID().toString()

    static boolean messageReceived = false

    @Test
    void runTest() throws Exception {


        deploy 'msg-receiver' with new APSActivator() using '/se/natusoft/osgi/aps/net/vertx/MsgReceiver.class'

        deploy 'msg-sender' with new APSActivator() using '/se/natusoft/osgi/aps/net/vertx/MsgSender.class'

        deploy 'aps-vertx-provider' with new APSActivator() from 'se.natusoft.osgi.aps', 'aps-vertx-provider', '1.0.0'

        deploy 'aps-vertx-event-bus-messaging-provider' with new APSActivator() from 'APS-Network/APSVertxEventBusMessagingProvider/target/classes'

        // Wait for and then validate result. Even if in this case a cluster of one member is created, it takes
        // some time.
        try {
            int count = 0
            while ( !messageReceived ) {
                synchronized ( this ) {
                    wait( 200 )
                }
                ++count
                if ( count > 30 ) {
                    break
                } // If built on a really slow machine this might not be enough!
            }
            assert messageReceived
        }
        finally {
            shutdown()
            Thread.sleep( 500 ) // Give Vertx time to shut down.
        }
    }
}

@SuppressWarnings("GroovyUnusedDeclaration")
@CompileStatic
@TypeChecked
class MsgReceiver {

    @OSGiService(timeout = "15 sec", nonBlocking = true)
    private APSMessageService<Map<String, Object>> msgService

    @Managed(loggingFor = "msg-receiver")
    private APSLogger logger

    @Initializer
    void init() {
        this.msgService.subscribe(

                // This is a workaround to that in the [ : ] format, only [ "aps-msg-target": "testaddr" ] is legal.
                // The following are not legal: [ APSMessageService.TARGET: "testaddr" ] nor [ "${APSMessageService.TARGET}": "testaddr" ]
                props() + APSMessageService.TARGET >> "testaddr"

        ) { APSValue<Map<String, Object>> messageValue ->

            this.logger.info( "Received message!" )

            StructMap message = new StructMap( messageValue.value() )
            message.lookupObject( "meta.test-message" ).onTrue() { boolean tm ->
                if ( message[ 'id' ] == APSVertXEventBusMessagingTest.UNIQUE_MESSAGE ) {
                    this.logger.info( "Got '${message[ "id" ]}'!" )
                    APSVertXEventBusMessagingTest.messageReceived = true
                }
            }
        }
        this.logger.info( "Subscribed to 'testaddr'" )
    }
}


@SuppressWarnings("GroovyUnusedDeclaration")
@CompileStatic
@TypeChecked
class MsgSender {

    // This variant should result in a wait and then APSNoServiceAvailableException.
    //@OSGiService(timeout = "15 sec")
    // This manages since on nonBlocking = true, the call to msgService is cached by the proxy until
    // the service is available, and then executed.
    @OSGiService(timeout = "15 sec", nonBlocking = true)
    private APSMessageService<Map<String, Object>> msgService

    @Managed(loggingFor = "msg-sender")
    private APSLogger logger

    @Initializer
    void init() {
        // Note that we must do publish, not send here! send sends to one listening target member. If there
        // are more it does a round robin, but it only goes to one in each case. Publish however always goes
        // to everybody. Since the clustered version of vertx is used, this means that any cluster on the same
        // subnet will receive the message, including other builds running at the same time. This is why we are
        // sending an UUID and publish to everyone. The receiver will only react on the correct UUID and ignore
        // the rest.
        this.msgService.publisher( props() + APSMessageService.TARGET >> "testaddr" ) { APSPublisher<Map<String, Object>> publisher ->

            publisher.publish(
                    [
                            "meta": [
                                    "test-message": true
                            ],
                            "id"  : APSVertXEventBusMessagingTest.UNIQUE_MESSAGE
                    ] as Map<String, Object>
            )
            logger.info( "Published '${APSVertXEventBusMessagingTest.UNIQUE_MESSAGE}'!" )
        }
    }
}

