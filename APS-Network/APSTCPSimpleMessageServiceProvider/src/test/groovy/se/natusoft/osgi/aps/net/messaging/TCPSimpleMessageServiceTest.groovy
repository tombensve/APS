package se.natusoft.osgi.aps.net.messaging

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.junit.Test
import org.osgi.framework.Bundle
import se.natusoft.osgi.aps.api.net.messaging.service.APSSimpleMessageService
import se.natusoft.osgi.aps.api.net.util.TypedData
import se.natusoft.osgi.aps.net.messaging.config.ServiceConfig
import se.natusoft.osgi.aps.net.messaging.models.config.TestConfigValue
import se.natusoft.osgi.aps.net.messaging.models.config.TestConfigValueList
import se.natusoft.osgi.aps.tcpipsvc.config.ExpertConfig
import se.natusoft.osgi.aps.tcpipsvc.config.TCPIPConfig
import se.natusoft.osgi.aps.test.tools.OSGIServiceTestTools
import se.natusoft.osgi.aps.test.tools.TestBundle
import se.natusoft.osgi.aps.tools.APSActivator
import se.natusoft.osgi.aps.tools.APSServiceTracker
import se.natusoft.osgi.aps.tools.annotation.activator.BundleStart
import se.natusoft.osgi.aps.tools.annotation.activator.BundleStop
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiService
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiServiceProvider

import static org.junit.Assert.*;

@CompileStatic
@TypeChecked
class TCPSimpleMessageServiceTest {

    private static boolean isTestActive() {
        return !(System.getProperty("aps.test.disabled") == "true")
    }

    /**
     * Sets up configuration for APSTCPSimpleMessageServiceProvider.
     */
    private static void messageConfigSetup1() {

        ServiceConfig testServiceConfig = new ServiceConfig()
        testServiceConfig.registerWithDiscoveryService = new TestConfigValue(value: "false")
        testServiceConfig.listenConnectionPointUrl = new TestConfigValue(value: "tcp://localhost:11320")
        testServiceConfig.lookInDiscoveryService = new TestConfigValue(value: "false")

        testServiceConfig.sendConnectionPointUrls = new TestConfigValueList()
        ((TestConfigValueList)testServiceConfig.sendConnectionPointUrls).getConfigs().
                add(new TestConfigValue(value: "tcp://localhost:11320"))

        ServiceConfig.managed.serviceProviderAPI.configInstance = testServiceConfig
        ServiceConfig.managed.serviceProviderAPI.setManaged() // VERY IMPORTANT!
    }

    /**
     * Sets up configuration for APSTCPIPServiceProvider used by APSTCPSimpleMessageServiceProvider.
     */
    private static void tcpipConfigSetup1() {

        TCPIPConfig testTCPIPConfig = new TCPIPConfig()

        ExpertConfig expertConfig = new ExpertConfig()
        expertConfig.exceptionGuardMaxExceptions = new TestConfigValue(value: "30")
        expertConfig.exceptionGuardReactLimit = new TestConfigValue(value: "300")
        expertConfig.tcpCallbackThreadPoolSize = new TestConfigValue(value: "30")

        testTCPIPConfig.expert = expertConfig

        TCPIPConfig.managed.serviceProviderAPI.configInstance = testTCPIPConfig
        TCPIPConfig.managed.serviceProviderAPI.setManaged() // VERY IMPORTANT!
    }

    @Test
    public void testAPSTCPSimpleMessageServiceProvider() {
        if (testActive) {
            runTest()
        }
        else {
            println("This test is currently disabled!")
            println("Run with -Daps.test.disabled=false to run it.")
        }

        System.out.println("Test done!")
    }

