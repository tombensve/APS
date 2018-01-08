package se.natusoft.osgi.aps.net.vertx

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.junit.Test
import se.natusoft.osgi.aps.api.pubsub.APSPubSubService
import se.natusoft.osgi.aps.api.reactive.APSAsyncValue
import se.natusoft.osgi.aps.test.tools.OSGIServiceTestTools
import se.natusoft.osgi.aps.tools.APSActivator
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.annotation.activator.Initializer
import se.natusoft.osgi.aps.tools.annotation.activator.Managed
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiService

@CompileStatic
@TypeChecked
class APSVertXEventBusMessagingTest extends OSGIServiceTestTools {

    static final String GOAT = "baeaeae"

    static boolean messageReceived = false

    @Test
    void runTest() throws Exception {


        deploy 'aps-vertx-event-bus-messaging-provider' with new APSActivator() from 'APS-Network/APSVertxEventBusMessagingProvider/target/classes'

        deploy 'msg-receiver' with new APSActivator() using '/se/natusoft/osgi/aps/net/vertx/MsgReceiver.class'

        deploy 'msg-sender' with new APSActivator() using '/se/natusoft/osgi/aps/net/vertx/MsgSender.class'

        deploy 'aps-vertx-provider' with new APSActivator() from 'se.natusoft.osgi.aps', 'aps-vertx-provider', '1.0.0'

        // Wait for and then validate result. Even if in this case a cluster of one member is created, it takes
        // some time.
        try {
            int count = 0
            while ( !messageReceived ) {
                synchronized ( this ) {
                    wait( 200 )
                }
                ++count
                if ( count > 30 ) break // If build on a really slow machine this might not be enough!
            }
            assert messageReceived
        }
        finally {
            shutdown()
            Thread.sleep(500) // Give Vertx time to shut down.
        }
    }

}

@SuppressWarnings("GroovyUnusedDeclaration")
@CompileStatic
@TypeChecked
class MsgReceiver {

    @OSGiService( timeout = "15 sec", nonBlocking = true )
    private APSPubSubService<Map<String, Object>> msgService

    @Managed( loggingFor = "msg-receiver" )
    private APSLogger logger

    @Initializer
    void init() {
        this.msgService.subscribe( [ "address": "testaddr" ] ) { APSAsyncValue<Map<String, Object>> message ->
            this.logger.info("Received message!")
            if (message.value(  )["goat"] == APSVertXEventBusMessagingTest.GOAT) {
                this.logger.info("Got '${APSVertXEventBusMessagingTest.GOAT}' from goat!")
                APSVertXEventBusMessagingTest.messageReceived = true
            }
        }
        this.logger.info("Subscribed to 'testaddr'")
    }
}


@SuppressWarnings("GroovyUnusedDeclaration")
@CompileStatic
@TypeChecked
class MsgSender {

    @OSGiService( timeout = "15 sec" )
    private APSPubSubService<Map<String, Object>> msgService

    @Initializer
    void init() {

        this.msgService.sender( [ "address": "testaddr" ] ).send( [ "goat": APSVertXEventBusMessagingTest.GOAT ] as Map<String, Object>)
    }
}

