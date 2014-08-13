/* 
 * 
 * PROJECT
 *     Name
 *         APS Hazelcast Networking Config Service
 *     
 *     Code Version
 *         1.0.0
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
 *         2014-08-13: Created!
 *         
 */
package se.natusoft.osgi.aps.hazelcast.config;

import se.natusoft.osgi.aps.api.core.config.APSConfig;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigDescription;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigItemDescription;
import se.natusoft.osgi.aps.api.core.config.annotation.APSDefaultValue;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue;

@APSConfigDescription(
        configId = "multicast",
        description = "Provides Hazelcast multicast configuration",
        version = "1.0.0"
)
public class APSMulticastConfig extends APSConfig {


    @APSConfigItemDescription(
            environmentSpecific = true,
            defaultValue = @APSDefaultValue("false"),
            isBoolean = true,
            description = "Enable/disable config."
    )
    public APSConfigValue configEnabled;

    @APSConfigItemDescription(
            environmentSpecific = true,
            defaultValue = @APSDefaultValue("224.2.2.3"),
            description = "The multicast group address to use. Default 224.2.2.3."
    )
    public APSConfigValue group;

    @APSConfigItemDescription(
            environmentSpecific = true,
            defaultValue = @APSDefaultValue("54327"),
            description = "The port to use. Default 54327."
    )
    public APSConfigValue port;

    @APSConfigItemDescription(
            environmentSpecific = true,
            defaultValue = @APSDefaultValue("2"),
            description = "The timeout to use. Default 2 seconds."
    )
    public APSConfigValue timeout;

    @APSConfigItemDescription(
            environmentSpecific = true,
            defaultValue = @APSDefaultValue("32"),
            description = "The 'time to live' value. Default 32."
    )
    public APSConfigValue timeToLive;

}
