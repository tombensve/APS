/* 
 * 
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         0.9.1
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
 *     Tommy Svensson (tommy@natusoft.se)
 *         Changes:
 *         2012-08-24: Created!
 *         
 */
package se.natusoft.osgi.aps.api.auth.user.model;

import java.util.List;
import java.util.Properties;

/**
 * Provides update API for the User.
 */
public interface UserAdmin extends User {

    /**
     * Returns all roles for this user.
     */
    public List<Role> getRoles();

    /**
     * Adds a role to this user.
     *
     * @param role The role to add.
     */
    public void addRole(Role role);

    /**
     * Removes a role from this user.
     *
     * @param role The role to remove.
     */
    public void removeRole(Role role);

    /**
     * Adds a user property.
     *
     * @param key The key of the property.
     * @param value The value of the property.
     */
    public void addUserProperty(String key, String value);

    /**
     * Removes a user property.
     *
     * @param key The key of the property to remove.
     */
    public void removeUserProperty(String key);

    /**
     * Sets properties for the user.
     * <p/>
     * To update the user properties either first do getProperties() do your changes, and then call this method
     * with the changed properties or just use the addUserProperty() and removeUserProperty() methods.
     *
     * @param properties The properties to set.
     */
    public void setUserProperties(Properties properties);

}
