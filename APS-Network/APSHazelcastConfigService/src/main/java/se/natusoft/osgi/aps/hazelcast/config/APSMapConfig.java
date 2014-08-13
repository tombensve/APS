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
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValueList;

@APSConfigDescription(
        configId = "maps",
        description = "Provides Hazelcast maps configuration",
        version = "1.0.0"
)
public class APSMapConfig extends APSConfig {

    @APSConfigItemDescription(
            environmentSpecific = true,
            description = "The name of the map."
    )
    public APSConfigValue name;

    @APSConfigItemDescription(
            environmentSpecific = true,
            description = "The asynchronous backup count."
    )
    public APSConfigValue asyncBackupCount;

    @APSConfigItemDescription(
            environmentSpecific = true,
            description = "The backup count."
    )
    public APSConfigValue backupCount;

    @APSConfigItemDescription(
            environmentSpecific = true,
            description = "The eviction procentage when max is reached."
    )
    public APSConfigValue evictionProcentage;

    @APSConfigItemDescription(
            environmentSpecific = true,
            description = "The maximumsize of the map."
    )
    public APSConfigValue maxSize;

    @APSConfigItemDescription(
            environmentSpecific = true,
            description = "The max idle seconds."
    )
    public APSConfigValue maxIdleSeconds;

    @APSConfigItemDescription(
            environmentSpecific = true,
            isBoolean = true,
            description = "Select to optimize queries."
    )
    public APSConfigValue optimizeQueries;

    @APSConfigItemDescription(
            environmentSpecific = true,
            isBoolean = true,
            description = "Select to read backup data."
    )
    public APSConfigValue readBackupData;

    @APSConfigItemDescription(
            environmentSpecific = true,
            isBoolean = true,
            description = "Select to enable statistics."
    )
    public APSConfigValue statisticsEnabled;

    @APSConfigItemDescription(
            environmentSpecific = true,
            description = "The time to live."
    )
    public APSConfigValue timeToLive;

    @APSConfigItemDescription(
            environmentSpecific = true,
            description = "A name of a listener configured in 'listeners'. Must implement EntryListener."
    )
    public APSConfigValueList entryListeners;

    @APSConfigItemDescription(
            environmentSpecific = true,
            description = "The name of a store configured in 'stores'. This must implement MapStore."
    )
    public APSConfigValue mapStore;

}
