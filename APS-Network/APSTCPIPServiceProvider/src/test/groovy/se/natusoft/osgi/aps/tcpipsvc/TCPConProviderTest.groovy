package se.natusoft.osgi.aps.tcpipsvc

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.junit.Test
import se.natusoft.osgi.aps.api.net.tcpip.StreamedRequest
import se.natusoft.osgi.aps.api.net.tcpip.StreamedRequestListener
import se.natusoft.osgi.aps.net.messaging.models.config.TestConfigValue
import se.natusoft.osgi.aps.tcpipsvc.config.ExpertConfig
import se.natusoft.osgi.aps.tcpipsvc.config.TCPIPConfig
import se.natusoft.osgi.aps.tcpipsvc.security.TCPSecurityHandler
import se.natusoft.osgi.aps.tools.APSLogger

import static org.junit.Assert.assertTrue

/**
 * Test UDPReceiver and UDPSender.
 */
@CompileStatic
@TypeChecked
class TCPConProviderTest {

    private static boolean isTestActive() {
        return !(System.getProperty("aps.test.disabled") == "true")
    }

    private static void configSetup1() {

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
    public void testSendAndReceive() throws Exception {
        if (testActive) {
            configSetup1()

            URI connectionPoint = new URI("tcp://localhost:12345")

            TCPSecurityHandler tcpSecurityHandler = new TCPSecurityHandler()
            TCPReceiver receiver = new TCPReceiver(
                    connectionPoint: connectionPoint,
                    logger: new APSLogger(),
                    securityHandler: tcpSecurityHandler
            )

            receiver.setListener(new StreamedRequestListener() {
                @Override
                void requestReceived(URI receivePoint, InputStream requestStream, OutputStream responseStream) {
                    System.out.println("receive point:${receivePoint}")

                    DataInputStream dis = new DataInputStream(requestStream)
                    String reqMsg = dis.readUTF()
                    DataOutputStream dos = new DataOutputStream(responseStream)
                    if (reqMsg == "This is a test string!") {
                        dos.writeUTF("OK")
                    }
                    else {
                        dos.writeUTF("BAD")
                    }
                    dos.flush()

//                    dos.close() // Uncomment to test the close() catch.
                }
            })

            receiver.start()

                TCPSender sender = new TCPSender(
                        connectionPoint: connectionPoint,
                        securityHandler: tcpSecurityHandler
                )
                sender.start()
                    String resp = ""

                    sender.send(new StreamedRequest() {
                        @Override
                        void sendRequest(URI sendPoint, OutputStream requestStream, InputStream responseStream) throws IOException {
                            DataOutputStream dos = new DataOutputStream(requestStream)
                            dos.writeUTF("This is a test string!")
                            dos.flush()

                            println("Sent req, waiting answer ...")

                            DataInputStream dis = new DataInputStream(responseStream)
                            resp = dis.readUTF()
                        }
                    })

                    assertTrue("Expected 'OK', got '${resp}'!", resp == "OK")

                    println("Got OK Response!")
                sender.stop()

            receiver.stop()

        }
        else {
            println("This test is currently disabled!")
            println("Run with -Daps.test.disabled=false to run it.")
        }

        System.out.println("Test done!")
    }

}
