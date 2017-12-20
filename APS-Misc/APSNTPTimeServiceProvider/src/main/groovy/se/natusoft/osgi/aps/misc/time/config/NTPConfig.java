/*
 *
 * PROJECT
 *     Name
 *         APS Session Service Provider
 *
 *     Code Version
 *         1.0.0
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
package se.natusoft.osgi.aps.misc.time.config;

import groovy.transform.CompileStatic;
import groovy.transform.TypeChecked;
import se.natusoft.osgi.aps.api.core.configold.APSConfig;
import se.natusoft.osgi.aps.api.core.configold.annotation.APSConfigDescription;
import se.natusoft.osgi.aps.api.core.configold.annotation.APSConfigItemDescription;
import se.natusoft.osgi.aps.api.core.configold.annotation.APSDefaultValue;
import se.natusoft.osgi.aps.api.core.configold.model.APSConfigValue;

/**
 * Configuration for APSNTPTimeServiceProvider.
 */
@CompileStatic
@TypeChecked
@APSConfigDescription(
        version = "1.0",
        configId = "se.natusoft.osgi.aps.aps-ntp-time-service-provider",
        group = "misc",
        description = "Configuration of NTP servers."
)
public class NTPConfig extends APSConfig {

    /** This will receive a populated instance of this configold class if it is listed in the APS-Config: manifest entry. */
    public static NTPConfig get;

    @APSConfigItemDescription(
            description = "A comma separated list of NTP servers to get time from."
    )
    public APSConfigValue ntpServers;
}
