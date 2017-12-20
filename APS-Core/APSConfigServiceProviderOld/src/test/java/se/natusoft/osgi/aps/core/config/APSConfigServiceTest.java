/*
 *
 * PROJECT
 *     Name
 *         APS Configuration Service Provider
 *
 *     Code Version
 *         1.0.0
 *
 *     Description
 *         A more advanced configuration service that uses annotated interfaces to
 *         describe and provide access to configuration. It supports structured
 *         configuration models.
 *
 * COPYRIGHTS
 *     Copyright (C) 2012 by Natusoft AB All rights reserved.
 *
 * LICENSE
 *     Apache 2.0 (Open Source)
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 * AUTHORS
 *     tommy ()
 *         Changes:
 *         2011-08-13: Created!
 *
 */
package se.natusoft.osgi.aps.core.config;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import se.natusoft.osgi.aps.api.core.configold.service.APSConfigService;
import se.natusoft.osgi.aps.api.core.configold.service.APSConfigAdminService;
import se.natusoft.osgi.aps.api.core.configold.service.APSConfigAdminService.APSConfigEnvAdmin;
import se.natusoft.osgi.aps.api.core.configold.model.admin.*;
import se.natusoft.osgi.aps.api.core.configold.*;
import se.natusoft.osgi.aps.api.core.configold.annotation.*;
import se.natusoft.osgi.aps.api.core.configold.model.*;
import se.natusoft.osgi.aps.api.core.filesystem.model.*;
import se.natusoft.osgi.aps.core.config.service.APSConfigAdminServiceProvider;
import se.natusoft.osgi.aps.core.config.service.APSConfigServiceProvider;
import se.natusoft.osgi.aps.core.config.store.APSConfigEnvStore;
import se.natusoft.osgi.aps.core.config.store.APSConfigMemoryStore;
import se.natusoft.osgi.aps.core.config.store.APSConfigPersistentStore;
import se.natusoft.osgi.aps.core.config.store.APSConfigServiceFileTool;
import se.natusoft.osgi.aps.core.test.support.filesystem.model.APSFilesystemImpl;
import se.natusoft.osgi.aps.tools.APSLogger;

import java.io.File;

import static org.junit.Assert.*;

// PLEASE NOTE: For some incomprehensible reason maven refuses to accept this as a JUnit test!
// It is thus never run during build!
/**
 * These tests uses the actual service APIs and thus tests the functionality as it would
 * work in reality.
 */
public class APSConfigServiceTest {

    // This is created in setUp() and are available to all tests.
    /** The client configuration service. */
    private APSConfigService configService = null;

    // This is created in setUp() and are available to all tests.
    /** The editing configuration service. */
    private APSConfigAdminService configAdminService = null;

    // This is created in setUp() and are available to all tests.
    /** The active configuration environment. */
    private APSConfigEnvironment configEnv = null;

    // This is created in setupEditing() and are only available to those tests that call setupEditing().
    /** The admin object representing the entire configuration. Gotten from configAdminService. */
    private APSConfigAdmin configAdmin = null;

    // This is created in setupEditing() and are only available to those tests that call setupEditing().
    /** The client configold model. */
    private APSDiscoveryServiceConfig config = null;

    // This is created in setupEditing() and are only available to those tests that call setupEditing().
    /** The editing configold model. */
    private APSConfigEditModel discoverySvcConfigModel = null;

    /**
     * Tests the APSConfigEnvStore class.
     */
    public APSConfigServiceTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Sets up a filesystem under target.
     *
     * @throws Exception on failure.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Before
    public void setUp() throws Exception {
        File projRoot = new File("target");
        File testFs = new File(projRoot, "testfs");
        testFs.mkdirs();
        APSFilesystem fs = new APSFilesystemImpl(testFs.getAbsolutePath(), "APSConfigEnvStoreTest");
        APSConfigServiceFileTool fileTool = new APSConfigServiceFileTool(fs);

        APSLogger logger = new APSLogger(System.out);
        APSConfigMemoryStore memStore = new APSConfigMemoryStore();
        APSConfigEnvStore envStore = new APSConfigEnvStore(fileTool);
        APSConfigPersistentStore configStore = new APSConfigPersistentStore(fileTool, envStore, logger);

        this.configService = new APSConfigServiceProvider(logger, null, memStore, envStore, configStore);
        this.configAdminService = new APSConfigAdminServiceProvider(logger, memStore, envStore, configStore);

        this.configEnv = this.configAdminService.getConfigEnvAdmin().getActiveConfigEnvironment();
    }

