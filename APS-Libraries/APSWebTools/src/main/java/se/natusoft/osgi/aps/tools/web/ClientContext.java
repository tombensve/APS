/* 
 * 
 * PROJECT
 *     Name
 *         APS Web Tools
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         This provides some utility classes for web applications.
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
 *     tommy ()
 *         Changes:
 *         2011-08-27: Created!
 *         
 */
package se.natusoft.osgi.aps.tools.web;

import org.osgi.framework.BundleContext;

import java.util.HashMap;
import java.util.Map;

/**
 * A context to pass to client code for use in calling services.
 *
 * @deprecated Use WebClientContext instead.
 */
@Deprecated
public class ClientContext extends WebClientContext {
    //
    // Private Members
    //
    
    /** Used for sending messages to the user. */
    private UserMessager userMessager = null;
    
    /** A cached bundle context. */
    private BundleContext bundleContext = null;

    /** Holds services mapped to service interface class. */
    private Map<Class, Object> serviceMap = new HashMap<Class, Object>();

    //
    // Constructors
    //
    
    /**
     * Creates a new ClientContext instance. 
     * 
     * @param userMessager Used to send messages to the user.
     * @param bundleContextProvider Provides the OSGi BundleContext.
     */
    public ClientContext(UserMessager userMessager, OSGiBundleContextProvider bundleContextProvider) {
        super(userMessager, bundleContextProvider);
    }
}
