package se.natusoft.osgi.aps.net.messaging

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.junit.Test
import se.natusoft.osgi.aps.api.net.util.TypedData
import se.natusoft.osgi.aps.net.messaging.bundles.ReceiverSvc
import se.natusoft.osgi.aps.net.messaging.bundles.SenderSvc
import se.natusoft.osgi.aps.net.messaging.config.ServiceConfig
import se.natusoft.osgi.aps.net.messaging.models.config.TestConfigValue
import se.natusoft.osgi.aps.net.messaging.models.config.TestConfigValueList
import se.natusoft.osgi.aps.tcpipsvc.config.ExpertConfig
import se.natusoft.osgi.aps.tcpipsvc.config.TCPIPConfig
import se.natusoft.osgi.aps.test.tools.OSGIServiceTestTools
import se.natusoft.osgi.aps.test.tools.TestBundle
import se.natusoft.osgi.aps.tools.APSActivator

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull

/**
 *
 */
@CompileStatic
@TypeChecked
class TCPSimpleMessageServiceTest {

    private TypedData message

    private static boolean isTestActive() {
        return !(System.getProperty("aps.test.disabled") == "true")
    }

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
        // Setup config
        messageConfigSetup1()
        tcpipConfigSetup1()

        // ---- Start OSGi test run support ---- //
        OSGIServiceTestTools testTools = new OSGIServiceTestTools();

        // ---- Deploy APSTCPiPServiceProvider ---- //
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
        receiverBundle.addEntryPaths("/se/natusoft/osgi/aps/net/messaging/bundles/ReceiverSvc.class")
        APSActivator receiverActivator = new APSActivator()
        receiverActivator.start(receiverBundle.bundleContext)

        // ---- Deploy SenderSvc ---- //
        TestBundle senderBundle = testTools.createBundle("sender-bundle")
        senderBundle.addEntryPaths("/se/natusoft/osgi/aps/net/messaging/bundles/SenderSvc.class")
        APSActivator senderActivator = new APSActivator()
        senderActivator.start(senderBundle.bundleContext)

        // ---- Wait for things to happen ---- //
        Thread.sleep(500)

        // ---- Validate Result ---- //
        try {
            // The receiver stores the received message in system properties.
            String messageType = System.getProperty(ReceiverSvc.TYPE)
            String messageText = System.getProperty(ReceiverSvc.MSG)

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
