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
package se.natusoft.apsgroups.internal.protocol.message;

/**
 * This defines the type of a packet.
 */
public enum PacketType {

    /** This is any part of a full message except the last packet. This is only delivered within a group. */
    MESSAGE_PART,

    /** This is the last packet of a full message. This is only delivered within a group. */
    MESSAGE_END,

    /** This is an acknowledgement of a received packet. This is only delivered within a group. */
    ACKNOWLEDGEMENT,

    /** This is a member announcing itself to the group. This is only delivered withing a group. */
    MEMBER_ANNOUNCEMENT,

    /** This is sent when a member leaves a group. */
    MEMBER_LEAVING,

    /** This is a timestamp for time synchronization. This is delivered to all groups. */
    NET_TIME
}
