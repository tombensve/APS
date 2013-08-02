/* 
 * 
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         0.9.2
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

/**
 * This defines a role.
 */
public interface Role extends Comparable<Role> {

    /**
     * @return The id of the role.
     */
    public String getId();

    /**
     * @return A description of the role.
     */
    public String getDescription();

    /**
     * Returns true if the role has the specified sub role name.
     *
     * @param roleName The name of the role to check for.
     */
    public boolean hasRole(String roleName);

    /**
     * @return true if this role is a master role. Only master roles can be added to users.
     */
    boolean isMasterRole();

}
