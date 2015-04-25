package se.natusoft.osgi.aps.tcpipsvc

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.junit.Test
import se.natusoft.osgi.aps.api.net.tcpip.APSTCPIPService
import se.natusoft.osgi.aps.api.net.tcpip.UDPListener
import se.natusoft.osgi.aps.net.messaging.models.config.TestConfigList
import se.natusoft.osgi.aps.net.messaging.models.config.TestConfigValue
import se.natusoft.osgi.aps.tcpipsvc.config.TCPIPConfig
import se.natusoft.osgi.aps.tcpipsvc.security.UDPSecurityHandler
import se.natusoft.osgi.aps.test.tools.OSGIServiceTestTools
import se.natusoft.osgi.aps.test.tools.TestBundle
import se.natusoft.osgi.aps.tools.APSActivator
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.APSServiceTracker

import static org.junit.Assert.assertTrue

/**
 * Test MulticastReceiver and MulticastSender.
 */
@CompileStatic
@TypeChecked
class MulticastConProviderTest {

    /** We don't want to open sockets on a normal build.  */
//    private static final boolean testActiveFlag = false
    private static final boolean testActiveFlag = true

    private static boolean isTestActive() {
        return testActiveFlag || System.getProperty("aps.test.activate.disabled") == "true"
    }

    private static void configSetup1() {
        TCPIPConfig.NamedConfig namedConfig1 = new TCPIPConfig.NamedConfig()
        namedConfig1.name = new TestConfigValue(value: "testsvc")
        namedConfig1.type = new TestConfigValue(value: ConnectionProvider.Type.Multicast.name())
        namedConfig1.address = new TestConfigValue(value: "all-systems.mcast.net") //
        namedConfig1.port = new TestConfigValue(value: "12345")
        namedConfig1.secure = new TestConfigValue(value: "false")

        TCPIPConfig.NamedConfig namedConfig2 = new TCPIPConfig.NamedConfig()
        namedConfig2.name = new TestConfigValue(value: "testclient")
        namedConfig2.type = new TestConfigValue(value: ConnectionProvider.Type.Multicast.name())
        namedConfig2.address = new TestConfigValue(value: "all-systems.mcast.net")
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

                String testString = "This is a multicasted string!"
                int test = 0

                tcpipService.addUDPListener("testsvc", new UDPListener() {
                    @Override
                    void udpDataReceived(String name, DatagramPacket dataGramPacket) {
                        println("name: ${name}")
                        println "" + ++test

                        String received = new String(dataGramPacket.data, 0, dataGramPacket.length)
                        println("Received: ${received}")

                        if (received == testString) success = true
                    }
                })

                tcpipService.sendUDP("testclient", testString.bytes)
                println("Send: ${testString}")

                tcpipSvcTracker.releaseService()

                Thread.sleep(2000)

                assertTrue("Failed to receive correct message!", success)
            }
            finally {
                // This logs a ConcurrentModificationException where it should be impossible for that to happen as far
                // as I can determine. This is only test code, so we skip this for now.
                // try {activator.stop(testBundle.bundleContext)} catch (Exception cme) {}
            }

        }
        else {
            println("This test is currently disabled since we don't want to run it during normal builds due to TCP/IP traffic.")
            println("Add -Daps.test.activate.disabled=true to actually run it.")
        }
    }

}
