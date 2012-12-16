/* 
 * 
 * PROJECT
 *     Name
 *         APS APSConfigTest
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         This project is only for providing an APSConfigService configuration for testing on.
 *         It was made to test the APSConfigAdminWeb.
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
 *     Tommy Svensson (tommy@natusoft.se)
 *         Changes:
 *         2012-01-01: Created!
 *         
 */
package se.natusoft.osgi.aps.test.config;

import se.natusoft.osgi.aps.api.core.config.APSConfig;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigDescription;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigItemDescription;
import se.natusoft.osgi.aps.api.core.config.annotation.APSDefaultValue;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigList;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValueList;

/**
 * Configuration for external protocol support.
 */
@APSConfigDescription(
        configId = "se.natusoft.osgi.aps.config-test",
        group = "test",
        description = "This is only for testing config editor. Contains copies of other configs to cover all variants.",
        version = "1.0.0"
)
public class APSTestConfig extends APSConfig {

    @APSConfigItemDescription(description = "List of services to be made externally available.")
    public APSConfigList<ExternalizableService> externalizableServices;

    @APSConfigItemDescription(description = "This item tests the different config value types: text, boolean, date, valid values.")
    public ConfigTypesTest configTypesTest;

    @APSConfigDescription(
            configId = "se.natusoft.osgi.aps.config-test.many-node",
            description = "This specifies registered service APIs to make available externally when they are available locally.",
            version = "1.0.0"
    )
    public static class ExternalizableService extends APSConfig {

        @APSConfigItemDescription(description = "A fully qualified name of a service interface to make externally available.")
        public APSConfigValue serviceQName;

        @APSConfigItemDescription(description = "The version of the service to make externally available.")
        public APSConfigValue serviceVersion;
    }

    @APSConfigDescription(
            configId = "se.natusoft.osgi.aps.config-test.config-types-test",
            description = "A test config node.",
            version = "1.0.0"
    )
    public static class ConfigTypesTest extends APSConfig {

        @APSConfigItemDescription(description = "A boolean value for test.", isBoolean = true)
        public APSConfigValue boolVal;

        // This works but does not make much sense!
//        @APSConfigItemDescription(description = "A list of boolean values for test.", isBoolean = true)
//        public APSConfigValueList boolValues;

        @APSConfigItemDescription(description = "A test value to test config environment specific value.", environmentSpecific = true,
                defaultValue = {@APSDefaultValue(value="hej", configEnv = "default")})
        public APSConfigValue testValue;

        @APSConfigItemDescription(description = "A test value of 'many' type (a list of values).")
        public APSConfigValueList myValues;

        @APSConfigItemDescription(description = "A test date.", datePattern = "yyyy-MM-dd")
        public APSConfigValue myDate;

        @APSConfigItemDescription(description = "A list of date values for test.", datePattern = "MM/dd/yyyy")
        public APSConfigValueList testDates;

        @APSConfigItemDescription(description = "A value with a set of valid values.", validValues = {"Berlin", "Boston", "Munich", "Stockholm"})
        public APSConfigValue validValuesVal;

        @APSConfigItemDescription(description = "A list of values with a set of valid values.", validValues = {"Haj", "Val", "Delfin", "Aborre"})
        public APSConfigValueList validValuesList;
    }
}
