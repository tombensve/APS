/* 
 * 
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         0.9.3
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
 *     tommy (tommmy@natusoft.se)
 *         Changes:
 *         2012-07-17: Created!
 *         
 */
package se.natusoft.osgi.aps.api.data.jdbc.model;

/**
 * This represents information required for setting upp a JDBC data source.
 */
public interface DataSourceDef {

    /**
     * @return The name of this data source definition. This information is optional and can return null!
     */
    String getName();

    /**
     * @return The JDBC connection URL. Ex: jdbc:provider://host:port/database[;properties].
     */
    String getConnectionURL();

    /**
     * @return The fully qualified class name of the JDBC driver to use.
     */
    String getConnectionDriveName();

    /**
     * @return The name of the database user to login as.
     */
    String getConnectionUserName();

    /**
     * @return The password for the database user.
     */
    String getConnectionPassword();
}
