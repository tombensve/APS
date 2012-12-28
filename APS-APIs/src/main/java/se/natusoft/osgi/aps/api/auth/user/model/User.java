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
package se.natusoft.osgi.aps.api.auth.user.model;

import java.util.Properties;

/**
 * This defines a User.
 */
public interface User extends Comparable<User> {

    /**
     * Returns the unique id of the user.
     */
    public String getId();

    /**
     * Returns true if this user is authenticated.
     */
    public boolean isAuthenticated();

    /**
     * Returns true if the user has the specified role name.
     *
     * @param roleName The name of the role to check for.
     */
    public boolean hasRole(String roleName);

    /**
     * This provides whatever extra information about the user you want. How to use this
     * is upp to the user of the service. There are some constants in this class that
     * provide potential keys for the user properties.
     * <p/>
     * Please note that the returned properties are read only!
     */
    public Properties getUserProperties();

    /** Optional suggestion for user properties key. */
    public static final String USER_NAME = "name";

    /** Optional suggestion for user properties key. */
    public static final String USER_PHONE = "phone";

    /** Optional suggestion for user properties key. */
    public static final String USER_PHONE_WORK = "phone.work";

    /** Optional suggestion for user properties key. */
    public static final String USER_PHONE_HOME = "phone.home";

    /** Optional suggestion for user properties key. */
    public static final String USER_EMAIL = "email";

}
