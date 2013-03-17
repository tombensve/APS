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
package se.natusoft.osgi.aps.api.auth.user;

import se.natusoft.osgi.aps.api.auth.user.model.Role;
import se.natusoft.osgi.aps.api.auth.user.model.User;

/**
 * This is the API of a simple user service that provide basic user
 * handling that will probably be enough in many cases, but not all.
 * <P/>
 * Please note that this API does not declare any exceptions! In the
 * case of an exception being needed the APSSimpleUserServiceException
 * should be thrown. This is a runtime exception.
 */
public interface APSSimpleUserService {

    /** Password authentication method for authenticateUser(). */
    public static final String AUTH_METHOD_PASSWORD = "password";

    /**
     * Gets a role by its id.
     *
     * @param roleId The id of the role to get.
     *
     * @return A Role object representing the role or null if role was not found.
     */
    public Role getRole(String roleId);

    /**
     * Gets a user by its id.
     *
     * @param userId The id of the user to get.
     *
     * @return A User object representing the user or null if userId was not found.
     */
    public User getUser(String userId);

    /**
     * Authenticates a user using its user id and user provided authentication.
     *
     * @param user The User object representing the user to authenticate.
     * @param authentication The user provided authentication data. For example if AuthMethod is AUTH_METHOD_PASSWORD
     *                       then this is a String with a password.
     * @param authMethod Specifies what authentication method is wanted.
     *
     * @return true if authenticated, false otherwise. If true user.isAuthenticated() will also return true.
     */
    public boolean authenticateUser(User user, Object authentication, String authMethod);
}
