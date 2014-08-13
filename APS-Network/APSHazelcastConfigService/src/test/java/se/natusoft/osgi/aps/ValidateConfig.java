/* 
 * 
 * PROJECT
 *     Name
 *         APS Hazelcast Networking Config Service
 *     
 *     Code Version
 *         1.0.0
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
package se.natusoft.osgi.aps;

import junit.framework.TestCase;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigEnvironment;
import se.natusoft.osgi.aps.core.config.model.APSConfigInstanceMemoryStoreImpl;
import se.natusoft.osgi.aps.core.config.model.APSConfigObjectFactory;
import se.natusoft.osgi.aps.core.config.model.ConfigEnvironmentProvider;
import se.natusoft.osgi.aps.core.config.model.admin.APSConfigEditModelImpl;
import se.natusoft.osgi.aps.core.config.model.admin.APSConfigEnvironmentImpl;
import se.natusoft.osgi.aps.hazelcast.config.APSHazelCastConfig;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class ValidateConfig extends TestCase {

    private APSConfigEnvironment testConfigEnvironment = new APSConfigEnvironmentImpl("testenv", "For test.", 0);

    public void testConfig() throws Exception {
        // This code will throw an exception if the config class passed on the last line is bad.
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
                return envs;
            }

            @Override
            public APSConfigEnvironment getConfigEnvironmentByName(String name) {
                return getActiveConfigEnvironment();
            }
        }, configValueStore);

        new APSConfigEditModelImpl(APSHazelCastConfig.class, configObjectFactory);
    }
}