    private static void runTest() throws Exception {
        messageConfigSetup1()
        tcpipConfigSetup1()

        // ---- Start OSGi test run support ---- //
        OSGIServiceTestTools testTools = new OSGIServiceTestTools();

        // ---- Deploy APSTCPIPServiceProvider ---- //
        TestBundle tcpipServiceBundle = testTools.createBundle("aps-tcpip-service-provider")
        // Since there is a test dependency on this artifact that artifact should have been built before this one!
        tcpipServiceBundle.loadEntryPathsFromMaven("se.natusoft.osgi.aps", "aps-tcpip-service-provider" ,"1.0.0")
        APSActivator tcpipSvcActivator = new APSActivator()
        tcpipSvcActivator.start(tcpipServiceBundle.bundleContext)

        // ---- Deploy APSTCPSimpleMessageServiceProvider ---- //
        TestBundle simpleMessageServiceBundle = testTools.createBundle("aps-tcp-simple-message-service-provider")
        // Our code is at least built at this time, so load from target/classes.
        simpleMessageServiceBundle.loadEntryPathsFromDirScan("APS-Network/APSTCPSimpleMessageServiceProvider/target/classes")
        APSActivator msgActivator = new APSActivator()
        msgActivator.start(simpleMessageServiceBundle.bundleContext)

        // ---- Deploy ReceiverSvc ---- //
        TestBundle receiverBundle = testTools.createBundle("receiver-bundle")
        receiverBundle.addEntryPaths("/se/natusoft/osgi/aps/net/messaging/ReceiverSvc.class")
        APSActivator receiverActivator = new APSActivator()
        receiverActivator.start(receiverBundle.bundleContext)

        // ---- Deploy SenderSvc ---- //
        TestBundle senderBundle = testTools.createBundle("sender-bundle")
        senderBundle.addEntryPaths("/se/natusoft/osgi/aps/net/messaging/SenderSvc.class")
        APSActivator senderActivator = new APSActivator()
        senderActivator.start(senderBundle.bundleContext)

        // ---- Wait for things to happen ---- //
        Thread.sleep(500)

        // ---- Get received message ---- //
        // We create a new Bundle in which we use APSServiceTracker to get the MessageReceivedService
        // (implemented by ReceiverSvc) which we use to fetch the received message that SenderSvc sent.
        Bundle testValidateBundle = testTools.createBundle("test-validate-bundle")

            APSServiceTracker<MessageReceivedService> msgRecvSvcTracker =
                    new APSServiceTracker<>(testValidateBundle.bundleContext, MessageReceivedService.class, "2 seconds")
            msgRecvSvcTracker.start()
            MessageReceivedService messageReceivedService = msgRecvSvcTracker.allocateService()

                String messageType = messageReceivedService.receivedMessage.contentType
                String messageText = new String(messageReceivedService.receivedMessage.content)

            msgRecvSvcTracker.releaseService()
            msgRecvSvcTracker.stop(testValidateBundle.bundleContext)

        testTools.removeBundle(testValidateBundle)

        // ---- Validate Result ---- //
        try {

            assertNotNull("Expected a received message type, but it was null!", messageType)
            assertNotNull("Expected a received message, but it was null!", messageText)

            assertEquals("Got '${messageType}' rather than expected '${SenderSvc.CONTENT_TYPE}' type!", SenderSvc.CONTENT_TYPE, messageType)

            assertEquals("Got '${messageText}' rather than expected '${SenderSvc.CONTENT}'!", SenderSvc.CONTENT, messageText)
        }

        // ---- Cleanup ---- //
        finally {

            senderActivator.stop(senderBundle.bundleContext)
            testTools.removeBundle(senderBundle)

            receiverActivator.stop(receiverBundle.bundleContext)
            testTools.removeBundle(receiverBundle)

            msgActivator.stop(simpleMessageServiceBundle.bundleContext)
            testTools.removeBundle(simpleMessageServiceBundle)

            tcpipSvcActivator.stop(tcpipServiceBundle.bundleContext)
            testTools.removeBundle(tcpipServiceBundle)
        }
    }
}

/**
 * The service interface implemented by ReceiverSvc to be able to fetch received message.
 */
interface MessageReceivedService {
    TypedData getReceivedMessage();
}

/**
 * Represents a service in a Bundle that receives messages.
 *
 * Since we annotate this with @OSGiServiceProvider this instance will be registered as an OSGi service
 * by APSActivator. Thereby it is possible to lookup the instance using a service tracker and call it
 * to get the received message for validation.
 */
@OSGiServiceProvider
class ReceiverSvc implements MessageReceivedService, APSSimpleMessageService.MessageListener {

    public static final String TYPE = "tcp.msg.svc.msg.type"
    public static final String MSG = "tcp.msg.svc.msg"

    private TypedData message = null

    @OSGiService(timeout = "2 seconds")
    private APSSimpleMessageService msgService

    @BundleStart
    public void start() throws Exception {
        this.msgService.addMessageListener("test", this)
    }

    @BundleStop
    public void stop() throws Exception {
        this.msgService.removeMessageListener("test", this)
    }

    @Override
    void messageReceived(String topic, TypedData message) {
        this.message = message
    }

    @Override
    TypedData getReceivedMessage() {
        this.message
    }
}

/**
 * Represents a service in a Bundle that sends messages.
 */
class SenderSvc {

    public static final CONTENT_TYPE = "saltwater/fish"
    // I was listening to Scooters "How much is the fish" when I got the idea for test data :-)
    public static final CONTENT = "><> ><> <>< ><>"

    @OSGiService(timeout = "2 seconds")
    private APSSimpleMessageService msgService

    @BundleStart
    public void start() throws Exception {
        TypedData message = new TypedData.Provider(
                contentType: CONTENT_TYPE,
                content: CONTENT.getBytes()
        )

        this.msgService.sendMessage("test", message)
    }
}
