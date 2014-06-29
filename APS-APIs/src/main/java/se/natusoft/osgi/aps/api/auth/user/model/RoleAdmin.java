/* 
 * 
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         0.11.0
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

/**
 * Provides update API for Role.
 */
public interface RoleAdmin extends Role {

    /**
     * Changes the description of the role.
     *
     * @param description The new description.
     */
    public void setDescription(String description);

    /**
     * Returns all sub roles for this role.
     */
    public List<Role> getRoles();

    /**
     * Adds a sub role to this role.
     *
     * @param role The role to add.
     */
    public void addRole(Role role);

    /**
     * Removes a sub role from this role.
     *
     * @param role The role to remove.
     */
    public void removeRole(Role role);

    /**
     * Sets whether this is a master role or not.
     *
     * @param masterRole true for master role.
     */
    public void setMasterRole(boolean masterRole);

}
