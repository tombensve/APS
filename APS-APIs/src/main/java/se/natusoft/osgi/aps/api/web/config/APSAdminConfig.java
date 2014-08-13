/* 
 * 
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         1.0.0
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
 *         2013-03-16: Created!
 *         
 */
package se.natusoft.osgi.aps.api.web.config;

import se.natusoft.osgi.aps.api.core.config.APSConfig;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigDescription;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigItemDescription;
import se.natusoft.osgi.aps.api.core.config.annotation.APSDefaultValue;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue;

/**
 * Configuration for the APS admin web. This is used by multiple admin web apps.
 */
@APSConfigDescription(
        version="1.0",
        configId="se.natusoft.aps.admin-web",
        group="aps",
        description="Configuration for the APS admin web."
)
public class APSAdminConfig extends APSConfig {

    @APSConfigItemDescription(
            description = "Enable to require a login for accessing '/apsadminweb'. However make sure you have " +
                          "setup the APSSimpleUserServiceDS in persistence/datasources first if you are using " +
                          "the APSSimpleUserService for authentication.",
            isBoolean = true,
            defaultValue = @APSDefaultValue("false")
    )
    public APSConfigValue requireAuthentication;
}
