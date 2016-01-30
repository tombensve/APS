package se.natusoft.osgi.aps.tcpipsvc

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.junit.Test
import se.natusoft.osgi.aps.api.net.tcpip.APSTCPIPService
import se.natusoft.osgi.aps.api.net.tcpip.DatagramPacketListener
import se.natusoft.osgi.aps.net.messaging.models.config.TestConfigValue
import se.natusoft.osgi.aps.tcpipsvc.config.ExpertConfig
import se.natusoft.osgi.aps.tcpipsvc.config.TCPIPConfig
import se.natusoft.osgi.aps.test.tools.OSGIServiceTestTools
import se.natusoft.osgi.aps.test.tools.TestBundle
import se.natusoft.osgi.aps.tools.APSActivator
import se.natusoft.osgi.aps.tools.APSServiceTracker

import static org.junit.Assert.assertTrue

/**
 * Test sending and receiving multicast data.
 */
@CompileStatic
@TypeChecked
class MulticastConProviderTest {

    private static boolean isTestActive() {
        return !(System.getProperty("aps.test.disabled") == "true")
    }

    private static void configSetup1() {

        TCPIPConfig testTCPIPConfig = new TCPIPConfig()
        testTCPIPConfig.byteBufferSize = new TestConfigValue(value: "10000")

        ExpertConfig expertConfig = new ExpertConfig()
        expertConfig.exceptionGuardMaxExceptions = new TestConfigValue(value: "30")
        expertConfig.exceptionGuardReactLimit = new TestConfigValue(value: "300")
        expertConfig.tcpCallbackThreadPoolSize = new TestConfigValue(value: "30")

        testTCPIPConfig.expert = expertConfig

        TCPIPConfig.managed.serviceProviderAPI.configInstance = testTCPIPConfig
        TCPIPConfig.managed.serviceProviderAPI.setManaged() // VERY IMPORTANT!
    }

    @Test
    public void testSendAndReceive() throws Exception {
        if (testActive) {
            configSetup1()

            OSGIServiceTestTools testTools = new OSGIServiceTestTools()
            TestBundle testBundle = testTools.createBundle("test-bundle")
            testBundle.loadEntryPathsFromDirScan("APS-Network/APSTCPIPServiceProvider/target/classes")

            APSActivator activator = new APSActivator()
            activator.start(testBundle.bundleContext)

            try {

                APSServiceTracker<APSTCPIPService> tcpipSvcTracker =
                        new APSServiceTracker<APSTCPIPService>(
                                testBundle.bundleContext,
                                APSTCPIPService.class,
                                "5 seconds"
                        );
                tcpipSvcTracker.start()

                APSTCPIPService tcpipService = tcpipSvcTracker.allocateService()

                boolean success = false

                String testString = "This is a multicasted string!"

                URI connectionPoint = new URI("multicast://all-systems.mcast.net:12345")

                tcpipService.addDataPacketListener(connectionPoint, new DatagramPacketListener() {
                    @Override
                    void dataBlockReceived(URI receivePoint, DatagramPacket packet) {
                        String received = new String(packet.data, 0, packet.length)
                        println("Received: ${received}")

                        if (received == testString) success = true
                    }
                })

                tcpipService.sendDataPacket(connectionPoint, testString.bytes)

                println("Send: ${testString}")

                Thread.sleep(500)

                tcpipService.removeDataPacketListener(connectionPoint, APSTCPIPService.ALL_LISTENERS)

                tcpipSvcTracker.releaseService()


                assertTrue("Failed to receive correct message!", success)
            }
            finally {
                activator.stop(testBundle.bundleContext)
            }

        }
        else {
            println("This test is currently disabled!")
            println("Run with -Daps.test.disabled=false to run it.")
        }
    }

}
