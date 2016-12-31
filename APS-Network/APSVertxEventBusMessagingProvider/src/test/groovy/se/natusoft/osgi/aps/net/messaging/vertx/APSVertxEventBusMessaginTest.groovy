package se.natusoft.osgi.aps.net.messaging.vertx

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.junit.Test
import se.natusoft.docutations.NotNull
import se.natusoft.osgi.aps.api.net.messaging.service.APSMessageService
import se.natusoft.osgi.aps.constants.APS
import se.natusoft.osgi.aps.test.tools.OSGIServiceTestTools
import se.natusoft.osgi.aps.tools.APSActivator
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.annotation.activator.*

@CompileStatic
@TypeChecked
class APSVertXEventBusMessagingTest extends OSGIServiceTestTools {

    static final String GOAT = "baeaeae"

    static boolean messageReceived = false

    @Test
    void test() throws Exception {

        deploy 'aps-vertx-event-bus-messaging-provider' with new APSActivator() from 'APS-Network/APSVertxEventBusMessagingProvider/target/classes'

        deploy 'msg-receiver' with new APSActivator() using '/se/natusoft/osgi/aps/net/messaging/vertx/MsgReceiver.class'

        deploy 'msg-sender' with new APSActivator() using '/se/natusoft/osgi/aps/net/messaging/vertx/MsgSender.class'


        // Wait for and then validate result. Even if in this case a cluster of one member is created, it takes
        // some time.
        try {
            int count = 0
            while ( !messageReceived ) {
                synchronized ( this ) {
                    wait( 200 )
                }
                ++count;
                if ( count > 20 ) break // If build on a really slow machine this might not be enough!
            }
            assert messageReceived
        }
        finally {
            shutdown()
        }
    }

}


@OSGiServiceProvider(
        properties = [
                @OSGiProperty( name = APS.Service.Provider, value = "aps-vertx-event-bus-receiver" ),
                @OSGiProperty( name = APS.Service.Category, value = APS.Value.Service.Category.Network ),
                @OSGiProperty( name = APS.Service.Function, value = APS.Value.Service.Function.Messaging )
        ]
)
class MsgReceiver implements APSMessageService.Listener {

    @OSGiService( timeout = "15 sec" )
    private APSMessageService msgService

    @Managed( loggingFor = "msg-receiver" )
    private APSLogger logger

    @Initializer
    void init() {
        // Note: Since the APSVertxEventBusMessagingProvider does not register as an OSGi service until the cluster is upp and
        // Vert.x is ready, this call will just wait for the service to become available and then call it, since APSActivator
        // injects an APSServiceTracker wrapped service implementation. You can see the timeout on the @OSGiService annotation
        // above.

        this.msgService.subscribe( "goat", this )
    }

    @Override
    void messageReceived( @NotNull Object message ) {
        this.logger.info( "Message received: [${message}]" )

        if ( message.toString() == APSVertXEventBusMessagingTest.GOAT ) {
            APSVertXEventBusMessagingTest.messageReceived = true
        }
    }
}


@OSGiServiceProvider(
        properties = [
                @OSGiProperty( name = APS.Service.Provider, value = "aps-vertx-event-bus-sender" ),
                @OSGiProperty( name = APS.Service.Category, value = APS.Value.Service.Category.Network ),
                @OSGiProperty( name = APS.Service.Function, value = APS.Value.Service.Function.Messaging )
        ]
)
class MsgSender  {

    @OSGiService
    private APSMessageService msgService

    @Initializer
    void init() {
        // See the comment on MsgReceiver. The same applies here.

        this.msgService.publish( "goat", APSVertXEventBusMessagingTest.GOAT, APSMessageService.Receivers.ALL )
    }
}

