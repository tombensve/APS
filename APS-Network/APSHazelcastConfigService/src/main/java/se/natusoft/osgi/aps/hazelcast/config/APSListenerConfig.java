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
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue;

@APSConfigDescription(
        configId = "listeners",
        description = "Provides Hazelcast listener configuration",
        version = "1.0.0"
)
public class APSListenerConfig extends APSConfig {

    @APSConfigItemDescription(
            environmentSpecific = true,
            description = "Identifying name of this config entry for reference in other configs."
    )
    public APSConfigValue name;

    @APSConfigItemDescription(
            environmentSpecific = true,
            description = "A fully qualified name of a class that implements the relevant listener interface."
    )
    public APSConfigValue implementationClassName;

    @APSConfigItemDescription(
            environmentSpecific = true,
            isBoolean = true,
            description = "Select to include values in the events sent to the listener."
    )
    public APSConfigValue includeValue;

    @APSConfigItemDescription(
            environmentSpecific = true,
            isBoolean = true,
            description = "This listener is local (does not apply to all types of listeners!)"
    )
    public APSConfigValue local;
}
