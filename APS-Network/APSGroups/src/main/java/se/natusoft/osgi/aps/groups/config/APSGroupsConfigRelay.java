/* 
 * 
 * PROJECT
 *     Name
 *         APS Groups
 *     
 *     Code Version
 *         0.9.0
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
 *         2012-12-28: Created!
 *         
 */
package se.natusoft.osgi.aps.groups.config;

import se.natusoft.apsgroups.config.APSGroupsConfig;

/**
 * Provides an implementation of APSGroupsConfig that relays to APSGroupsServiceConfig.
 */
public class APSGroupsConfigRelay implements APSGroupsConfig {

    /**
     * Checks if config is managed and if not wait until it is managed.
     */
    private void checkManaged() {
        if (!APSGroupsServiceConfig.managed.isManaged()) {
            APSGroupsServiceConfig.managed.waitUtilManaged();
        }
    }

    /**
     * The multicast address to use.
     */
    @Override
    public String getMulticastAddress() {
        checkManaged();
        return APSGroupsServiceConfig.managed.get().multicastAddress.toString();
    }

    /**
     * The multicast target port to use.
     */
    @Override
    public int getMulticastPort() {
        checkManaged();
        return APSGroupsServiceConfig.managed.get().multicastPort.toInt();
    }

    /**
     * The number of seconds to allow for a send of a message before timeout.
     */
    @Override
    public int getSendTimeout() {
        checkManaged();
        return APSGroupsServiceConfig.managed.get().sendTimeout.toInt();
    }

    /**
     * The number of seconds to wait before a packet is resent if not acknowledged.
     */
    @Override
    public int getResendInterval() {
        checkManaged();
        return APSGroupsServiceConfig.managed.get().resendInterval.toInt();
    }

    /**
     * The interval in seconds that members announce that they are (sill) members. If a member has
     * not announced itself again within this time other members of the group will drop the member.
     */
    @Override
    public int getMemberAnnounceInterval() {
        checkManaged();
        return APSGroupsServiceConfig.managed.get().memberAnnounceInterval.toInt();
    }
}
