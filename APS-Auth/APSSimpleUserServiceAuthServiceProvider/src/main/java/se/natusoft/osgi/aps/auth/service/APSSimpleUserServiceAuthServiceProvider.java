/* 
 * 
 * PROJECT
 *     Name
 *         APSSimpleUserServiceAuthServiceProvider
 *     
 *     Code Version
 *         0.9.0
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
 *         2013-02-21: Created!
 *         
 */
package se.natusoft.osgi.aps.auth.service;

import se.natusoft.osgi.aps.api.auth.user.APSAuthService;
import se.natusoft.osgi.aps.api.auth.user.APSSimpleUserService;
import se.natusoft.osgi.aps.api.auth.user.exceptions.APSAuthMethodNotSupportedException;
import se.natusoft.osgi.aps.api.auth.user.model.User;

import java.util.Properties;

/**
 * Provides an implementation that uses APSSimpleUserService.
 */
public class APSSimpleUserServiceAuthServiceProvider implements APSAuthService<String> {
    //
    // Private Members
    //

    /** The user service we authenticate against. */
    private APSSimpleUserService userService = null;

    //
    // Constructors
    //

    /**
     * Creates a new instance.
     *
     * @param userService The user service we will use to authenticate users.
     */
    public APSSimpleUserServiceAuthServiceProvider(APSSimpleUserService userService) {
        this.userService = userService;
    }

    //
    // Methods
    //

    /**
     * This authenticates a user. A Properties object is returned on successful authentication. null is returned
     * on failure. The Properties object returned contains misc information about the user. It can contain anything
     * or nothing at all. There can be no assumptions about its contents! If the specified AuthMethod is not
     * supported by the service implementation it should simply fail the auth by returning null.
     *
     * @param userId         The id of the user to authenticate.
     * @param credentials    What this is depends on the value of AuthMethod. It is up to the service implementation to resolve this.
     * @param authMethod This hints at how to interpret the credentials.
     * @return User properties on success, null on failure.
     */
    @Override
    public Properties authUser(String userId, String credentials, AuthMethod authMethod) throws APSAuthMethodNotSupportedException {
        if (authMethod != AuthMethod.PASSWORD) {
            throw new APSAuthMethodNotSupportedException("This APSAuthService provider only supports password authentication!");
        }

        Properties userProps = null;
        User user = this.userService.getUser(userId);
        if (user != null) {
            if (this.userService.authenticateUser(user, credentials, APSSimpleUserService.AUTH_METHOD_PASSWORD)) {
                userProps = new Properties(user.getUserProperties());
                userProps.setProperty("userid", user.getId());
            }
        }

        return userProps;
    }

    /**
     * Returns an array of the AuthMethods supported by the implementation.
     */
    @Override
    public AuthMethod[] getSupportedAuthMethods() {
        return new AuthMethod[] {AuthMethod.PASSWORD};
    }

}
