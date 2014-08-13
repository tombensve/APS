/*
 *
 * PROJECT
 *     Name
 *         APS Configuration Service Provider
 *     
 *     Code Version
 *         0.11.0
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
package se.natusoft.osgi.aps.core.config.service.store;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.service.cm.ManagedService;
import se.natusoft.osgi.aps.api.core.config.APSConfig;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigDescription;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigItemDescription;
import se.natusoft.osgi.aps.api.core.config.annotation.APSDefaultValue;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigList;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigEditModel;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigEnvironment;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigReference;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigValueEditModel;
import se.natusoft.osgi.aps.core.config.model.APSConfigInstanceMemoryStoreImpl;
import se.natusoft.osgi.aps.core.config.model.APSConfigObjectFactory;
import se.natusoft.osgi.aps.core.config.model.admin.APSConfigAdminImpl;
import se.natusoft.osgi.aps.core.config.model.admin.APSConfigEditModelImpl;
import se.natusoft.osgi.aps.core.config.model.admin.APSConfigEnvironmentImpl;
import se.natusoft.osgi.aps.core.config.store.APSConfigEnvStore;
import se.natusoft.osgi.aps.core.config.store.APSConfigPersistentStore;
import se.natusoft.osgi.aps.core.config.store.APSFileTool;
import se.natusoft.osgi.aps.core.test.support.filesystem.model.APSFilesystemImpl;
import se.natusoft.osgi.aps.tools.APSLogger;

import java.io.File;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests the APSConfigPersistentStore class.
 * <p/>
 * If you wonder about all the javadoc which of some are empty is is because I have missing
 * javadoc as an error in my IDE. In general I want that, but for tests it is not always relevant,
 * but not to have a lot of error markers I have to make the IDE happy.
 */
public class APSConfigPersistentStoreTest {

    /** This is needed for the tests. */
    private APSConfigEnvStore envStore = null;

    /** This is needed by the APSConfigPersistentStore. */
    private APSFileTool fileTool = null;

    /**
     * Creates a new APSConfigPersistentStoreTest.
     */
    public APSConfigPersistentStoreTest() {
    }

//    @BeforeClass
//    public static void setUpClass() throws Exception {
//    }
//
//    @AfterClass
//    public static void tearDownClass() throws Exception {
//    }

    /**
     * Setup for the test.
     *
     * @throws Exception on any failure.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Before
    public void setUp() throws Exception {
        File projRoot = new File("target");
        File testFs = new File(projRoot, "testfs");
        testFs.mkdirs();
        APSFilesystemImpl fs = new APSFilesystemImpl(testFs.getAbsolutePath(), "APSConfigPersistentStoreTest");
        this.fileTool = new APSFileTool(fs);
        this.envStore = new APSConfigEnvStore(fileTool);
        this.envStore.removeAllEnvironments();
        this.envStore.addEnvironment(new APSConfigEnvironmentImpl("devel", "The development configuration environment", 0));
        this.envStore.addEnvironment(new APSConfigEnvironmentImpl("systest", "The system test configuration environment", 0));
        this.envStore.setActiveConfigEnvironment(this.envStore.getConfigEnvironments().get(0));
    }

    /**
     * Cleanup after the test.
     */
    @After
    public void tearDown() {
        this.envStore = null;
        this.fileTool = null;
    }

    /**
     * @return a new APSConfigPersistentStore.
     * @throws Exception on failure.
     */
    private APSConfigPersistentStore createConfigStore() throws Exception {
        return new APSConfigPersistentStore(fileTool, this.envStore, new APSLogger(System.out));
    }

