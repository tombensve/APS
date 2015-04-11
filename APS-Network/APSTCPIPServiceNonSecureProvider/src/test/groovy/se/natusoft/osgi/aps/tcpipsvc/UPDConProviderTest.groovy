package se.natusoft.osgi.aps.tcpipsvc

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.junit.Test
import se.natusoft.osgi.aps.api.core.config.model.APSConfigList
import se.natusoft.osgi.aps.api.net.tcpip.UDPListener
import se.natusoft.osgi.aps.net.messaging.models.config.TestConfigList
import se.natusoft.osgi.aps.net.messaging.models.config.TestConfigValue
import se.natusoft.osgi.aps.tcpipsvc.config.TCPIPConfig
import se.natusoft.osgi.aps.tools.APSLogger

import static org.junit.Assert.*

/**
 * Test UDPReceiver and UDPSender.
 */
@CompileStatic
@TypeChecked
class UPDConProviderTest {

    /** We don't want to open sockets on a normal build.  */
//    private static final boolean testActiveFlag = false
    private static final boolean testActiveFlag = true

    private static boolean isTestActive() {
        return testActiveFlag && !(System.getProperty("aps.test.disabled") == "true")
    }

    private static void configSetup1() {
        TCPIPConfig.NamedConfig namedConfig1 = new TCPIPConfig.NamedConfig()
        namedConfig1.name = new TestConfigValue(value: "testsvc")
        namedConfig1.type = new TestConfigValue(value: ConnectionProvider.Type.UDP.name())
        namedConfig1.address = new TestConfigValue(value: "localhost")
        namedConfig1.port = new TestConfigValue(value: "12345")

        TCPIPConfig.NamedConfig namedConfig2 = new TCPIPConfig.NamedConfig()
        namedConfig2.name = new TestConfigValue(value: "testclient")
        namedConfig2.type = new TestConfigValue(value: ConnectionProvider.Type.UDP.name())
        namedConfig2.address = new TestConfigValue(value: "localhost")
        namedConfig2.port = new TestConfigValue(value: "12345")

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

            APSLogger logger = new APSLogger()

            UDPReceiver receiver = new UDPReceiver(config: new ConfigWrapper(name: "testsvc"), logger: logger)

            boolean success = false

            receiver.addListener(new UDPListener() {
                @Override
                void udpDataReceived(String name, DatagramPacket dataGramPacket) {
                    println("name: ${name}")

                    String received = new String(dataGramPacket.data, 0 , dataGramPacket.length)
                    println("Received: ${received}")

                    if (received == "This is a test string!") success = true
                }
            })

            receiver.start()

                UDPSender sender = new UDPSender(config: new ConfigWrapper(name: "testclient"))
                sender.start()
                    sender.send("This is a test string!".bytes)
                sender.stop()

            receiver.stop()

            assertTrue("Failed to receive correct message!", success)

        }
        else {
            println("This test is currently disabled!")
            println("Remove -Daps.test.disabled=true to run it.")
        }
    }

}
