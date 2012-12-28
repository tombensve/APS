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
package se.natusoft.apsgroups.config;

/**
 * This respresents the configuration values needed by netgroups.
 */
public interface APSGroupsConfig {

    /**
     * The multicast address to use.
     */
    public String getMulticastAddress();

    /**
     * The multicast target port to use.
     */
    public int getMulticastPort();

    /**
     * The number of seconds to allow for a send of a message before timeout.
     */
    public int getSendTimeout();

    /**
     * The number of seconds to wait before a packet is resent if not acknowledged.
     */
    public int getResendInterval();

    /**
     * The interval in seconds that members announce that they are (sill) members. If a member has
     * not announced itself again within this time other members of the group will drop the member.
     */
    public int getMemberAnnounceInterval();

}
