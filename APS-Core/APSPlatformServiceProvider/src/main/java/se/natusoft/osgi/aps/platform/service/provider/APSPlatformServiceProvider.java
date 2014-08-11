/* 
 * 
 * PROJECT
 *     Name
 *         APS Platform Service Provider
 *     
 *     Code Version
 *         0.11.0
 *     
 *     Description
 *         Provides a platform specific configuration and service providing platform instance
 *         specific information.
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
 *         2011-08-16: Created!
 *         
 */
package se.natusoft.osgi.aps.platform.service.provider;

import se.natusoft.osgi.aps.api.core.platform.model.PlatformDescription;
import se.natusoft.osgi.aps.api.core.platform.service.APSPlatformService;
import se.natusoft.osgi.aps.platform.config.APSPlatformConfig;

/**
 * Provides an implementation of the APSPlatformService.
 */
public class APSPlatformServiceProvider implements APSPlatformService {
    //
    // Private Members
    //

    //
    // Constructors
    //
    
    /**
     * Creates a new APSPlatformServiceProvider instance.
     */
    public APSPlatformServiceProvider() {
    }
    
    // 
    // Methods
    //

    /**
     * Returns the description of the platform.
     */
    @Override
    public PlatformDescription getPlatformDescription() {
        try {
            PlatformDescription pd = new PlatformDescription();
            if (!APSPlatformConfig.managed.isManaged()) {
                APSPlatformConfig.managed.waitUntilManaged();
            }
            APSPlatformConfig config = APSPlatformConfig.managed.get();

            pd.setIdentifier(config.platformID.toString());
            pd.setType(config.platformType.toString());
            pd.setDescription(config.description.toString());
            return pd;
        }
        catch (Exception e) {
            return new PlatformDescription("<unknown>", "<unknown>", "<unknown>");
        }
    }

}