    /**
     * Requirements for all "testEditing*" tests. Should be the second thing called after startTest(...).
     *
     * @throws Exception on failure.
     */
    private void setupEditing() throws Exception {
        this.configService.registerConfiguration(APSDiscoveryServiceConfig.class, false);
        this.config = this.configService.getConfiguration(APSDiscoveryServiceConfig.class);

        assertEquals(1, this.configAdminService.getAllConfigurationIds().size());
        assertEquals(1, this.configAdminService.getAllConfigurations().size());

        this.configAdmin = this.configAdminService.getConfiguration("se.natusoft.osgi.aps.discovery", "1.0.0");

        assertNotNull(this.configAdmin);

        this.discoverySvcConfigModel = configAdmin.getConfigModel();
        assertEquals("se.natusoft.osgi.aps.discovery", discoverySvcConfigModel.getConfigId());
        assertEquals("1.0.0", discoverySvcConfigModel.getVersion());
        assertEquals(7, discoverySvcConfigModel.getValues().size());
        assertEquals(7, discoverySvcConfigModel.getValueNames().size());

    }

    private String indent = "";

    private void startTest(String name) {
        System.out.print("Running " + name + "...");
    }

    private void startStructuredTest(String name) {
        System.out.println("Running" + name);
    }

    private void startGroup(String name) {
        indent += "   ";
        System.out.println(indent + name);
    }

    private void endGroup() {
        System.out.println(indent + "ok");
        indent = indent.substring(0, indent.length() - 3);
    }

    private void startSubTest(String name) {
        indent += "   ";
        System.out.print(indent + name + "...");
    }

    private void endSubTest() {
        indent = indent.substring(0, indent.length() - 3);
        endTest();
    }

    private void endTest() {
        System.out.println("ok");
    }

//    @After
//    public void tearDown() {
//    }

    //
    // Tests
    //

    /**
     * Tests the default values that you get without editing anything. These are specified with the @APSDefaultValue
     * in the configuration class.
     *
     * @throws Exception on failure.
     */
    @Test
    public void testDefaultValues() throws Exception {
        startTest("testDefaultValues");

        this.configService.registerConfiguration(APSDiscoveryServiceConfig.class, false);
        APSDiscoveryServiceConfig config = this.configService.getConfiguration(APSDiscoveryServiceConfig.class);

        assertEquals("228.31.32.33", config.multicastAddress.toString());
        assertEquals("14000", config.multicastPort.toString());
        assertEquals("auto", config.udpLocalListenAddress.toString());
        assertEquals("14001", config.udpLocalListenPort.toString());
        assertEquals("10", config.consecutiveReadFailureLimit.toString());

        endTest();
    }

