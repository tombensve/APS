/* 
 * 
 * PROJECT
 *     Name
 *         APS Administration Web Registration Service
 *     
 *     Code Version
 *         0.11.0
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
package se.natusoft.osgi.aps.apsadminweb.service;

import se.natusoft.osgi.aps.apsadminweb.service.model.AdminWebReg;

import java.util.List;

/**
 * This service registers other specific administration web applications
 * to make them available under a common administration gui. 
 */
public interface APSAdminWebService {
    
    /**
     * Registers an admin web application.
     * 
     * @param adminWebReg Registration information for the admin web.
     * 
     * @throws IllegalArgumentException if the admin web has already been registered or if it is using the 
     *                                  same deployment url as some other registered admin web.
     */
    public void registerAdminWeb(AdminWebReg adminWebReg) throws IllegalArgumentException;
    
    /**
     * Unregisters a previously registered admin web. This is failsafe. If it has not been registered nothing happens. 
     * 
     * @param adminWebReg Registration information for the admin web. Use the same as registered with.
     */
    public void unregisterAdminWeb(AdminWebReg adminWebReg);
    
    /** 
     * @return All currently registered admin webs.
     */
    public List<AdminWebReg> getRegisteredAdminWebs();
}
