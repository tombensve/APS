package se.natusoft.osgi.aps.net.messaging

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.junit.Test
import org.osgi.framework.BundleContext
import se.natusoft.osgi.aps.api.net.messaging.service.APSSimpleMessageService
import se.natusoft.osgi.aps.net.messaging.config.ServiceConfig
import se.natusoft.osgi.aps.api.net.util.TypedData
import se.natusoft.osgi.aps.net.messaging.models.config.TestConfigValue
import se.natusoft.osgi.aps.net.messaging.models.config.TestConfigValueList
import se.natusoft.osgi.aps.tcpipsvc.config.ExpertConfig
import se.natusoft.osgi.aps.tcpipsvc.config.TCPIPConfig
import se.natusoft.osgi.aps.test.tools.OSGIServiceTestTools
import se.natusoft.osgi.aps.tools.APSActivator
import se.natusoft.osgi.aps.tools.APSServiceTracker
import se.natusoft.osgi.aps.tools.annotation.activator.BundleStart
import se.natusoft.osgi.aps.tools.annotation.activator.BundleStop
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiService
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiServiceProvider

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull

@CompileStatic
@TypeChecked
class TCPSimpleMessageServiceTest extends OSGIServiceTestTools {

    private static boolean isTestActive() {
        return !(System.getProperty("aps.test.disabled") == "true")
    }

    @Test
    public void testAPSTCPSimpleMessageServiceProvider() {
        if (testActive) {
            runTest()
        }
        else {
            println "This test is currently disabled!"
            println "Remove the -Daps.test.disabled=true to run it."
        }

        println "Test done!"
    }

    private void runTest() throws Throwable {

        // ---- Deploy some bundles (Using Groovy DSL:ish goodness :-)) ---- //
        // Since there is a test dependency on this artifact that artifact should have been built before this one!
        deploy 'aps-tcpip-service-provider' with new APSActivator() with {
            TCPIPConfig testTCPIPConfig = new TCPIPConfig()

            ExpertConfig expertConfig = new ExpertConfig()
            expertConfig.exceptionGuardMaxExceptions = new TestConfigValue(value: "30")
            expertConfig.exceptionGuardReactLimit = new TestConfigValue(value: "300")
            expertConfig.tcpCallbackThreadPoolSize = new TestConfigValue(value: "30")

            testTCPIPConfig.expert = expertConfig

            testTCPIPConfig // <--
        } from 'se.natusoft.osgi.aps', 'aps-tcpip-service-provider' ,'1.0.0'

        deploy 'aps-tcp-simple-message-service-provider' with new APSActivator() with {
            ServiceConfig testServiceConfig = new ServiceConfig()
            testServiceConfig.registerWithDiscoveryService = new TestConfigValue(value: "false")
            testServiceConfig.listenConnectionPointUrl = new TestConfigValue(value: "tcp://localhost:11320")
            testServiceConfig.lookInDiscoveryService = new TestConfigValue(value: "false")

            testServiceConfig.sendConnectionPointUrls = new TestConfigValueList()
            ((TestConfigValueList)testServiceConfig.sendConnectionPointUrls).getConfigs().
                    add(new TestConfigValue(value: "tcp://localhost:11320"))

            testServiceConfig // <--
        } from 'APS-Network/APSTCPSimpleMessageServiceProvider/target/classes'

        // ReceiverSvc is implemented below.
        deploy 'receiver-bundle' with new APSActivator() using '/se/natusoft/osgi/aps/net/messaging/ReceiverSvc.class'

        // SenderSvc is implemented below.
        deploy 'sender-bundle' with new APSActivator() using '/se/natusoft/osgi/aps/net/messaging/SenderSvc.class'

        // ---- Wait for things to happen ---- //
        delay 1000

        // ---- Get received message ---- //
        try {
            // We create a new Bundle in which we use APSServiceTracker to get the MessageReceivedService
            // (implemented by ReceiverSvc) which we use to fetch the received message that SenderSvc sent.

            with_new_bundle "test-result-lookup-bundle", { BundleContext context ->
                APSServiceTracker<MessageReceivedService> msgRecvSvcTracker =
                        new APSServiceTracker<>(context, MessageReceivedService.class, "2 seconds")
                msgRecvSvcTracker.start()
                MessageReceivedService messageReceivedService = msgRecvSvcTracker.allocateService()
                    TypedData receivedData = TypedData.Provider.fromBytes(messageReceivedService.receivedMessage)

                    String messageType = receivedData?.contentType
                    byte[] msgBytes = receivedData?.content
                    String messageText = new String(msgBytes != null ? msgBytes : new byte[0])

                msgRecvSvcTracker.releaseService()
                msgRecvSvcTracker.stop(context)

                // ---- Validate Result ---- //

                assertNotNull "Expected a received message type, but it was null!", messageType
                assertNotNull "Expected a received message, but it was null!", messageText

                assertEquals "Got '${messageType}' rather than expected '${SenderSvc.CONTENT_TYPE}' type!", SenderSvc.CONTENT_TYPE, messageType

                assertEquals "Got '${messageText}' rather than expected '${SenderSvc.CONTENT}'!", SenderSvc.CONTENT, messageText
            }
        }
        // ---- Cleanup ---- //
        finally {
            shutdown()
        }
    }
}

/**
 * The service interface implemented by ReceiverSvc to be able to fetch received message.
 */
@CompileStatic
@TypeChecked
interface MessageReceivedService {
    byte[] getReceivedMessage();
}

/**
 * Represents a service in a Bundle that receives messages.
 *
 * Since we annotate this with @OSGiServiceProvider this instance will be registered as an OSGi service
 * by APSActivator. Thereby it is possible to lookup the instance using a service tracker and call it
 * to get the received message for validation.
 */
@CompileStatic
@TypeChecked
@OSGiServiceProvider
class ReceiverSvc implements MessageReceivedService, APSSimpleMessageService.MessageListener {

    private byte[] message = null

    @OSGiService(timeout = "2 seconds")
    private APSSimpleMessageService msgService

    @BundleStart
    public void start() throws Exception {
        this.msgService.addMessageListener "test", this
    }

    @BundleStop
    public void stop() throws Exception {
        this.msgService.removeMessageListener "test", this
    }

    @Override
    void messageReceived(String topic, byte[] message) {
        this.message = message
    }

    @Override
    byte[] getReceivedMessage() {
        this.message
    }
}

/**
 * Represents a service in a Bundle that sends messages.
 */
@CompileStatic
@TypeChecked
class SenderSvc {

    public static final String CONTENT_TYPE = "saltwater/fish"
    // I was listening to Scooters "How much is the fish" when I got the idea for test data :-)
    public static final String CONTENT = "><> ><> <>< ><>"

    @OSGiService(timeout = "2 seconds")
    private APSSimpleMessageService msgService

    @BundleStart
    public void start() throws Exception {
        this.msgService.sendMessage "test", new TypedData.Provider(contentType: CONTENT_TYPE, content: CONTENT.getBytes()).toBytes()
    }
}