    /**
     * @return A test configuration.
     *
     * @throws Exception on failure.
     */
    private APSConfigAdminImpl createConfig1() throws Exception {
        Properties confProps = new Properties();
        APSConfigInstanceMemoryStoreImpl configValueStore = new APSConfigInstanceMemoryStoreImpl(confProps);
        APSConfigEnvironment configEnvironment = this.envStore.getConfigEnvironmentByName("devel");
        APSConfigObjectFactory configObjectFactory = new APSConfigObjectFactory(envStore, configValueStore);

        APSConfigEditModel configModel = new APSConfigEditModelImpl(MyConfig.class, configObjectFactory);
        APSConfigAdminImpl config = new APSConfigAdminImpl(configModel, configValueStore, envStore);

        APSConfigValueEditModel southernComfort = configModel.getValueByName("southerncomfort");
        APSConfigReference southernComfortRef = config.createRef()._(configModel)._(southernComfort)._(configEnvironment);
        config.setConfigValue(southernComfortRef, "false");

        return config;
    }

    /**
     * @return another test configuration.
     * @throws Exception on failure.
     */
    private APSConfigAdminImpl createConfig2() throws Exception {
        Properties confProps = new Properties();
        APSConfigInstanceMemoryStoreImpl configValueStore = new APSConfigInstanceMemoryStoreImpl(confProps);
        APSConfigEnvironment configEnvironment = this.envStore.getConfigEnvironmentByName("devel");
        APSConfigObjectFactory configObjectFactory = new APSConfigObjectFactory(this.envStore, configValueStore);

        APSConfigEditModel configModel = new APSConfigEditModelImpl(MyConfig2.class, configObjectFactory);
        APSConfigAdminImpl config = new APSConfigAdminImpl(configModel, configValueStore, envStore);

        return config;
    }

    /**
     * Creates the 2 configurations and stores them in an APSConfigPersistentStore.
     *
     * @throws Exception on failure.
     */
    private void createConfigurations() throws Exception {
        APSConfigPersistentStore configStore = createConfigStore();
        configStore.saveConfiguration(createConfig1());
        configStore.saveConfiguration(createConfig2());
    }

    /**
     * Test of getAvailableConfigurationIds method, of class APSConfigPersistentStore.
     *
     * @throws Exception on failure.
     */
    @Test
    public void testGetAvailableConfigurationIds() throws Exception  {
        System.out.print("getAvailableConfigurationIds...");
        createConfigurations();

        APSConfigPersistentStore configStore = createConfigStore();
        Set<String> configIds = configStore.getAvailableConfigurationIds();

        assertEquals(1, configIds.size());
        assertEquals("se.natusoft.osgi.aps.core.config.service.MyConfig", configIds.iterator().next());

        System.out.println("ok");
    }

    /**
     * Test of getAvailableVersions method, of class APSConfigPersistentStore.
     *
     * @throws Exception on failure.
     */
    @Test
    public void testGetAvailableVersions() throws Exception {
        System.out.print("getAvailableVersions..");
        createConfigurations();

        APSConfigPersistentStore configStore = createConfigStore();
        String configId = configStore.getAvailableConfigurationIds().iterator().next();
        List<String> versions = configStore.getAvailableVersions(configId);

        assertEquals(2, versions.size());
        assertEquals("1.0", versions.get(0).toString());
        assertEquals("1.1", versions.get(1).toString());

        System.out.println("ok");
    }

    /**
     * Test of loadConfiguration method, of class APSConfigPersistentStore.
     *
     * @throws Exception on failure.
     */
    @Test
    public void testLoadConfiguration_Class() throws Exception {
        System.out.print("loadConfiguration(schema)...");
        createConfigurations();

        APSConfigPersistentStore configStore = createConfigStore();
        APSConfigAdminImpl config = configStore.loadConfiguration(MyConfig2.class);

        assertNotNull(config);
        APSConfigEditModel otherChoicesEditModel = (APSConfigEditModel)config.getConfigModel().getValueByName("otherchoices");
        APSConfigValueEditModel otherFishEditModel = otherChoicesEditModel.getValueByName("otherfish");
        APSConfigReference otherFishRef = config.createRef()._(config.getConfigModel())._(otherChoicesEditModel)._(otherFishEditModel);

        // Please note that we actually get the default value back here since we have not set this to anything.
        assertEquals("false", config.getConfigValue(otherFishRef._(this.envStore.getActiveConfigEnvironment())));

        System.out.println("ok");
    }

