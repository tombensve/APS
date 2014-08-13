/*
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
 *         2011-08-05: Created!
 *
 */
package se.natusoft.osgi.aps.core.config.model.admin;

import junit.framework.TestCase;
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
import se.natusoft.osgi.aps.core.config.model.ConfigEnvironmentProvider;
import se.natusoft.osgi.aps.core.config.store.APSConfigEnvStore;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class ConfigModelTest extends TestCase {

    private APSConfigEnvironment testConfigEnvironment = new APSConfigEnvironmentImpl("testenv", "For test.", 0);
    private APSConfigEnvironment defaultConfigEnvironment = new APSConfigEnvironmentImpl("default", "For test.", 0);
    private APSConfigEnvironment whateverConfigEnvironment = new APSConfigEnvironmentImpl("whatever", "For test.", 0);

    private Properties props = new Properties();
    private APSConfigInstanceMemoryStoreImpl configValueStore = new APSConfigInstanceMemoryStoreImpl(props);
    private APSConfigEnvStore envStore = new APSConfigEnvStore(
            testConfigEnvironment, // Also active
            defaultConfigEnvironment,
            whateverConfigEnvironment
    );

    private APSConfigObjectFactory configObjectFactory = new APSConfigObjectFactory(new ConfigEnvironmentProvider() {
        @Override
        public APSConfigEnvironment getActiveConfigEnvironment() {
            return envStore.getActiveConfigEnvironment();
        }

        @Override
        public List<APSConfigEnvironment> getConfigEnvironments() {
            return envStore.getConfigEnvironments();
        }

        @Override
        public APSConfigEnvironment getConfigEnvironmentByName(String name) {
            return envStore.getConfigEnvironmentByName(name);
        }
    }, configValueStore);


    /**
     *
     */
    public ConfigModelTest() {
    }

    /**
     * Note that this test is very long. I don't want to split it up into several tests since that would
     * run against new instances each time. That is not what happens in reality and would also hide defects
     * like modification of one config value affects another for example.
     *
     * @throws Exception on failure.
     */
    public void testConfigModels() throws Exception {
        System.out.print("testConfigModels...");

        APSConfigEditModelImpl configModel = new APSConfigEditModelImpl(RootConfig.class, configObjectFactory);
        RootConfig rootConfig = (RootConfig)configModel.getInstance();
        APSConfigAdminImpl config = new APSConfigAdminImpl(configModel, configValueStore, envStore);

        // RootConfig.bool

        APSConfigValueEditModel bool = configModel.getValueByName("bool");
        APSConfigReference boolRef = config.createRef()._(configModel)._(bool);

        String boolRefValue = config.getConfigValue(boolRef.configEnvironment(testConfigEnvironment));
        assertEquals("true", boolRefValue);

        boolRefValue = config.getConfigValue(boolRef.configEnvironment(defaultConfigEnvironment));
        assertEquals("false", boolRefValue);

        assertEquals(rootConfig.bool.toBoolean(), true);

        config.setConfigValue(boolRef.configEnvironment(configObjectFactory.getActiveConfigEnvironment()), "false");

        assertEquals(rootConfig.bool.toBoolean(), false);


        // RootConfig.str

        APSConfigValueEditModel str = configModel.getValueByName("str");
        APSConfigReference strRef = config.createRef()._(configModel)._(str);

        String strRefValue = config.getConfigValue(strRef.configEnvironment(configObjectFactory.getActiveConfigEnvironment()));
        // This has no default values and have not been set so it will be null.
        assertTrue(strRefValue == null);

        config.setConfigValue(strRef.configEnvironment(configObjectFactory.getActiveConfigEnvironment()), "testValue");
        assertEquals("testValue", rootConfig.str.toString());

        // RootConfig.simple

        assertTrue(rootConfig.simple != null);

        assertEquals("", rootConfig.simple.simpleValue1.toString());
        assertEquals("", rootConfig.simple.simpleValue2.toString());

        APSConfigEditModel simple = (APSConfigEditModel)configModel.getValueByName("simple");
        assertNotNull(simple);
        APSConfigReference simpleRef = config.createRef()._(configModel)._(simple);

        APSConfigValueEditModel simpleValue1 = simple.getValueByName("simpleValue1");
        APSConfigReference simpleValue1Ref = simpleRef._(simpleValue1);
        config.setConfigValue(simpleValue1Ref, "qaz");

        APSConfigValueEditModel simpleValue2 = simple.getValueByName("simpleValue2");
        APSConfigReference simpleValue2Ref = simpleRef._(simpleValue2);
        config.setConfigValue(simpleValue2Ref, "wsx");

        assertEquals("qaz", config.getConfigValue(simpleValue1Ref));
        assertEquals("wsx", config.getConfigValue(simpleValue2Ref));
        assertEquals("qaz", rootConfig.simple.simpleValue1.toString());
        assertEquals("wsx", rootConfig.simple.simpleValue2.toString());

        // RootConfig.listComplex.

        assertTrue(rootConfig.listComplex != null);

        APSConfigEditModel listComplex = (APSConfigEditModel)configModel.getValueByName("listComplex");
        assertNotNull(listComplex);
        APSConfigReference listComplexRef = config.createRef()._(configModel)._(listComplex);

        APSConfigReference listComplex_0_Ref = config.addConfigList(listComplexRef);
        assertEquals(config.getListSize(listComplexRef), 1);

        APSConfigValueEditModel listComplex_0_complexValue1 = listComplex.getValueByName("complexValue1");
        APSConfigReference listComplex_0_complexValue1Ref = listComplex_0_Ref._(listComplex_0_complexValue1);
        config.setConfigValue(listComplex_0_complexValue1Ref, "edc");

        APSConfigValueEditModel listComplex_0_complexValue2 = listComplex.getValueByName("complexValue2");
        APSConfigReference listComplex_0_complexValue2Ref = listComplex_0_Ref._(listComplex_0_complexValue2);
        config.setConfigValue(listComplex_0_complexValue2Ref, "rfv");

        assertEquals("edc", rootConfig.listComplex.get(0).complexValue1.toString());
        assertEquals("rfv", rootConfig.listComplex.get(0).complexValue2.toString());

        // RootConfig.listComplex.listSimple

        APSConfigEditModel listComplex_0_listSimple = (APSConfigEditModel)listComplex.getValueByName("listSimple");
        assertNotNull(listComplex_0_listSimple);
        APSConfigReference listComplex_0_listSimpleRef = listComplex_0_Ref._(listComplex_0_listSimple);

        APSConfigReference listComplex_0_listSimple_0_Ref = config.addConfigList(listComplex_0_listSimpleRef);
        assertEquals(1, config.getListSize(listComplex_0_listSimpleRef));

        APSConfigValueEditModel listComplex_0_listSimple_0_simpleValue1 =
                listComplex_0_listSimple.getValueByName("simpleValue1");
        APSConfigReference listComplex_0_listSimple_0_simpleValue1Ref =
                listComplex_0_listSimple_0_Ref._(listComplex_0_listSimple_0_simpleValue1);
        config.setConfigValue(listComplex_0_listSimple_0_simpleValue1Ref, "tgb");

        APSConfigValueEditModel listComplex_0_listSimple_0_simpleValue2 =
                listComplex_0_listSimple.getValueByName("simpleValue2");
        APSConfigReference listComplex_0_listSimple_0_simpleValue2Ref =
                listComplex_0_listSimple_0_Ref._(listComplex_0_listSimple_0_simpleValue2);
        config.setConfigValue(listComplex_0_listSimple_0_simpleValue2Ref, "yhn");

        assertEquals("tgb", rootConfig.listComplex.get(0).listSimple.get(0).simpleValue1.toString());
        assertEquals("yhn", rootConfig.listComplex.get(0).listSimple.get(0).simpleValue2.toString());

        try {
            assertEquals("", rootConfig.listComplex.get(1).listSimple.get(0).simpleValue2.toString());
            fail("This should have thrown an IndexOutOfBoundException since we are referencing index 1 and there are only 1 entry.");
        }
        catch (IndexOutOfBoundsException iobe) {/*OK*/}

        // Test one more index of the first.

        APSConfigReference listComplex_1_Ref = config.addConfigList(listComplexRef);
        assertEquals(config.getListSize(listComplexRef), 2);

        APSConfigValueEditModel listComplex_1_complexValue1 = listComplex.getValueByName("complexValue1");
        APSConfigReference listComplex_1_complexValue1Ref = listComplex_1_Ref._(listComplex_1_complexValue1);
        config.setConfigValue(listComplex_1_complexValue1Ref, "ujm");

        APSConfigValueEditModel listComplex_1_complexValue2 = listComplex.getValueByName("complexValue2");
        APSConfigReference listComplex_1_complexValue2Ref = listComplex_1_Ref._(listComplex_1_complexValue2);
        config.setConfigValue(listComplex_1_complexValue2Ref, "ik,");

        assertEquals("ujm", rootConfig.listComplex.get(1).complexValue1.toString());
        assertEquals("ik,", rootConfig.listComplex.get(1).complexValue2.toString());

        APSConfigEditModel listComplex_1_listSimple = (APSConfigEditModel)listComplex.getValueByName("listSimple");
        assertNotNull(listComplex_1_listSimple);
        APSConfigReference listComplex_1_listSimpleRef = listComplex_1_Ref._(listComplex_1_listSimple);

        APSConfigReference listComplex_1_listSimple_0_Ref = config.addConfigList(listComplex_1_listSimpleRef);
        assertEquals(1, config.getListSize(listComplex_1_listSimpleRef));

        APSConfigValueEditModel listComplex_1_listSimple_0_simpleValue1 =
                listComplex_1_listSimple.getValueByName("simpleValue1");
        APSConfigReference listComplex_1_listSimple_0_simpleValue1Ref =
                listComplex_1_listSimple_0_Ref._(listComplex_1_listSimple_0_simpleValue1);
        config.setConfigValue(listComplex_1_listSimple_0_simpleValue1Ref, "ol.");

        APSConfigValueEditModel listComplex_1_listSimple_0_simpleValue2 =
                listComplex_1_listSimple.getValueByName("simpleValue2");
        APSConfigReference listComplex_1_listSimple_0_simpleValue2Ref =
                listComplex_1_listSimple_0_Ref._(listComplex_1_listSimple_0_simpleValue2);
        config.setConfigValue(listComplex_1_listSimple_0_simpleValue2Ref, "po-");

        assertEquals("ol.", rootConfig.listComplex.get(1).listSimple.get(0).simpleValue1.toString());
        assertEquals("po-", rootConfig.listComplex.get(1).listSimple.get(0).simpleValue2.toString());

        // Double check that the previous index  hasn't changed.

        assertEquals("tgb", rootConfig.listComplex.get(0).listSimple.get(0).simpleValue1.toString());
        assertEquals("yhn", rootConfig.listComplex.get(0).listSimple.get(0).simpleValue2.toString());


        System.out.println("ok");
    }


    @APSConfigDescription(
            version="1.0",
            configId="aps.config.test",
            description="Root config model."
    )
    public static class RootConfig extends APSConfig {

        @APSConfigItemDescription(description="Boolean + config env specific.",
                environmentSpecific = true,
                defaultValue={@APSDefaultValue(value="true", configEnv="testenv"), @APSDefaultValue(value="false", configEnv="default")})
        public APSConfigValue bool;

        @APSConfigItemDescription(description="Plain string.")
        public APSConfigValue str;

        @APSConfigItemDescription(description="Some other choices")
        public SimpleConfig simple;

        @APSConfigItemDescription(description="List of config object test 1.")
        public APSConfigList<ComplexConfig> listComplex;

    }

    @APSConfigDescription(
            version="1.0",
            configId="simple",
            description="Test of sub config model."
    )
    public static class SimpleConfig extends APSConfig {

        @APSConfigItemDescription(description="Some value 1")
        public APSConfigValue simpleValue1;

        @APSConfigItemDescription(description="Some value 2")
        public APSConfigValue simpleValue2;

    }

    @APSConfigDescription(
            version="1.0",
            configId="complex",
            description="Test of list of config object."
    )
    public static class ComplexConfig extends APSConfig {

        @APSConfigItemDescription(description="Some value 1")
        public APSConfigValue complexValue1;

        @APSConfigItemDescription(description="Some value 2")
        public APSConfigValue complexValue2;

        @APSConfigItemDescription(description="List of config object test 1.")
        public APSConfigList<SimpleConfig> listSimple;
    }


}
