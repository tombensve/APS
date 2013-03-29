/*
 *
 * PROJECT
 *     Name
 *         APS Groups
 *     
 *     Code Version
 *         0.9.1
 *     
 *     Description
 *         Provides network groups where named groups can be joined as members and then send and
 *         receive data messages to the group. This is based on multicast and provides a verified
 *         multicast delivery with acknowledgements of receive to the sender and resends if needed.
 *         The sender will get an exception if not all members receive all data. Member actuality
 *         is handled by members announcing themselves relatively often and will be removed when
 *         an announcement does not come in expected time. So if a member dies unexpectedly
 *         (network goes down, etc) its membership will resolve rather quickly. Members also
 *         tries to inform the group when they are doing a controlled exit. Most network aspects
 *         are configurable. Please note that this does not support streaming! That would require
 *         a far more complex protocol. It waits in all packets of a message before delivering
 *         the message.
 *         
 *         Note that even though this is an OSGi bundle, the jar produced can also be used as a
 *         library outside of OSGi. The se.natusoft.apsgroups.APSGroups API should then be used.
 *         This API has no external dependencies, only this jar is required for that use.
 *         
 *         When run with java -jar a for test command line shell will run where you can check
 *         members, send messages and files.
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
 *         2011-10-16: Created!
 *
 */
package se.natusoft.osgi.aps.groups.config;

import se.natusoft.osgi.aps.api.core.config.APSConfig;
import se.natusoft.osgi.aps.api.core.config.ManagedConfig;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigDescription;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigItemDescription;
import se.natusoft.osgi.aps.api.core.config.annotation.APSDefaultValue;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue;

/**
 * Configuration for the APS discovery service.
 */
@APSConfigDescription(
    configId="se.natusoft.osgi.aps.groups",
    group = "network",
    description="Network specific configuration for APSGroups.",
    version="1.0.0"
)
public class APSGroupsServiceConfig extends APSConfig {

    /** This config is auto-managed and will be accessed through this. */
    public static final ManagedConfig<APSGroupsServiceConfig> managed = new ManagedConfig<APSGroupsServiceConfig>();


    @APSConfigItemDescription(description = "The multicast address to use.", defaultValue = {@APSDefaultValue("224.0.0.1")})
    public APSConfigValue multicastAddress;

    @APSConfigItemDescription(description = "The multicast target port to use.", defaultValue = {@APSDefaultValue("58100")})
    public APSConfigValue multicastPort;

    @APSConfigItemDescription(description = "The number of seconds to allow for a send of a message before timeout.",
            defaultValue = {@APSDefaultValue("120")})
    public APSConfigValue sendTimeout;

    @APSConfigItemDescription(description = "The number of seconds to wait before a packet is resent if not acknowledged. " +
            "sendTimeout / resendInterval = the number or resends before giving up.",
            defaultValue = {@APSDefaultValue("5")})
    public APSConfigValue resendInterval;

    @APSConfigItemDescription(description = "The interval in seconds that members announce that they are (still) members. If a member has " +
            "not announced itself again within this time other members of the group will drop the member.",
             defaultValue = {@APSDefaultValue("10")})
    public APSConfigValue memberAnnounceInterval;
}