    @Test
    public void testAPSConfig_lookup() throws Exception {
        startTest("testAPSConfig_lookup");

        setupEditing();

        APSConfigEditModel udpRemoteDestConfigModel =
                (APSConfigEditModel)discoverySvcConfigModel.getValueByName("defaultUDPTargetDiscoveryService");
        assertNotNull(udpRemoteDestConfigModel);
        assertEquals(2, udpRemoteDestConfigModel.getValues().size());

        APSConfigValueEditModel targetPortConfigValue = udpRemoteDestConfigModel.getValueByName("targetPort");
        APSConfigReference targetPortRef =
                configAdmin.createRef().__(discoverySvcConfigModel).__(udpRemoteDestConfigModel).__(targetPortConfigValue)
                        .__(configEnv);
        assertNotNull(targetPortConfigValue);
        assertEquals(true, targetPortConfigValue.isMany());

        assertEquals(0, config.defaultUDPTargetDiscoveryService.targetPort.size());
        configAdmin.addConfigValue(targetPortRef, "1234");
        configAdmin.addConfigValue(targetPortRef, "5678");
        configAdmin.addConfigValue(targetPortRef, "13579");
        assertEquals(3, config.defaultUDPTargetDiscoveryService.targetPort.size());

        APSConfigValueEditModel targetHostConfigValue = udpRemoteDestConfigModel.getValueByName("targetHost");
        APSConfigReference targetHostRef =
                configAdmin.createRef().__(discoverySvcConfigModel).__(udpRemoteDestConfigModel).__(targetHostConfigValue)
                        .__(configEnv);

        configAdmin.setConfigValue(targetHostRef, "my.test.host");

        Object cfo = this.config.lookup("defaultUDPTargetDiscoveryService.targetHost");
        assertNotNull(cfo);
        assertTrue("Expected an APSConfigValue instance. Got: '" + cfo.getClass().getSimpleName() + "'", APSConfigValue.class.isAssignableFrom(cfo.getClass()));
        assertEquals("my.test.host", cfo.toString());

        endTest();
    }

    /**
     * Tests editing plain configold values (APSConfigValue).
     *
     * @throws Exception on any failure.
     */
    @Test
    public void testEditingValues() throws Exception {
        startTest("testEditingValues");

        setupEditing();

        assertEquals("228.31.32.33", config.multicastAddress.toString());
        APSConfigReference discoverySvcRef = configAdmin.createRef()
                .__(discoverySvcConfigModel);
        APSConfigReference multicastAddressRef =
                discoverySvcRef.__(discoverySvcConfigModel.getValueByName("multicastAddress")).__(configEnv);
        configAdmin.setConfigValue(multicastAddressRef, "192.168.1.5");
        assertEquals("192.168.1.5", config.multicastAddress.toString());

        APSConfigReference multicastPortRef =
                discoverySvcRef.__(discoverySvcConfigModel.getValueByName("multicastPort")).__(configEnv);
        configAdmin.setConfigValue(multicastPortRef, "15000");
        assertEquals("15000", config.multicastPort.toString());

        endTest();
    }

    /**
     * Tests editing lists of plain configold values (APSConfigValueList).
     *
     * @throws Exception on any failure.
     */
    @Test
    public void testEditingValueLists() throws Exception {
        startTest("testEditingValueLists");

        setupEditing();

        APSConfigEditModel udpRemoteDestConfigModel =
                (APSConfigEditModel)discoverySvcConfigModel.getValueByName("defaultUDPTargetDiscoveryService");
        assertNotNull(udpRemoteDestConfigModel);
        assertEquals(2, udpRemoteDestConfigModel.getValues().size());

        APSConfigValueEditModel targetPortConfigValue = udpRemoteDestConfigModel.getValueByName("targetPort");
        APSConfigReference targetPortRef =
                configAdmin.createRef().__(discoverySvcConfigModel).__(udpRemoteDestConfigModel).__(targetPortConfigValue)
                        .__(configEnv);
        assertNotNull(targetPortConfigValue);
        assertEquals(true, targetPortConfigValue.isMany());

        assertEquals(0, config.defaultUDPTargetDiscoveryService.targetPort.size());
        configAdmin.addConfigValue(targetPortRef, "1234");
        configAdmin.addConfigValue(targetPortRef, "5678");
        configAdmin.addConfigValue(targetPortRef, "13579");
        assertEquals(3, config.defaultUDPTargetDiscoveryService.targetPort.size());

        assertEquals("1234", config.defaultUDPTargetDiscoveryService.targetPort.get(0).toString());
        assertEquals("5678", config.defaultUDPTargetDiscoveryService.targetPort.get(1).toString());
        assertEquals("13579", config.defaultUDPTargetDiscoveryService.targetPort.get(2).toString());

        try {
            config.defaultUDPTargetDiscoveryService.targetPort.get(3);
            fail("An IndexOutOfBoundsException was expected here since we referenced the 4th out of 3 elements!");
        }
        catch (IndexOutOfBoundsException iobe) {/*OK*/}
        catch (Exception e) {
            fail("Expected an IndexOutOfBoundsException, but received an " + e.getClass().getSimpleName() + " exception!");
        }

        // Test of remove.
        configAdmin.removeConfigValue(targetPortRef.index(1));
        assertEquals(2, config.defaultUDPTargetDiscoveryService.targetPort.size());
        assertEquals("1234", config.defaultUDPTargetDiscoveryService.targetPort.get(0).toString());
        assertEquals("13579", config.defaultUDPTargetDiscoveryService.targetPort.get(1).toString());

        endTest();
    }

