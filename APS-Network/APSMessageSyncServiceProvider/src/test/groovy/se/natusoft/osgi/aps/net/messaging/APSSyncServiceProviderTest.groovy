package se.natusoft.osgi.aps.net.messaging

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.osgi.framework.ServiceReference
import se.natusoft.osgi.aps.api.net.messaging.service.APSSyncService
import se.natusoft.osgi.aps.net.messaging.config.SyncServiceConfig
import se.natusoft.osgi.aps.tools.APSActivator
import se.natusoft.osgi.aps.test.tools.OSGIServiceTestTools
import se.natusoft.osgi.aps.test.tools.TestBundle
import se.natusoft.osgi.aps.net.messaging.models.config.*
import se.natusoft.osgi.aps.tools.APSServiceTracker

import static junit.framework.TestCase.assertTrue

class APSSyncServiceProviderTest {


    private TestBundle messageServiceBundle
    private APSActivator messageServiceBundleActivator

    private TestBundle syncServiceBundle
    private APSActivator syncServiceBundleActivator

    private OSGIServiceTestTools testTools = new OSGIServiceTestTools();

    @Before
    public void setup() {
        // Message service

        // This will register with instance name "test-cluster".
        messageServiceBundle = testTools.createBundle("message-service-bundle")
        messageServiceBundle.addEntryPaths(
                "/se/natusoft/osgi/aps/net/messaging/service/APSMessageServiceTestProvider.class"
        )
        messageServiceBundleActivator = new APSActivator()
        messageServiceBundleActivator.start(messageServiceBundle.bundleContext)

        // SyncService Bundle

        syncServiceBundle = testTools.createBundle("sync-service-bundle");
        syncServiceBundle.addEntryPaths(
                "/se/natusoft/osgi/aps/net/messaging/service/APSSyncServiceProvider.class"
        );

        SyncServiceConfig syncServiceConfig = new SyncServiceConfig()

        syncServiceConfig.validateSenderUUID = new TestConfigValue(value: UUID.randomUUID().toString())

        TestConfigList<SyncServiceConfig.SyncInstance> syncInstances = new TestConfigList<>()

        // Since we cannot fake 2 different bundles using the same config class due to running with
        // the same ClassLoader we have to make the same bundle publish 2 instances.

        SyncServiceConfig.SyncInstance syncInstance = new SyncServiceConfig.SyncInstance()
        syncInstance.name = new TestConfigValue(value: "test-sync-1")
        syncInstance.messageInstanceName = new TestConfigValue(value: "test-cluster")
        syncInstances.configs.add(syncInstance)

        syncInstance = new SyncServiceConfig.SyncInstance()
        syncInstance.name = new TestConfigValue(value: "test-sync-2")
        syncInstance.messageInstanceName = new TestConfigValue(value: "test-cluster")
        syncInstances.configs.add(syncInstance)

        syncServiceConfig.instances = syncInstances

        SyncServiceConfig.managed.serviceProviderAPI.configInstance = syncServiceConfig
        SyncServiceConfig.managed.serviceProviderAPI.setManaged()

        syncServiceBundleActivator = new APSActivator()
        syncServiceBundleActivator.start(syncServiceBundle.bundleContext)

    }

    @After
    public void shutdown() {
        syncServiceBundleActivator.stop(syncServiceBundle.bundleContext)
        messageServiceBundleActivator.stop(messageServiceBundle.bundleContext)
    }

    @Test
    public void runTest() throws Exception {
        APSServiceTracker<APSSyncService> sync1ServiceTracker =
                new APSServiceTracker<>(syncServiceBundle.bundleContext, APSSyncService.class,
                "(" + APSSyncService.SYNC_INSTANCE_NAME + "=test-sync-1)", "10 seconds")
        sync1ServiceTracker.start()

        APSServiceTracker<APSSyncService> sync2ServiceTracker =
                new APSServiceTracker<>(syncServiceBundle.bundleContext, APSSyncService.class,
                "(" + APSSyncService.SYNC_INSTANCE_NAME + "=test-sync-2)", "10 seconds")
        sync2ServiceTracker.start()

        APSSyncService syncSvc1 = sync1ServiceTracker.allocateService()
        APSSyncService syncSvc2 = sync2ServiceTracker.allocateService()

        SyncProps sp1 = new SyncProps(syncSvc1)

        SyncProps sp2 = new SyncProps(syncSvc2)

        sp1.setProperty("fruit", "pear")
        assertTrue("fruit pear not in sp2!", sp1.getProperty("fruit").equals(sp2.getProperty("fruit")))

        sync1ServiceTracker.releaseService()
        sync2ServiceTracker.releaseService()
    }

    public static void main(String[] args) {
        APSSyncServiceProviderTest sspt = new APSSyncServiceProviderTest()
        sspt.setup()
        sspt.runTest()
        sspt.shutdown()
    }
}
