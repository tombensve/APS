/* 
 * 
 * PROJECT
 *     Name
 *         APS Data Source
 *     
 *     Code Version
 *         0.9.1
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
 *     Tommy Svensson (tommy.svensson@biltmore.se)
 *         Changes:
 *         2012-07-17: Created!
 *         
 */
package se.natusoft.osgi.aps.dsconfig.config;

import se.natusoft.osgi.aps.api.core.config.APSConfig;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigDescription;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigItemDescription;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigList;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue;

@APSConfigDescription(
        configId = "se.natusoft.osgi.aps.dsconfig.datasources",
        group = "persistence",
        description =
                "This provides a general APS configuration for setting up JDBC data sources. This is " +
                "provided by the aps-datasource.jar bundle which also provides an implementation of " +
                "APSDataSourceDefService for looking up a configuration by its name. This can be used " +
                "by other bundles that provide connection pools, JPA, etc.",
        version = "1.0.0"
)
public class APSDataSourcesConfig extends APSConfig {

    /** Injected by aps-config-service-provider due to APS-Configs manifest header entry! */
    public static APSDataSourcesConfig inst;

    @APSConfigItemDescription(
            description = "A set of data source definitions. Add one for each definition you want to create."
    )
    public APSConfigList<APSDataSourceConfig> dataSourceDefinitions;


    @APSConfigDescription(
            configId = "se.natusoft.osgi.aps.dsconfig.datasource",
            description = "This configures a specific data source.",
            version = "1.0.0"
    )
    public static class APSDataSourceConfig extends APSConfig {

        @APSConfigItemDescription(
                description = "The name of the data source for referencing it.",
                environmentSpecific = false
        )
        public APSConfigValue name;

        @APSConfigItemDescription(
                description = "The JDBC connection URL for the database. Ex: jdbc:provider://host:port/database[;property;...]",
                environmentSpecific = true
        )
        public APSConfigValue connectionURL;

        @APSConfigItemDescription(
                description = "The JDBC driver class to use.",
                environmentSpecific = true
        )
        public APSConfigValue connectionDriverName;

        @APSConfigItemDescription(
                description = "The database user to login with.",
                environmentSpecific = true
        )
        public APSConfigValue user;

        @APSConfigItemDescription(
                description = "The password for the database user.",
                environmentSpecific = true
        )
        public APSConfigValue password;
    }

}
