/* 
 * 
 * PROJECT
 *     Name
 *         APS Configuration Admin Web
 *     
 *     Code Version
 *         0.10.0
 *     
 *     Description
 *         Edits configurations registered with the APSConfigurationService.
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
 *     Tommy Svensson (tommy.svensson@biltmore.se)
 *         Changes:
 *         2012-04-28: Created!
 *         
 */
package se.natusoft.osgi.aps.apsconfigadminweb.config;

import se.natusoft.osgi.aps.api.core.config.APSConfig;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigDescription;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigItemDescription;
import se.natusoft.osgi.aps.api.core.config.annotation.APSDefaultValue;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue;

/**
 * Configuration values for APSConfigAdminWeb.
 */
@APSConfigDescription(
        version = "1.0.0",
        configId = "se.natusoft.osgi.aps.config-admin-web",
        group = "aps.ui",
        description = "Configuration for APSConfigAdminWeb."
)
public class CAWConfig extends APSConfig {

    /** Injected by aps-config-service-provider. */
    public static CAWConfig inst;

    @APSConfigItemDescription(
            description = "The maximum number of lines to show in the list of values for a value with multiple instances " +
                          "before scrolling content.",
            defaultValue = @APSDefaultValue(value = "8")
    )
    public APSConfigValue manyValueMaxLinesBeforeScrolling;
}