    /**
     * Tests editing lists of sub configold classes (APSConfigList).
     *
     * @throws Exception on any failure.
     */
    @Test
    public void testEditingConfigLists() throws Exception {
        startStructuredTest("testEditingConfigLists");

        setupEditing();

        APSConfigEditModel udpTargetDiscoveryServicesEditModel = (APSConfigEditModel)discoverySvcConfigModel.getValueByName("udpTargetDiscoveryServices");
        assertNotNull(udpTargetDiscoveryServicesEditModel);
        assertEquals(true, udpTargetDiscoveryServicesEditModel.isMany());

        {
            startGroup("Create first list entry [0]");
            APSConfigReference udpTargetDiscoverySvcRef0 =
                    configAdmin.addConfigList(configAdmin.createRef().__(discoverySvcConfigModel).__(udpTargetDiscoveryServicesEditModel));
            {
                startSubTest("targetHost");
                {
                    APSConfigValueEditModel tdsTargetHostEditModel = udpTargetDiscoveryServicesEditModel.getValueByName("targetHost");
                    APSConfigReference tdsTargetHostRef = udpTargetDiscoverySvcRef0.__(tdsTargetHostEditModel).__(configEnv);
                    configAdmin.setConfigValue(tdsTargetHostRef, "testhost");
                    assertEquals("testhost", config.udpTargetDiscoveryServices.get(0).targetHost.toString());
                }
                endSubTest();

                startGroup("targetPort");
                {
                    startSubTest("Add target ports");
                    APSConfigValueEditModel tdsTargetPortEditModel = udpTargetDiscoveryServicesEditModel.getValueByName("targetPort");
                    APSConfigReference tdsTargetPortRef = udpTargetDiscoverySvcRef0.__(tdsTargetPortEditModel).__(configEnv);
                    configAdmin.addConfigValue(tdsTargetPortRef, "1234");
                    configAdmin.addConfigValue(tdsTargetPortRef, "5678");
                    endSubTest();

                    startSubTest("Verify fetching values through editing api");
                    assertEquals("1234", configAdmin.getConfigValue(tdsTargetPortRef.index(0)));
                    assertEquals("5678", configAdmin.getConfigValue(tdsTargetPortRef.index(1)));
                    assertEquals(2, configAdmin.getListSize(tdsTargetPortRef));
                    endSubTest();

                    startSubTest("Verify fetching values through the client configold api");
                    assertEquals("1234", config.udpTargetDiscoveryServices.get(0).targetPort.get(0).toString());
                    assertEquals("5678", config.udpTargetDiscoveryServices.get(0).targetPort.get(1).toString());
                    assertEquals(2, config.udpTargetDiscoveryServices.get(0).targetPort.size());
                    endSubTest();
                }
                endGroup();
            }
            endGroup();

            startGroup("Create second list entry [1]");
            APSConfigReference udpTargetDiscoverySvcRef1 =
                    configAdmin.addConfigList(configAdmin.createRef().__(discoverySvcConfigModel).__(udpTargetDiscoveryServicesEditModel));
            {
                startSubTest("targetHost");
                {
                    APSConfigValueEditModel tdsTargetHostEditModel = udpTargetDiscoveryServicesEditModel.getValueByName("targetHost");
                    APSConfigReference tdsTargetHostRef = udpTargetDiscoverySvcRef1.__(tdsTargetHostEditModel).__(configEnv);
                    configAdmin.setConfigValue(tdsTargetHostRef, "testhost2");
                    assertEquals("testhost2", config.udpTargetDiscoveryServices.get(1).targetHost.toString());
                }
                endSubTest();

                startGroup("targetPort");
                {
                    startSubTest("Add target ports");
                    APSConfigValueEditModel tdsTargetPortEditModel = udpTargetDiscoveryServicesEditModel.getValueByName("targetPort");
                    APSConfigReference tdsTargetPortRef = udpTargetDiscoverySvcRef1.__(tdsTargetPortEditModel).__(configEnv);
                    configAdmin.addConfigValue(tdsTargetPortRef, "12345");
                    configAdmin.addConfigValue(tdsTargetPortRef, "56789");
                    endSubTest();

                    startSubTest("Verify fetching values through editing api");
                    assertEquals("12345", configAdmin.getConfigValue(tdsTargetPortRef.index(0)));
                    assertEquals("56789", configAdmin.getConfigValue(tdsTargetPortRef.index(1)));
                    assertEquals(2, configAdmin.getListSize(tdsTargetPortRef));
                    endSubTest();

                    startSubTest("Verify fetching values through the client configold api");
                    assertEquals("12345", config.udpTargetDiscoveryServices.get(1).targetPort.get(0).toString());
                    assertEquals("56789", config.udpTargetDiscoveryServices.get(1).targetPort.get(1).toString());
                    assertEquals(2, config.udpTargetDiscoveryServices.get(1).targetPort.size());
                    endSubTest();

                }
                endGroup();
            }
            endGroup();

            startGroup("Test list deletion");
            {
                // Now we will remove the first entry and verify that the second is still available at index 0.
                startSubTest("Remove first entry and verify that the second is still available at index 0");
                {
                    configAdmin.removeConfigList(udpTargetDiscoverySvcRef0);
                    assertEquals(1, config.udpTargetDiscoveryServices.size());
                    assertEquals("12345", config.udpTargetDiscoveryServices.get(0).targetPort.get(0).toString());
                    assertEquals("56789", config.udpTargetDiscoveryServices.get(0).targetPort.get(1).toString());
                    assertEquals(2, config.udpTargetDiscoveryServices.get(0).targetPort.size());
                }
                endSubTest();

                startSubTest("Remove remaining entry and verify that #entries == 0");
                {
                    configAdmin.removeConfigList(udpTargetDiscoverySvcRef0);
                    assertEquals(0, config.udpTargetDiscoveryServices.size());
                }
                endSubTest();
            }
            endGroup();

            // Check that we can build a new reference from scratch.
            startSubTest("Verify that we can build a new reference from scratch matching the one received from addConfigList(...)");
            {
                APSConfigReference reCreatedRef = configAdmin.createRef().__(discoverySvcConfigModel).__(udpTargetDiscoveryServicesEditModel).index(0);
                assertEquals(udpTargetDiscoverySvcRef0.toString(), reCreatedRef.toString());
            }
            endSubTest();
        }

        endTest();
    }

