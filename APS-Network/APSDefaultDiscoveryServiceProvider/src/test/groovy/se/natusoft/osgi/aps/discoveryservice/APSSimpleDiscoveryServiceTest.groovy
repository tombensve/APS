package se.natusoft.osgi.aps.discoveryservice

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.junit.Test
import se.natusoft.osgi.aps.json.APSJSONServiceActivator
import se.natusoft.osgi.aps.net.messaging.models.config.TestConfigList

import static org.junit.Assert.*
import org.osgi.framework.BundleContext
import se.natusoft.osgi.aps.api.net.discovery.service.APSSimpleDiscoveryService
import se.natusoft.osgi.aps.discoveryservice.config.DiscoveryConfig
import se.natusoft.osgi.aps.net.messaging.models.config.TestConfigValue
import se.natusoft.osgi.aps.tcpipsvc.config.ExpertConfig
import se.natusoft.osgi.aps.tcpipsvc.config.TCPIPConfig
import se.natusoft.osgi.aps.test.tools.OSGIServiceTestTools
import se.natusoft.osgi.aps.tools.APSActivator
import se.natusoft.osgi.aps.tools.APSServiceTracker
import se.natusoft.osgi.aps.tools.tracker.WithService

@CompileStatic
@TypeChecked
class APSSimpleDiscoveryServiceTest extends OSGIServiceTestTools {

    // If this test fails, raise this number! I have a very fast machine ...
    private static final int DELAY = 1000;

    private static boolean isTestActive() {
        return !(System.getProperty("aps.test.disabled") == "true")
    }

    @Test
    public void testAPSSimpleDiscoveryServiceTest() {
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
        deploy 'aps-tcpip-service-provider' with new APSActivator() with {
            TCPIPConfig testTCPIPConfig = new TCPIPConfig()

            ExpertConfig expertConfig = new ExpertConfig()
            expertConfig.exceptionGuardMaxExceptions = new TestConfigValue(value: "30")
            expertConfig.exceptionGuardReactLimit = new TestConfigValue(value: "300")
            expertConfig.tcpCallbackThreadPoolSize = new TestConfigValue(value: "30")

            testTCPIPConfig.expert = expertConfig
            testTCPIPConfig.byteBufferSize = new TestConfigValue(value: "1000")

            testTCPIPConfig // <--
        } from 'se.natusoft.osgi.aps', 'aps-tcpip-service-provider' ,'1.0.0'

        deploy 'aps-json-service' with new APSJSONServiceActivator() from 'se.natusoft.osgi.aps', 'aps-json-service-provider', '1.0.0'

        deploy 'aps-default-discovery-svc-1' with new APSActivator() with {
            DiscoveryConfig discoveryConfig = new DiscoveryConfig()
            discoveryConfig.multicastConnectionPoint = new TestConfigValue(value: "multicast://all-systems.mcast.net:11265")
            discoveryConfig.manualServiceEntries = new TestConfigList<>()
            discoveryConfig
        } from 'APS-Network/APSDefaultDiscoveryServiceProvider/target/classes'

        deploy 'aps-default-discovery-svc-2' with new APSActivator() with {
            DiscoveryConfig discoveryConfig = new DiscoveryConfig()
            discoveryConfig.multicastConnectionPoint = new TestConfigValue(value: "multicast://all-systems.mcast.net:11265")
            discoveryConfig.manualServiceEntries = new TestConfigList<>()
            discoveryConfig
        } from 'APS-Network/APSDefaultDiscoveryServiceProvider/target/classes'

        try {
            with_new_bundle 'test-exec-bundle', { BundleContext context ->
                // Note, normally APSServiceTracker.allocate() waits for a service to become available before
                // returning or timing out. But in this case we are using the .withAllAvailableServices(...)
                // method. This loops through the currently available services without waiting for any if
                // there are none! Therefore we have to do a small delay so that we don't do this before everything
                // is upp and running.
                Thread.sleep(DELAY)

                println "Starting discovery ..."
                APSServiceTracker<APSSimpleDiscoveryService> discoveryServiceTracker =
                        new APSServiceTracker<>(context, APSSimpleDiscoveryService.class, "10 sec")
                discoveryServiceTracker.start()

                discoveryServiceTracker.withAllAvailableServices(new WithService<APSSimpleDiscoveryService>() {
                    @Override
                    public void withService(APSSimpleDiscoveryService service) throws Exception {
                        handleSvc(service)
                    }
                })

                discoveryServiceTracker.stop()
            }
        }
        finally {
            shutdown()
        }
    }

    // The first invokation is the publishing discovery service, the second invokation is the receiving
    // discovery service that should have received what the first published.
    private int invokation = 0;
    private void handleSvc(APSSimpleDiscoveryService service) throws Exception {
        println "service: ${service}, invokation: ${invokation}"
        if (this.invokation == 0) {
            Properties serviceDesc = [
                    serviceName: "testSvc",
                    serviceURI: "multicast://all-systems.mcast.net:56789"
            ]
            service.publishService(serviceDesc)
            Thread.sleep(DELAY)
        }
        else {
            Set<Properties> services = service.getServices("(serviceName=testSvc)")
            assertEquals(1, services.size())
            Properties serviceDescription = services.getAt(0)
            assertEquals("testSvc", serviceDescription.serviceName)
            assertEquals("multicast://all-systems.mcast.net:56789", serviceDescription.serviceURI)
        }
        ++this.invokation
    }

}
