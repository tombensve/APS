/* 
 * 
 * PROJECT
 *     Name
 *         APS Administration Web Registration Service
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         The service for registering admin webs with aps-admin-web.
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
package se.natusoft.osgi.aps.apsadminweb.service.provider;

import se.natusoft.osgi.aps.apsadminweb.service.APSAdminWebService;
import se.natusoft.osgi.aps.apsadminweb.service.model.AdminWebReg;
import se.natusoft.osgi.aps.tools.APSLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides an implementstion of APSAdminWebService.
 */
public class APSAdminWebServiceProvider implements APSAdminWebService {
    //
    // Private Members
    //
    
    /** Our logger. */
    private APSLogger logger;
    
    /** The list of registered admin webs. */
    private List<AdminWebReg> registeredAdminWebs = new ArrayList<>();
    
    /** A map of registered deployment urls. */
    private Map<String, AdminWebReg> deployUrls = new HashMap<>();
    
    //
    // Constructors
    //
    
    /**
     * Creates a new APSAdminWebServiceProvider instance.
     * 
     * @param logger The logger to use by this service.
     */
    public APSAdminWebServiceProvider(APSLogger logger) {
        this.logger = logger;
    }
    
    //
    // Methods
    //
    
    /**
     * Registers an admin web application.
     * 
     * @param adminWebReg Registration information for the admin web.
     * 
     * @throws IllegalArgumentException if the admin web has already been registered or if it is using the 
     *                                  same deployment url as some other registered admin web.
     */
    @Override
    public void registerAdminWeb(AdminWebReg adminWebReg) throws IllegalArgumentException {
        if (this.registeredAdminWebs.contains(adminWebReg)) {
            this.logger.error("Registered '" + adminWebReg + "' already exists!");
            throw new IllegalArgumentException("The admin web you are trying to register is already registered!");
        }
        AdminWebReg existing = this.deployUrls.get(adminWebReg.getUrl());
        if (existing != null) {
            this.logger.error("The deployment url(" + adminWebReg.getUrl() + ") specified by '" + adminWebReg + "' is alread in use by '" + existing + "'!");
            throw new IllegalArgumentException("The deployment url specified by your admin web registration is already in use by '" + 
                    existing + "'!");
        }
        this.deployUrls.put(adminWebReg.getUrl(), adminWebReg);

        this.registeredAdminWebs.add(adminWebReg);
        
        this.logger.debug("Registered '" + adminWebReg + "'.");
    }
    
    /**
     * Unregisters a previously registered admin web. This is failsafe. If it has not been registered nothing happens. 
     * 
     * @param adminWebReg Registration information for the admin web. Use the same as registered with.
     */
    @Override
    public void unregisterAdminWeb(AdminWebReg adminWebReg) {
        this.registeredAdminWebs.remove(adminWebReg);
        this.logger.debug("Unregistered '" + adminWebReg + "'.");
    }
    
    /** 
     * @return All currently registered admin webs.
     */
    @Override
    public List<AdminWebReg> getRegisteredAdminWebs() {
        return this.registeredAdminWebs;
    }
    
}
