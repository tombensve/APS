/* 
 * 
 * PROJECT
 *     Name
 *         APS Data Source
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         This bundle provides data source definitions configuration and a service to lookup
 *         the configured data source definitions with. These are not javax.sql.DataSource
 *         objects! This only provides the configuration data to setup a data source. Some
 *         other bundle can use this to configure a DataSource, a connection pool, JPA, etc
 *         from this.
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
 *         2014-08-13: Created!
 *         
 */
package se.natusoft.osgi.aps.dsconfig;

import org.junit.Test;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigEditModel;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigEnvironment;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigValueEditModel;
import se.natusoft.osgi.aps.core.config.model.APSConfigInstanceMemoryStoreImpl;
import se.natusoft.osgi.aps.core.config.model.APSConfigObjectFactory;
import se.natusoft.osgi.aps.core.config.model.ConfigEnvironmentProvider;
import se.natusoft.osgi.aps.core.config.model.admin.APSConfigAdminImpl;
import se.natusoft.osgi.aps.core.config.model.admin.APSConfigEditModelImpl;
import se.natusoft.osgi.aps.core.config.model.admin.APSConfigEnvironmentImpl;
import se.natusoft.osgi.aps.core.config.model.admin.APSConfigValueEditModelImpl;
import se.natusoft.osgi.aps.dsconfig.config.APSDataSourcesConfig;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class ConfigTest {

    private static final APSConfigEnvironment testConfigEnvironment = new APSConfigEnvironmentImpl("testenv", "For test.", 0);
    private static final APSConfigEnvironment defaultConfigEnvironment = new APSConfigEnvironmentImpl("default", "For test.", 0);

    @Test
    public void testConfig() throws Exception {
        Properties props = new Properties();
        APSConfigInstanceMemoryStoreImpl configValueStore = new APSConfigInstanceMemoryStoreImpl(props);
        APSConfigObjectFactory configObjectFactory = new APSConfigObjectFactory(new ConfigEnvironmentProvider() {
            @Override
            public APSConfigEnvironment getActiveConfigEnvironment() {
                return testConfigEnvironment;
            }

            @Override
            public List<APSConfigEnvironment> getConfigEnvironments() {
                List<APSConfigEnvironment> envs = new LinkedList<>();
                envs.add(testConfigEnvironment);
                envs.add(defaultConfigEnvironment);
                return envs;
            }

            @Override
            public APSConfigEnvironment getConfigEnvironmentByName(String name) {
                return getActiveConfigEnvironment();
            }
        }, configValueStore);

        APSConfigEditModelImpl configModel = new APSConfigEditModelImpl(APSDataSourcesConfig.class, configObjectFactory);
        APSConfigAdminImpl config = new APSConfigAdminImpl(configModel, configValueStore);
        APSConfigEditModel dataSourceModel = (APSConfigEditModel)config.getConfigModel().getValueByName("dataSourceDefinitions");
        APSConfigEditModel dsInstanceModel = config.createConfigListEntry(dataSourceModel, testConfigEnvironment);

        APSConfigValueEditModel valueModel = dsInstanceModel.getValueByName("name");
        System.out.println("KEY: " + ((APSConfigValueEditModelImpl)dataSourceModel).getKey().toString());
        config.setConfigValue(valueModel, "test", testConfigEnvironment);

        valueModel = dsInstanceModel.getValueByName("connectionURL");
        config.setConfigValue(valueModel, "http://somewhere/something", testConfigEnvironment);

        valueModel = dsInstanceModel.getValueByName("connectionDriverName");
        config.setConfigValue(valueModel, "TestConnectionDriver", testConfigEnvironment);

        System.out.println(props.toString());
    }
}
