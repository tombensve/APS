/* 
 * 
 * PROJECT
 *     Name
 *         APSGroups
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
 *         are configurable.
 *         
 *         Note that even though this is an OSGi bundle, the jar produced can also be used as a
 *         library outside of OSGi. The se.natusoft.apsgroups.APSGroups API should then be used.
 *         This API has no external dependencies, only this jar is required for that use.
 *         
 *         When run with java -jar a for test command line shell will run where you can check
 *         members, send messages and files and other things.
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
package se.natusoft.osgi.aps.groups.service;

import se.natusoft.apsgroups.internal.protocol.*;
import se.natusoft.apsgroups.logging.APSGroupsLogger;
import se.natusoft.osgi.aps.api.net.groups.service.APSGroupsService;
import se.natusoft.osgi.aps.api.net.groups.service.GroupMember;

import java.io.IOException;

/**
 * This is the service API implementation.
 */
public class APSGroupsServiceProvider implements APSGroupsService {
    //
    // Private Members
    //

    /** Our logger. */
    private APSGroupsLogger logger = null;

    /** We need to add and remove member to/from this. */
    private MemberManager memberManager = null;

    /** Needed to setup for incoming messages. */
    private DataReceiver dataReceiver = null;

    /** For decreasing time differences on network hosts. */
    private NetTime netTime = null;

    //
    // Constructors
    //

    /**
     * Creates a new APSNetworkGroupService instance.
     *
     * @param memberManager For adding and removing members.
     * @param dataReceiver For setup of incomming messages.
     * @param netTime For decreasing time differences on network hosts.
     * @param logger For logging to.
     */
    public APSGroupsServiceProvider(MemberManager memberManager, DataReceiver dataReceiver, NetTime netTime, APSGroupsLogger logger) {
        this.memberManager = memberManager;
        this.dataReceiver = dataReceiver;
        this.netTime = netTime;
        this.logger = logger;
    }

    //
    // Methods
    //

    /**
     * Joins a group.
     *
     * @param name The name of the group to join.
     *
     * @return A GroupMember that provides the API for sending and receiving data in the group.
     *
     * @throws IOException The unavoidable one!
     */
    @Override
    public GroupMember joinGroup(String name) throws IOException {
        if (name.equals("[net time]")) {
            throw new IOException("The group name '[net time]' is reserved for internal use and cannot be used.");
        }
        Group group = Groups.getGroup(name);
        group.setNetTime(this.netTime);
        Member member = new Member();
        group.addMember(member);
        this.memberManager.addMember(member);
        GroupMemberProvider groupMember = new GroupMemberProvider(member, this.dataReceiver, logger);
        groupMember.open();

        return groupMember;
    }

    /**
     * Leaves as member of group.
     *
     * @param groupMember The GroupMember returned when joined.
     *
     * @throws IOException The unavoidable one!
     */
    @Override
    public void leaveGroup(GroupMember groupMember) throws IOException {
        GroupMemberProvider gmp = (GroupMemberProvider)groupMember;
        gmp.close();
        Member member = gmp.getMember();
        this.memberManager.removeMember(member);
        member.getGroup().removeMember(member);
    }
}
