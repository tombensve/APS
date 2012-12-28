/* 
 * 
 * PROJECT
 *     Name
 *         APS APSNetworkGroups
 *     
 *     Code Version
 *         0.9.0
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
