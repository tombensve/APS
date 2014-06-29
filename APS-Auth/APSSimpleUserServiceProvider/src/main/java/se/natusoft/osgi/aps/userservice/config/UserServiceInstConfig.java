/* 
 * 
 * PROJECT
 *     Name
 *         APS Simple User Service Provider
 *     
 *     Code Version
 *         0.11.0
 *     
 *     Description
 *         Provides an implementation of APSSimpleUserService backed by a database.
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
package se.natusoft.osgi.aps.userservice.config;

import se.natusoft.osgi.aps.api.core.config.APSConfig;
import se.natusoft.osgi.aps.api.core.config.ManagedConfig;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigDescription;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigItemDescription;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigList;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue;

@APSConfigDescription(
        configId = "se.natusoft.osgi.aps.userservice.user-service",
        group = "aps",
        description = "This defines references to datasources. A service instance will be created " +
                      "for each with an additional 'se.natusoft.osgi.aps.userservice.dsref' property " +
                      "indicating the datasource used by the service instance.",
        version = "1.0.0"
)
public class UserServiceInstConfig extends APSConfig {

    public static ManagedConfig<UserServiceInstConfig> managed = new ManagedConfig<>();

    public static UserServiceInstConfig get() {
        if (!managed.isManaged()) {
            managed.waitUtilManaged();
        }

        return managed.get();
    }

    @APSConfigItemDescription(
            description = "A set of data source definitions. Add one for each definition you want to create.",
            environmentSpecific = true
    )
    public APSConfigList<UserServiceInstance> instances;


    @APSConfigDescription(
            configId = "se.natusoft.osgi.aps.userservice.instance",
            description = "This defines an instance by specifying the name of a defined datasource to " +
                          "use for the instance.",
            version = "1.0.0"
    )
    public static class UserServiceInstance extends APSConfig {

        @APSConfigItemDescription(
                description = "The name of the instance. The instance will be registered with " +
                    "a 'instance=<name>' property that can be used when tracking the service.",
                environmentSpecific = false
        )
        public APSConfigValue name;

        @APSConfigItemDescription(
                description = "The name of a defined data source to use for an instance of the user service.",
                environmentSpecific = false
        )
        public APSConfigValue dsRef;

    }

}
