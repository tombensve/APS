/* 
 * 
 * PROJECT
 *     Name
 *         APSDataSource
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
 *     Tommy Svensson (tommy.svensson@biltmore.se)
 *         Changes:
 *         2012-07-17: Created!
 *         
 */
package se.natusoft.osgi.aps.dsconfig.model;

import se.natusoft.osgi.aps.api.data.jdbc.model.DataSourceDef;
import se.natusoft.osgi.aps.dsconfig.config.APSDataSourcesConfig;

/**
 * Implements the DataSourceDef API model.
 */
public class DataSourceDefImpl implements DataSourceDef {
    //
    // Private Members
    //

    private String name = null;
    private String connectionUrl = null;
    private String connectionDriverName = null;
    private String userName = null;
    private String password = null;

    //
    // Constructors
    //

    /**
     * Creates a new DataSourceDefImpl instance.
     *
     * @param config The config whose values we are providing.
     */
    public DataSourceDefImpl(APSDataSourcesConfig.APSDataSourceConfig config) {
        this.name = config.name.toString();
        this.connectionUrl = config.connectionURL.toString();
        this.connectionDriverName = config.connectionDriverName.toString();
        this.userName = config.user.toString();
        this.password = config.password.toString();
    }

    //
    // Methods
    //

    /**
     * @return The name of this data source definition. This information is optional and can return null!
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * @return The JDBC connection URL. Ex: jdbc:provider://host:port/database[;properties].
     */
    @Override
    public String getConnectionURL() {
        return this.connectionUrl;
    }

    /**
     * @return The fully qualified class name of the JDBC driver to use.
     */
    @Override
    public String getConnectionDriveName() {
        return this.connectionDriverName;
    }

    /**
     * @return The name of the database user to login as.
     */
    @Override
    public String getConnectionUserName() {
        return this.userName;
    }

    /**
     * @return The password for the database user.
     */
    @Override
    public String getConnectionPassword() {
        return this.password;
    }
}
