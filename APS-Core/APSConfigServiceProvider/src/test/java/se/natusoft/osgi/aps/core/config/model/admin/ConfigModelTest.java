/*
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
import se.natusoft.osgi.aps.core.config.model.APSConfigInstanceMemoryStoreImpl;
import se.natusoft.osgi.aps.core.config.model.APSConfigObjectFactory;
import se.natusoft.osgi.aps.core.config.model.ConfigEnvironmentProvider;

import java.util.Properties;

public class ConfigModelTest extends TestCase {

    /**
     *
     */
    public ConfigModelTest() {
    }

    /**
     * @throws Exception on failure.
     */
    public void testConfigModels() throws Exception {
        System.out.print("testConfigModels...");

        Properties props = new Properties();
        APSConfigInstanceMemoryStoreImpl configValueStore = new APSConfigInstanceMemoryStoreImpl(props);
        APSConfigEnvironment testConfigEnvironment = new APSConfigEnvironmentImpl("test", "For test.");
        APSConfigEnvironment defaultConfigEnvironment = new APSConfigEnvironmentImpl("default", "For test.");
        APSConfigObjectFactory configObjectFactory = new APSConfigObjectFactory(new ConfigEnvironmentProvider() {
            @Override
            public APSConfigEnvironment getActiveConfigEnvironment() {
                return new APSConfigEnvironmentImpl("test", "For test.");
            }
        }, configValueStore);

        APSConfigEditModelImpl configModel = new APSConfigEditModelImpl(MyConfig.class, configObjectFactory);
        APSConfigAdminImpl config = new APSConfigAdminImpl(configModel, configValueStore);

        assertEquals(config.getConfigValue(configModel.getValueByName("southerncomfort"), testConfigEnvironment), "true");
        assertEquals(config.getConfigValue(configModel.getValueByName("southerncomfort"), defaultConfigEnvironment), "false");

        APSConfigEditModel otherChoicesModel = (APSConfigEditModel)configModel.getValueByName("otherchoices");
        assertEquals(config.getConfigValue(otherChoicesModel.getValueByName("pizza"), testConfigEnvironment), "true");
        assertEquals(config.getConfigValue(otherChoicesModel.getValueByName("salmon"), testConfigEnvironment), "false");

        System.out.println("ok");
    }

    /**
     * @throws Exception on failure.
     */
    public void testConfigModels2() throws Exception {
        System.out.print("testConfigModels2...");

        Properties props = new Properties();
        APSConfigInstanceMemoryStoreImpl configValueStore = new APSConfigInstanceMemoryStoreImpl(props);
        // Please note that we provide a different config environment than the default values in the config class!
        APSConfigEnvironment configEnvironment = new APSConfigEnvironmentImpl("whatever", "For test.");
        APSConfigObjectFactory configObjectFactory = new APSConfigObjectFactory(new ConfigEnvironmentProvider() {
            @Override
            public APSConfigEnvironment getActiveConfigEnvironment() {
                return new APSConfigEnvironmentImpl("whatever", "For test.");
            }
        }, configValueStore);

        APSConfigEditModelImpl configModel = new APSConfigEditModelImpl(MyConfig.class, configObjectFactory);
        APSConfigAdminImpl config = new APSConfigAdminImpl(configModel, configValueStore);

        // Please note that here we automatically fall back to config env "default" due to than config env "whatever" does not exist!
        // And since we have provided a default value for config env "default" for this value we will get that value back.
        assertEquals("false", config.getConfigValue(configModel.getValueByName("southerncomfort"), configEnvironment));

        APSConfigEditModel otherChoicesModel = (APSConfigEditModel)configModel.getValueByName("otherchoices");
        assertNull(config.getConfigValue(otherChoicesModel.getValueByName("pizza"), configEnvironment));
        assertNull(config.getConfigValue(otherChoicesModel.getValueByName("salmon"), configEnvironment));

        System.out.println("ok");
    }

    @APSConfigDescription(
            version="1.0",
            configId="aps.configapiproxy.test.sub",
            description="Test of sub config model."
    )
    public static class MySubConfig extends APSConfig {

        /** */
        @APSConfigItemDescription(description="Do you like pizza ?", defaultValue={@APSDefaultValue(value="true", configEnv="test")})
        public APSConfigValue pizza;

        /** */
        @APSConfigItemDescription(description="Do you like salmon ?", defaultValue={@APSDefaultValue(value="false", configEnv="test")})
        public APSConfigValue salmon;
    }

    @APSConfigDescription(
            version="1.0",
            configId="aps.configapiproxy.test",
            description="Test of sub config model."
    )
    public static class MyConfig extends APSConfig {

        /** */
        @APSConfigItemDescription(description="Do you like Southern Comfort ?",
                defaultValue={@APSDefaultValue(value="true", configEnv="test"), @APSDefaultValue(value="false", configEnv="default")})
        public APSConfigValue southernComfort;

        /** */
        @APSConfigItemDescription(description="Some other choices")
        public MySubConfig otherChoices;

        /** */
        @APSConfigItemDescription(description="More choices")
        public APSConfigList<MySubConfig> moreChoices;

    }


}
