/* 
 * 
 * PROJECT
 *     Name
 *         APS Platform Service Provider
 *     
 *     Code Version
 *         0.9.1
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
package se.natusoft.osgi.aps.platform.config;

import se.natusoft.osgi.aps.api.core.config.APSConfig;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigDescription;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigItemDescription;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue;

/**
 * Configuration for the platform.
 */
@APSConfigDescription(
        configId="se.natusoft.osgi.aps.platform",
        group="aps",
        description="Defines platform instance configuration.", 
        version="1.0.0")
public class APSPlatformConfig extends APSConfig {

    /** This will be autopopulated. */
    public static APSPlatformConfig get;
    
    @APSConfigItemDescription(description="A unique specific id for this installation. Ex: \"Sys-Test-1\", \"Dev-2\", etc.")
    public APSConfigValue platformID;

    @APSConfigItemDescription(description="A general type of the platform. Ex: \"SysTest\", \"Development\".")
    public APSConfigValue platformType;

    @APSConfigItemDescription(description="A description of the platform installation.")
    public APSConfigValue description;
    
}
