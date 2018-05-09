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
 *     Tommy Svensson (tommy.svensson@biltmore.se)
 *         Changes:
 *         2012-07-17: Created!
 *
 */
package se.natusoft.osgi.aps.dsconfig.service;

import se.natusoft.osgi.aps.api.data.jdbc.model.DataSourceDef;
import se.natusoft.osgi.aps.api.data.jdbc.service.APSDataSourceDefService;
import se.natusoft.osgi.aps.dsconfig.config.APSDataSourcesConfig;
import se.natusoft.osgi.aps.dsconfig.model.DataSourceDefImpl;
import se.natusoft.osgi.aps.util.APSLogger;

import java.util.LinkedList;
import java.util.List;

/**
 * Provides implementation of APSDataSourceDefService.
 */
public class APSDataSourceDefServiceProvider implements APSDataSourceDefService {
    //
    // Private Members
    //

    /** The logger to log to. */
    private APSLogger logger = null;

    //
    // Constructors
    //

    /**
     * Creates a new APSDataSourceDefServiceProvider instance.
     *
     * @param logger The logger to log to.
     */
    public APSDataSourceDefServiceProvider(APSLogger logger) {
        this.logger = logger;
    }

    //
    // Methods
    //

    /**
     * Looks up a data source definition by its configured name.
     *
     * @param name The name to lookup.
     * @return A DataSourceDef or null if name was not valid.
     */
    @Override
    public DataSourceDef lookupByName(String name) {
        DataSourceDef res = null;
        for (APSDataSourcesConfig.APSDataSourceConfig dsConfig : APSDataSourcesConfig.inst.dataSourceDefinitions) {
            if (dsConfig.name.toString().equals(name)) {
                res = new DataSourceDefImpl(dsConfig);
                break;
            }
        }
        return res;
    }

    /**
     * @return All available definitions.
     */
    @Override
    public List<DataSourceDef> getAllDefinitions() {
        List<DataSourceDef> all = null;
        all = new LinkedList<>();
        for (APSDataSourcesConfig.APSDataSourceConfig dsConfig : APSDataSourcesConfig.inst.dataSourceDefinitions) {
            all.add(new DataSourceDefImpl(dsConfig));
        }

        return all;
    }
}
