package se.natusoft.osgi.aps.tcpipsvc

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.junit.Test
import se.natusoft.osgi.aps.api.net.tcpip.APSTCPIPService
import se.natusoft.osgi.aps.api.net.tcpip.UDPListener
import se.natusoft.osgi.aps.net.messaging.models.config.TestConfigList
import se.natusoft.osgi.aps.net.messaging.models.config.TestConfigValue
import se.natusoft.osgi.aps.tcpipsvc.config.TCPIPConfig
import se.natusoft.osgi.aps.test.tools.OSGIServiceTestTools
import se.natusoft.osgi.aps.test.tools.TestBundle
import se.natusoft.osgi.aps.tools.APSActivator
import se.natusoft.osgi.aps.tools.APSServiceTracker

import static org.junit.Assert.assertTrue

/**
 * Test UDPReceiver and UDPSender.
 */
@CompileStatic
@TypeChecked
class UPDConProviderTest {

    private static boolean isTestActive() {
        return !(System.getProperty("aps.test.disabled") == "true")
    }

    private static void configSetup1() {
        TCPIPConfig.NamedConfig namedConfig1 = new TCPIPConfig.NamedConfig()
        namedConfig1.name = new TestConfigValue(value: "testsvc")
        namedConfig1.type = new TestConfigValue(value: ConnectionProvider.Type.UDP.name())
        namedConfig1.address = new TestConfigValue(value: "localhost")
        namedConfig1.port = new TestConfigValue(value: "12345")
        namedConfig1.secure = new TestConfigValue(value: "false")

        TCPIPConfig.NamedConfig namedConfig2 = new TCPIPConfig.NamedConfig()
        namedConfig2.name = new TestConfigValue(value: "testclient")
        namedConfig2.type = new TestConfigValue(value: ConnectionProvider.Type.UDP.name())
        namedConfig2.address = new TestConfigValue(value: "localhost")
        namedConfig2.port = new TestConfigValue(value: "12345")
        namedConfig2.secure = new TestConfigValue(value: "false")

        TestConfigList<TCPIPConfig.NamedConfig> configs = new TestConfigList<>()
        configs.configs.add(namedConfig1)
        configs.configs.add(namedConfig2)

        TCPIPConfig testTCPIPConfig = new TCPIPConfig()
        testTCPIPConfig.namedConfigs = configs
        testTCPIPConfig.byteBufferSize = new TestConfigValue(value: "10000")

        TCPIPConfig.ExpertConfig expertConfig = new TCPIPConfig.ExpertConfig()
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

            assertTrue("Config failure!", TCPIPConfig.managed.get().namedConfigs.get(0).name.string == "testsvc")
            assertTrue("Config failure!", TCPIPConfig.managed.get().namedConfigs.get(1).name.string == "testclient")

            OSGIServiceTestTools testTools = new OSGIServiceTestTools()
            TestBundle testBundle = testTools.createBundle("test-bundle")
            testBundle.addEntryPaths(
                    "/se/natusoft/osgi/aps/tcpipsvc/APSTCPIPServiceProvider.class"
            );

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

                String testString = "This is an UDP sent string!"

                tcpipService.addUDPListener("testsvc", new UDPListener() {
                    @Override
                    void udpDataReceived(String name, DatagramPacket dataGramPacket) {
                        println("name: ${name}")

                        String received = new String(dataGramPacket.data, 0, dataGramPacket.length)
                        println("Received: ${received}")

                        if (received == testString) success = true
                    }
                })

                tcpipService.sendUDP("testclient", testString.bytes)
                println("Send: ${testString}")

                Thread.sleep(500)

                tcpipService.removeUDPListener("testsvc", APSTCPIPService.ALL_LISTENERS)

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
