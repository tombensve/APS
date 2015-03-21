package se.natusoft.osgi.aps.tools;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import se.natusoft.osgi.aps.net.messaging.models.config.TestConfigList;
import se.natusoft.osgi.aps.net.messaging.models.config.TestConfigValue;
import se.natusoft.osgi.aps.test.tools.OSGIServiceTestTools;
import se.natusoft.osgi.aps.test.tools.TestBundle;
import se.natusoft.osgi.aps.tools.config.ClusterServiceConfig;
import se.natusoft.osgi.aps.tools.services.TestService;

/**
 *
 */
public class APSActivatorConfiguredInstanceTest {

    @Before
    public void setup() throws Exception {
        ClusterServiceConfig clusterServiceConfig = new ClusterServiceConfig();
        TestConfigList<ClusterServiceConfig.Cluster> tcvClusters = new TestConfigList<>();
        clusterServiceConfig.clusters = tcvClusters;

        ClusterServiceConfig.Cluster cluster = new ClusterServiceConfig.Cluster();
        cluster.multicastDiscovery = createTestConfigValue("false");
        cluster.name = createTestConfigValue("syncCluster");
        cluster.tcpMembers = new TestConfigList<>();
        tcvClusters.getConfigs().add(cluster);

        cluster = new ClusterServiceConfig.Cluster();
        cluster.multicastDiscovery = createTestConfigValue("false");
        cluster.name = createTestConfigValue("msgCluster");
        cluster.tcpMembers = new TestConfigList<>();
        tcvClusters.getConfigs().add(cluster);

        ClusterServiceConfig.managed.serviceProviderAPI.setConfigInstance(clusterServiceConfig);
        ClusterServiceConfig.managed.serviceProviderAPI.setManaged();
    }

    private TestConfigValue createTestConfigValue(String value) {
        TestConfigValue tcv = new TestConfigValue();
        tcv.setValue(value);
        return tcv;
    }

    @Test
    public void testConfiguredInstance() throws Exception {
        OSGIServiceTestTools testTools = new OSGIServiceTestTools();
        TestBundle testBundle = testTools.createBundle("test-bundle");
        testBundle.addEntryPaths(
                "/se/natusoft/osgi/aps/tools/services/ConfiguredInstanceTestService.class"
        );

        APSActivator activator = new APSActivator();
        activator.start(testBundle.getBundleContext());

        APSServiceTracker<TestService> syncSvcTracker =
                new APSServiceTracker<TestService>(
                        testBundle.getBundleContext(),
                        TestService.class,
                        "(" + APSActivator.SERVICE_INSTANCE_NAME + "=syncCluster)",
                        "5 seconds"
                );
        syncSvcTracker.start();
        APSServiceTracker<TestService> msgSvcTracker =
                new APSServiceTracker<TestService>(
                        testBundle.getBundleContext(),
                        TestService.class,
                        "(" + APSActivator.SERVICE_INSTANCE_NAME + "=msgCluster)",
                        "5 seconds"
                );
        msgSvcTracker.start();

        TestService syncSvc = syncSvcTracker.allocateService();
        TestService msgSvc = msgSvcTracker.allocateService();

        try {
            assertEquals("Got wrong service instance!", "syncCluster", syncSvc.getServiceInstanceInfo());
            assertEquals("Got wrong service instance!", "msgCluster", msgSvc.getServiceInstanceInfo());
        }
        finally {
            syncSvcTracker.releaseService();
            msgSvcTracker.releaseService();

            syncSvcTracker.stop(testBundle.getBundleContext());
            msgSvcTracker.stop(testBundle.getBundleContext());
        }

    }
}
