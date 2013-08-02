/*
 *
 * PROJECT
 *     Name
 *         APS Configuration Service Provider
 *     
 *     Code Version
 *         0.9.2
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
import se.natusoft.osgi.aps.api.core.config.APSConfig;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigDescription;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigItemDescription;
import se.natusoft.osgi.aps.api.core.config.annotation.APSDefaultValue;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigList;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValueList;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigAdmin;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigEditModel;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigEnvironment;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigValueEditModel;
import se.natusoft.osgi.aps.api.core.config.service.APSConfigAdminService;
import se.natusoft.osgi.aps.api.core.config.service.APSConfigAdminService.APSConfigEnvAdmin;
import se.natusoft.osgi.aps.api.core.config.service.APSConfigService;
import se.natusoft.osgi.aps.api.core.filesystem.model.APSFilesystem;
import se.natusoft.osgi.aps.core.config.service.APSConfigAdminServiceProvider;
import se.natusoft.osgi.aps.core.config.service.APSConfigServiceProvider;
import se.natusoft.osgi.aps.core.config.store.APSConfigEnvStore;
import se.natusoft.osgi.aps.core.config.store.APSConfigMemoryStore;
import se.natusoft.osgi.aps.core.config.store.APSConfigPersistentStore;
import se.natusoft.osgi.aps.core.config.store.APSFileTool;
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
public class APSConfigServiceTests {

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
    /** The client config model. */
    private APSDiscoveryServiceConfig config = null;

    // This is created in setupEditing() and are only available to those tests that call setupEditing().
    /** The editing config model. */
    private APSConfigEditModel discoverySvcConfigModel = null;

    /**
     * Tests the APSConfigEnvStore class.
     */
    public APSConfigServiceTests() {
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
    @Before
    public void setUp() throws Exception {
        File projRoot = new File("target");
        File testFs = new File(projRoot, "testfs");
        testFs.mkdirs();
        APSFilesystem fs = new APSFilesystemImpl(testFs.getAbsolutePath(), "APSConfigEnvStoreTest");
        APSFileTool fileTool = new APSFileTool(fs);

        APSLogger logger = new APSLogger(System.out);
        APSConfigMemoryStore memStore = new APSConfigMemoryStore();
        APSConfigEnvStore envStore = new APSConfigEnvStore(fileTool);
        APSConfigPersistentStore configStore = new APSConfigPersistentStore(fileTool, envStore, logger);

        this.configService = new APSConfigServiceProvider(logger, null, memStore, envStore, configStore);
        this.configAdminService = new APSConfigAdminServiceProvider(logger, memStore, envStore, configStore);

        this.configEnv = this.configAdminService.getConfigEnvAdmin().getActiveConfigEnvironment();
    }

    /**
     * This should be the first thing any tests calls.
     *
     * @param name The name of the test.
     */
    private void start(String name) {
        System.out.print("Running " + name + "...");
    }

    /**
     * This should be the last thing any test calls.
     */
    private void end() {
        System.out.println("ok");
    }

//    @After
//    public void tearDown() {
//    }


    /**
     * Tests the default values that you get without editing anything. These are specified with the @APSDefaultValue
     * in the configuration class.
     *
     * @throws Exception on failure.
     */
    @Test
    public void testDefaultValues() throws Exception {
        start("testDefaultValues");

        this.configService.registerConfiguration(APSDiscoveryServiceConfig.class, false);
        APSDiscoveryServiceConfig config = this.configService.getConfiguration(APSDiscoveryServiceConfig.class);

        assertEquals("228.31.32.33", config.multicastAddress.toString());
        assertEquals("14000", config.multicastPort.toString());
        assertEquals("auto", config.udpLocalListenAddress.toString());
        assertEquals("14001", config.udpLocalListenPort.toString());
        assertEquals("10", config.consecutiveReadFailureLimit.toString());

        end();
    }

    /**
     * Requirements for all "testEditing*" tests. Should be the second thing called after start(...).
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

    /**
     * Tests editing plain config values (APSConfigValue).
     *
     * @throws Exception on any failure.
     */
    @Test
    public void testEditingValues() throws Exception {
        start("testEditingValues");

        setupEditing();

        assertEquals("228.31.32.33", config.multicastAddress.toString());
        configAdmin.setConfigValue(discoverySvcConfigModel.getValueByName("multicastAddress"), "192.168.1.5", configEnv);
        assertEquals("192.168.1.5", config.multicastAddress.toString());

        configAdmin.setConfigValue(discoverySvcConfigModel.getValueByName("multicastPort"), "15000", configEnv);
        assertEquals("15000", config.multicastPort.toString());

        end();
    }

    /**
     * Tests editing lists of plain config values (APSConfigValueList).
     *
     * @throws Exception on any failure.
     */
    @Test
    public void testEditingValueLists() throws Exception {
        start("testEditingValueLists");

        setupEditing();

        APSConfigEditModel udpRemoteDestConfigModel = (APSConfigEditModel)discoverySvcConfigModel.getValueByName("defaultUDPTargetDiscoveryService");
        assertNotNull(udpRemoteDestConfigModel);
        assertEquals(2, udpRemoteDestConfigModel.getValues().size());

        APSConfigValueEditModel targetPortConfigValue = udpRemoteDestConfigModel.getValueByName("targetPort");
        assertNotNull(targetPortConfigValue);
        assertEquals(true, targetPortConfigValue.isMany());

        assertEquals(0, config.defaultUDPTargetDiscoveryService.targetPort.size());
        configAdmin.addConfigValue(targetPortConfigValue, "1234", configEnv);
        configAdmin.addConfigValue(targetPortConfigValue, "5678", configEnv);
        configAdmin.addConfigValue(targetPortConfigValue, "13579", configEnv);
        assertEquals(3, config.defaultUDPTargetDiscoveryService.targetPort.size());

        assertEquals("1234", config.defaultUDPTargetDiscoveryService.targetPort.get(0).toString());
        assertEquals("5678", config.defaultUDPTargetDiscoveryService.targetPort.get(1).toString());
        assertEquals("13579", config.defaultUDPTargetDiscoveryService.targetPort.get(2).toString());

        try {
            config.defaultUDPTargetDiscoveryService.targetPort.get(3);
            fail("An IndexOutOfBoundsException was expected here since we referenced the 4th out of 3 elements!");
        }
        catch (IndexOutOfBoundsException iobe) {}
        catch (Exception e) {
            fail("Expected an IndexOutOfBoundsException, but received an " + e.getClass().getSimpleName() + " exception!");
        }

        // Test of remove.
        configAdmin.removeConfigValue(targetPortConfigValue, 1, configEnv);
        assertEquals(2, config.defaultUDPTargetDiscoveryService.targetPort.size());
        assertEquals("1234", config.defaultUDPTargetDiscoveryService.targetPort.get(0).toString());
        assertEquals("13579", config.defaultUDPTargetDiscoveryService.targetPort.get(1).toString());

        end();
    }

    /**
     * Tests editing lists of sub config classes (APSConfigList).
     *
     * @throws Exception on any failure.
     */
    @Test
    public void testEditingConfigLists() throws Exception {
        start("testEditingConfigLists");

        setupEditing();

        APSConfigEditModel udpTargetDiscoverySvcsEditModel = (APSConfigEditModel)discoverySvcConfigModel.getValueByName("udpTargetDiscoveryServices");
        assertNotNull(udpTargetDiscoverySvcsEditModel);
        assertEquals(true, udpTargetDiscoverySvcsEditModel.isMany());

        // Editing lists of sub config classes differs from editing lists of values. Values are values and nothing more and the key
        // for a list value can be determined by taking the key of the model and adding the index to it (which is done internally).
        // Lists of config classes however have a set of values after the index and possible more lists under it also. Each APSConfigModel
        // uses a base key and then adds keys for each value to the base to get a value key. Therefore we handle lists of configs by
        // creating a new APSConfigModel for each list entry that represents that specific entry and under the surface has a key with
        // a specific index. This model can then only be used for accessing that specific entry in the list. This is what is happening
        // on the next line below. newUDPTargetDiscoverySvcModel can only be used to modify that entry. To create a new entry the
        // original udpTargetDiscoverySvcModel must be used.
        APSConfigEditModel newUPDTargetDiscoverySvcEditModel1 = configAdmin.createConfigListEntry(udpTargetDiscoverySvcsEditModel, configEnv);
        assertEquals(1, config.udpTargetDiscoveryServices.size());

        APSConfigValueEditModel tdsTargetHostEditModel = newUPDTargetDiscoverySvcEditModel1.getValueByName("targetHost");
        configAdmin.setConfigValue(tdsTargetHostEditModel, "testhost", configEnv);
        assertEquals("testhost", config.udpTargetDiscoveryServices.get(0).targetHost.toString());

        APSConfigValueEditModel tdsTargetPortEditModel1 = newUPDTargetDiscoverySvcEditModel1.getValueByName("targetPort");
        configAdmin.addConfigValue(tdsTargetPortEditModel1, "1234", configEnv);
        configAdmin.addConfigValue(tdsTargetPortEditModel1, "5678", configEnv);
        // Test fetching values through the editing api.
        assertEquals("1234", configAdmin.getConfigValue(tdsTargetPortEditModel1, 0, configEnv));
        assertEquals("5678", configAdmin.getConfigValue(tdsTargetPortEditModel1, 1, configEnv));
        assertEquals(2, configAdmin.getSize(tdsTargetPortEditModel1, configEnv));
        // Test fetching values through the client config api.
        assertEquals("1234", config.udpTargetDiscoveryServices.get(0).targetPort.get(0).toString());
        assertEquals("5678", config.udpTargetDiscoveryServices.get(0).targetPort.get(1).toString());
        assertEquals(2, config.udpTargetDiscoveryServices.get(0).targetPort.size());

        // Second entry
        APSConfigEditModel newUPDTargetDiscoverySvcEditModel2 = configAdmin.createConfigListEntry(udpTargetDiscoverySvcsEditModel, configEnv);
        assertEquals(2, config.udpTargetDiscoveryServices.size());

        APSConfigValueEditModel tdsTargetHostEditModel2 = newUPDTargetDiscoverySvcEditModel2.getValueByName("targetHost");
        configAdmin.setConfigValue(tdsTargetHostEditModel2, "testhost2", configEnv);
        assertEquals("testhost2", config.udpTargetDiscoveryServices.get(1).targetHost.toString());

        APSConfigValueEditModel tdsTargetPortEditModel2 = newUPDTargetDiscoverySvcEditModel2.getValueByName("targetPort");
        configAdmin.addConfigValue(tdsTargetPortEditModel2, "12345", configEnv);
        configAdmin.addConfigValue(tdsTargetPortEditModel2, "56789", configEnv);
        // Test fetching values through the editing api.
        assertEquals("12345", configAdmin.getConfigValue(tdsTargetPortEditModel2, 0, configEnv));
        assertEquals("56789", configAdmin.getConfigValue(tdsTargetPortEditModel2, 1, configEnv));
        assertEquals(2, configAdmin.getSize(tdsTargetPortEditModel2, configEnv));
        // Test fetching values through the client config api.
        assertEquals("12345", config.udpTargetDiscoveryServices.get(1).targetPort.get(0).toString());
        assertEquals("56789", config.udpTargetDiscoveryServices.get(1).targetPort.get(1).toString());
        assertEquals(2, config.udpTargetDiscoveryServices.get(1).targetPort.size());

        // Now we will remove the first entry and verify that the second is still available at index 0.
        configAdmin.removeConfigListEntry(udpTargetDiscoverySvcsEditModel, 0, configEnv);
        assertEquals(1, config.udpTargetDiscoveryServices.size());
        assertEquals("12345", config.udpTargetDiscoveryServices.get(0).targetPort.get(0).toString());
        assertEquals("56789", config.udpTargetDiscoveryServices.get(0).targetPort.get(1).toString());
        assertEquals(2, config.udpTargetDiscoveryServices.get(0).targetPort.size());

        end();
    }

    @Test
    public void testDefaultValuesAndConfigEnvs() throws Exception {
        start("testDefaultValuesAndConfigEnvs");

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

        end();
    }

    // Template
//    @Test
//    public void test() throws Exception {
//        start("");
//
//        setupEditing();
//
//
//
//        end();
//    }

    //
    // Test Data
    //

    // This is the actual config for the aps-discovery-service-provider with some additions for testing purposes.
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
         * This is a sub config model of APSDiscoveryServiceConfig!
         */
        @APSConfigDescription(
                configId="se.natusoft.osgi.aps.discovery.static.services",
                description="Address and targetPort for known discovery service instance at static address and port outside of local net where multicast doesn't work.",
                version="1.0.0"
        )
        public static class APSUDPRemoteDestinationDiscoveryServiceConfig extends APSConfig {

            @APSConfigItemDescription(description = "The targetHost where a known discovery service runs.")
            public APSConfigValue targetHost;

            @APSConfigItemDescription(description = "The targetPort where the known discovery service listens to.")
            public APSConfigValueList targetPort; // Made this a list just for testing purposes!.
        }
    }

    @APSConfigDescription(
            configId="conf.env.test.configenv.test",
            description = "For testing config environments",
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