    @Test
    public void testDefaultValuesAndConfigEnvs() throws Exception {
        startTest("testDefaultValuesAndConfigEnvs");

        APSConfigEnvAdmin configEnvAdmin = this.configAdminService.getConfigEnvAdmin();
        configEnvAdmin.addConfigEnvironment("devel", "Development");
        configEnvAdmin.addConfigEnvironment("systest", "System test");
        configEnvAdmin.addConfigEnvironment("acceptance", "Acceptance test");

        this.configService.registerConfiguration(ConfEnvTestConfig.class, false);
        ConfEnvTestConfig config = this.configService.getConfiguration(ConfEnvTestConfig.class);

        configEnvAdmin.selectActiveConfigEnvironment(configEnvAdmin.getConfigEnvironmentByName("devel"));
        assertEquals("qwerty", config.testValue1.toString());

        configEnvAdmin.selectActiveConfigEnvironment(configEnvAdmin.getConfigEnvironmentByName("systest"));
        assertEquals("qazwsx", config.testValue1.toString());

        configEnvAdmin.selectActiveConfigEnvironment(configEnvAdmin.getConfigEnvironmentByName("acceptance"));
        assertEquals("asdf", config.testValue1.toString());

        endTest();
    }

    // Template
//    @Test
//    public void test() throws Exception {
//        startTest("");
//
//        setupEditing();
//
//
//
//        endTest();
//    }

