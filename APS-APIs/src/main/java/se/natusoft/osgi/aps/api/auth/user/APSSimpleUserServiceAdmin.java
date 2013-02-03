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
 *     Tommy Svensson (tommy@natusoft.se)
 *         Changes:
 *         2012-08-24: Created!
 *         
 */
package se.natusoft.osgi.aps.api.auth.user;

import se.natusoft.osgi.aps.api.auth.user.model.Role;
import se.natusoft.osgi.aps.api.auth.user.model.RoleAdmin;
import se.natusoft.osgi.aps.api.auth.user.model.User;
import se.natusoft.osgi.aps.api.auth.user.model.UserAdmin;

import java.util.List;

/**
 * Admin API for APSSimpleUserService.
 */
public interface APSSimpleUserServiceAdmin extends APSSimpleUserService {

    /**
     * Creates a new role.
     *
     * @param name The name of the role. This is also the key and cannot be changed.
     * @param description A description of the role. This can be updated afterwards.
     *
     * @return a new Role object representing the role.
     */
    public RoleAdmin createRole(String name, String description);

    /**
     * Updates a role.
     *
     * @param role The role to update.
     */
    public void updateRole(Role role);

    /**
     * Deletes a role.
     *
     * @param role The role to delete. This will likely fail if there are users still having this role!
     */
    public void deleteRole(Role role);

    /**
     * Returns all available roles.
     */
    public List<RoleAdmin> getRoles();

    /**
     * Creates a new user. Please note that you get an empty user back. You probably want to
     * add roles and also possibly properties to the user. After you have done that call updateUser(user).
     *
     * @param id The id of the user. This is key so it must be unique.
     *
     * @return A User object representing the new user.
     */
    public UserAdmin createUser(String id);

    /**
     * Updates a user.
     *
     * @param user The user to update.
     */
    public void updateUser(User user);

    /**
     * Deletes a user.
     *
     * @param user The user to delete.
     */
    public void deleteUser(User user);

    /**
     * Returns all users.
     */
    public List<UserAdmin> getUsers();

    /**
     * Sets authentication for the user.
     *
     * @param user The user to set authentication for.
     * @param authentication The authentication to set.
     */
    public void setUserAuthentication(User user, String authentication);

}
