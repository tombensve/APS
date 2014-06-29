/* 
 * 
 * PROJECT
 *     Name
 *         APS Session Service Provider
 *     
 *     Code Version
 *         0.11.0
 *     
 *     Description
 *         Provides an OSGi server wide session.
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
 *         2013-02-03: Created!
 *         
 */
package se.natusoft.osgi.aps.session.config;

import se.natusoft.osgi.aps.api.core.config.APSConfig;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigDescription;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigItemDescription;
import se.natusoft.osgi.aps.api.core.config.annotation.APSDefaultValue;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue;

/**
 * Configuration for APSSessionServiceProvider.
 */
@APSConfigDescription(
        version = "1.0",
        configId = "se.natusoft.osgi.aps.aps-session-service-provider",
        group = "misc",
        description = "Configuration for aps-session-service-provider providing of APSSessionService."
)
public class SessionConfig extends APSConfig {

    /** This will receive a populated instance of this config class if it is listed in the APS-Config: manifest entry. */
    public static SessionConfig get;

    @APSConfigItemDescription(
            description = "The default timeout of the session in minutes.",
            defaultValue = @APSDefaultValue("15")
    )
    public APSConfigValue timeout;
}
