package se.natusoft.osgi.aps.tcpipsvc

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.junit.Test
import org.osgi.framework.BundleContext
import se.natusoft.osgi.aps.api.net.tcpip.APSTCPIPService
import se.natusoft.osgi.aps.api.net.tcpip.StreamedRequest
import se.natusoft.osgi.aps.api.net.tcpip.StreamedRequestListener
import se.natusoft.osgi.aps.net.messaging.models.config.TestConfigValue
import se.natusoft.osgi.aps.tcpipsvc.config.ExpertConfig
import se.natusoft.osgi.aps.tcpipsvc.config.TCPIPConfig
import se.natusoft.osgi.aps.tcpipsvc.security.TCPSecurityHandler
import se.natusoft.osgi.aps.test.tools.OSGIServiceTestTools
import se.natusoft.osgi.aps.test.tools.TestBundle
import se.natusoft.osgi.aps.tools.APSActivator
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.APSServiceTracker

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
    public void testSendAndReceive() {
        if (testActive) {

            configSetup1()

            OSGIServiceTestTools testTools = new OSGIServiceTestTools()

            TestBundle bundle = testTools.createBundle("tcpip-service")
            bundle.loadEntryPathsFromDirScan("APS-Network/APSTCPIPServiceProvider/target/classes")

            APSActivator activator = new APSActivator()
            activator.start(bundle.bundleContext)

            URI cp = new URI("tcp://localhost:12345")

            try {
                receive(bundle.bundleContext, cp) { InputStream requestStream, OutputStream responseStream ->
                    print("Received rec, ")
                    DataInputStream dis = new DataInputStream(requestStream)
                    String reqMsg = dis.readUTF()
                    DataOutputStream dos = new DataOutputStream(responseStream)
                    if (reqMsg == "This is a test string!") {
                        dos.writeUTF("*1*")
                        println("responded with '*1*'.")
                    } else {
                        dos.writeUTF("*2*")
                        println("responded with '*2*'.")
                    }
                    dos.flush()
                }

                Random rnd = new Random()
                10.times {
                    if (rnd.nextBoolean()) {
                        send(bundle.bundleContext, cp) { OutputStream requestStream, InputStream responseStream ->
                            DataOutputStream dos = new DataOutputStream(requestStream)
                            dos.writeUTF("This is a test string!")
                            dos.flush()

                            println("Sent req, waiting answer ...")

                            DataInputStream dis = new DataInputStream(responseStream)
                            String resp = dis.readUTF()

                            println("Got answer: ${resp}")

                            assertTrue("Expected '*1*', got '${resp}'!", resp == "*1*")
                        }
                    }
                    else {
                        send(bundle.bundleContext, cp) { OutputStream requestStream, InputStream responseStream ->
                            DataOutputStream dos = new DataOutputStream(requestStream)
                            dos.writeUTF("This is another test string!")
                            dos.flush()

                            println("Sent req, waiting answer ...")

                            DataInputStream dis = new DataInputStream(responseStream)
                            String resp = dis.readUTF()

                            println("Got answer: ${resp}")

                            assertTrue("Expected '*2*', got '${resp}'!", resp == "*2*")
                        }
                    }
                }
            }
            finally {
                activator.stop(bundle.bundleContext)
            }
        }
        else {
            println("This test is currently disabled!")
            println("Run with -Daps.test.disabled=false to run it.")
        }

        System.out.println("Test done!")

    }

    private static void receive(BundleContext bundleContext, URI cp, Closure callback) {
        APSServiceTracker<APSTCPIPService> tcpipServiceTracker = new APSServiceTracker<>(bundleContext, APSTCPIPService.class, "2 seconds")
        tcpipServiceTracker.start()
        APSTCPIPService tcpipService = tcpipServiceTracker.wrappedService

        tcpipService.setStreamedRequestListener(cp, new StreamedRequestListener() {
            @Override
            void requestReceived(URI receivePoint, InputStream requestStream, OutputStream responseStream) throws IOException {
                callback.call(requestStream, responseStream)
            }
        })

        tcpipServiceTracker.stop(bundleContext)
    }

    private static void send(BundleContext bundleContext, URI cp, Closure callback) {
        APSServiceTracker<APSTCPIPService> tcpipServiceTracker = new APSServiceTracker<>(bundleContext, APSTCPIPService.class, "2 seconds")
        tcpipServiceTracker.start()
        APSTCPIPService tcpipService = tcpipServiceTracker.wrappedService

        tcpipService.sendStreamedRequest(cp, new StreamedRequest() {
            @Override
            void sendRequest(URI sendPoint, OutputStream requestStream, InputStream responseStream) throws IOException {
                callback.call(requestStream, responseStream)
            }
        })

        tcpipServiceTracker.stop(bundleContext)
    }
}