    //
    // Test Data
    //

    // This is the actual configold for the aps-discovery-service-provider with some additions for testing purposes.
    @APSConfigDescription(
            configId="se.natusoft.osgi.aps.discovery",
            description="Adresses and ports to use for discovery.",
            version="1.0.0"
    )
    public static class APSDiscoveryServiceConfig extends APSConfig {

        @APSConfigItemDescription(description = "The multicast address to use.", defaultValue = {@APSDefaultValue(value="228.31.32.33")})
        public APSConfigValue multicastAddress;

        @APSConfigItemDescription(description = "The multicast target port to use.", defaultValue = {@APSDefaultValue(value="14000")})
        public APSConfigValue multicastPort;

        @APSConfigItemDescription(description = "The UDP address to listen to. This is for enabling discovery between networks where multicast doesn't reach. " +
                "Defaults to \"auto\" which auto resolves the local address. " +
                "Specify an address if you have multiple network interfaces with different addresses and want to use one of those. " +
                "Set to blank or \"disable\" to disable!", defaultValue={@APSDefaultValue(value="auto")})
        public APSConfigValue udpLocalListenAddress;

        @APSConfigItemDescription(description = "The UDP targetPort to listen to.", defaultValue = {@APSDefaultValue(value="14001")})
        public APSConfigValue udpLocalListenPort;

        @APSConfigItemDescription(description = "The number of consecutive read failures allowed.", defaultValue = {@APSDefaultValue(value = "10")})
        public APSConfigValue consecutiveReadFailureLimit;

        @APSConfigItemDescription(description = "A list of target discovery services to announce ourself to.")
        public APSConfigList<APSUDPRemoteDestinationDiscoveryServiceConfig> udpTargetDiscoveryServices;

        @APSConfigItemDescription(description = "For testing purposes.")
        public APSUDPRemoteDestinationDiscoveryServiceConfig defaultUDPTargetDiscoveryService;

        /**
         * Configuration of static UDP discovery service instance elsewhere on the net
         * that services will be announced to.
         * <p/>
         * This is a sub configold model of APSDiscoveryServiceConfig!
         */
        @APSConfigDescription(
                configId="services",
                description="Address and targetPort for known discovery service instance at static address and port outside of local net where multicast doesn't work.",
                version="1.0.0"
        )
        public static class APSUDPRemoteDestinationDiscoveryServiceConfig extends APSConfig {

            @APSConfigItemDescription(description = "The targetHost where a known discovery service runs.", defaultValue = {@APSDefaultValue(value = "testHost")})
            public APSConfigValue targetHost;

            @APSConfigItemDescription(description = "The targetPort where the known discovery service listens to.")
            public APSConfigValueList targetPort; // Made this a list just for testing purposes!.
        }
    }

    @APSConfigDescription(
            configId="conf.env.test.configenv.test",
            description = "For testing configold environments",
            version="1.0.0"
    )
    public static class ConfEnvTestConfig extends APSConfig {

        @APSConfigItemDescription(
                description = "For test",
                environmentSpecific = true,
                defaultValue = {
                        @APSDefaultValue(value="qwerty", configEnv = "devel"),
                        @APSDefaultValue(value="qazwsx", configEnv = "systest"),
                        @APSDefaultValue(value="asdf", configEnv = "acceptance")
                }
        )
        public APSConfigValue testValue1;

    }

}