    /**
     * Test values.
     *
     * @throws Exception on failure.
     */
    @Test
    public void testValues() throws Exception {
        System.out.print("values...");
        createConfigurations();

        APSConfigPersistentStore configStore = createConfigStore();
        APSConfigAdminImpl config = configStore.loadConfiguration(MyConfig2.class);

        assertNotNull(config);
        APSConfigEditModel otherChoicesEditModel = (APSConfigEditModel)config.getConfigModel().getValueByName("otherchoices");
        APSConfigValueEditModel otherFishEditModel = otherChoicesEditModel.getValueByName("otherfish");
        APSConfigReference otherFishRef = config.createRef()._(config.getConfigModel())._(otherChoicesEditModel)._(otherFishEditModel);

        config.setConfigValue(otherFishRef._(this.envStore.getActiveConfigEnvironment()), "true");

        MyConfig2 mc2 = ((APSConfigEditModelImpl<MyConfig2>)config.getConfigModel()).getInstance();
        try {((ManagedService)mc2).updated(config.getConfigInstanceMemoryStore().getProperties());} catch (Exception e) {}

        assertEquals("true", mc2.otherChoices.otherFish.toString());

        System.out.println("ok");
    }

    //
    // Test Data
    //

    @APSConfigDescription(
            version="1.0",
            configId="se.natusoft.osgi.aps.core.config.service.MySubConfig",
            description="Test of sub config model."
    )
    public static class MySubConfig extends APSConfig {

        /** */
        @APSConfigItemDescription(description="Do you like pizza ?", defaultValue={@APSDefaultValue(value="true", configEnv="devel")})
        public APSConfigValue pizza;

        /** */
        @APSConfigItemDescription(description="Do you like salmon ?", defaultValue={@APSDefaultValue(value="false", configEnv="devel")})
        public APSConfigValue salmon;
    }

    @APSConfigDescription(
            version="1.0",
            configId="se.natusoft.osgi.aps.core.config.service.MyConfig",
            description="Test of sub config model."
    )
    public static class MyConfig extends APSConfig {

        /** */
        @APSConfigItemDescription(description="Do you like Southern Confort ?", defaultValue={@APSDefaultValue(value="true", configEnv="devel")})
        public APSConfigValue southernComfort;

        /** */
        @APSConfigItemDescription(description="Some other choices")
        public MySubConfig otherChoices;

        /** */
        @APSConfigItemDescription(description="More choices")
        public APSConfigList<MySubConfig> moreChoices;

    }

    @APSConfigDescription(
            version="1.1",
            configId="se.natusoft.osgi.aps.core.config.service.MySubConfig",
            description="Test of sub config model."
    )
    public static class MySubConfig2 extends APSConfig {

        /** */
        @APSConfigItemDescription(description="Do you like pizza ?", defaultValue={@APSDefaultValue(value="true", configEnv="devel")})
        public APSConfigValue pizza;

        /** */
        @APSConfigItemDescription(description="Do you like salmon ?", defaultValue={@APSDefaultValue(value="false", configEnv="devel")})
        public APSConfigValue salmon;

        /** */
        @APSConfigItemDescription(description="Do you like other fish ?", defaultValue={@APSDefaultValue(value="false", configEnv="devel")})
        public APSConfigValue otherFish;
    }

    @APSConfigDescription(
            version="1.1",
            configId="se.natusoft.osgi.aps.core.config.service.MyConfig",
            description="Test of sub config model."
    )
    public static class MyConfig2 extends APSConfig {

        /** */
        @APSConfigItemDescription(description="Do you like Southern Confort ?", defaultValue={@APSDefaultValue(value="true", configEnv="devel")})
        public APSConfigValue southernComfort;

        /** */
        @APSConfigItemDescription(description="Some other choices")
        public MySubConfig2 otherChoices;

        /** */
        @APSConfigItemDescription(description="More choices")
        public APSConfigList<MySubConfig2> moreChoices;

    }
}
