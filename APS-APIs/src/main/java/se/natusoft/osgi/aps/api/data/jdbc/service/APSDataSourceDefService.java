/* 
 * 
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         0.9.0
 *     
 *     Description
 *         Provides the APIs for the application platform services.
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
 *     tommy (tommy@natusoft.se)
 *         Changes:
 *         2012-07-17: Created!
 *         
 */
package se.natusoft.osgi.aps.api.data.jdbc.service;

import se.natusoft.osgi.aps.api.data.jdbc.model.DataSourceDef;

import java.util.List;

/**
 * This service provides lookup of configured data source definitions. These can be used
 * to setup connection pools, JPA, ...
 */
public interface APSDataSourceDefService {

    /**
     * Looks up a data source definition by its configured name.
     *
     * @param name The name to lookup.
     *
     * @return A DataSourceDef or null if name was not valid.
     */
    DataSourceDef lookupByName(String name);

    /**
     * @return All available definitions.
     */
    List<DataSourceDef> getAllDefinitions();
}
