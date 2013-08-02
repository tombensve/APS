/* 
 * 
 * PROJECT
 *     Name
 *         APS Groups
 *     
 *     Code Version
 *         0.9.2
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
 *     tommy ()
 *         Changes:
 *         2013-08-02: Created!
 *         
 */
package se.natusoft.osgi.aps.groups.service;

import se.natusoft.apsgroups.config.APSGroupsConfig;
import se.natusoft.apsgroups.internal.protocol.Group;
import se.natusoft.apsgroups.internal.protocol.Groups;
import se.natusoft.apsgroups.internal.protocol.Member;
import se.natusoft.osgi.aps.api.net.groups.service.APSGroupsInfoService;

import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

/**
 * Provides information about current groups and members.
 */
public class APSGroupsInfoServiceProvider implements APSGroupsInfoService {
    //
    // Private Members
    //

    /** Our config. */
    private APSGroupsConfig config = null;

    //
    // Constructors
    //

    /**
     * Creates a new APSGroupsInfoServiceProvider.
     *
     * @param config The service configuration.
     */
    public APSGroupsInfoServiceProvider(APSGroupsConfig config) {
        this.config = config;
    }

    //
    // Methods
    //

    /**
     * Returns the names of all available groups.
     */
    @Override
    public List<String> getGroupNames() {
        List<String> groupNames = new LinkedList<>();
        groupNames.addAll(Groups.getAvailableGroups());
        return groupNames;
    }

    /**
     * Returns a list of member ids for the specified group.
     *
     * @param groupName The name of the group to get member ids for.
     */
    @Override
    public List<String> getGroupMembers(String groupName) {
        Group group = Groups.getGroup(groupName);
        List<String> members = new LinkedList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        for (Member member : group.getListOfMembers()) {
            if (member.isLocalMember()) {
                members.add(member.getId().toString() + " (localMember=true" +
                        ", lastAnnounced='" + sdf.format(member.lastAnnounced()) + "'" +
                        ", stillKicking=" + member.stillKicking(this.config.getMemberAnnounceInterval()) + ")");
            }
            else {
                members.add(member.getId().toString() + " (localMember=false" +
                        ", lastHeardFrom='" + sdf.format(member.getLastHeardFrom().getLocalTimeDate()) + "'" +
                        ", stillKicking=" + member.stillKicking(this.config.getMemberAnnounceInterval()) + ")");
            }
        }

        return members;
    }

    /**
     * Returns a list of "groupName : groupMember" for all groups and members.
     */
    @Override
    public List<String> getGroupsAndMembers() {
        List<String> groupsAndMembers = new LinkedList<>();
        for (String groupName : getGroupNames()) {
            groupsAndMembers.add(groupName);
            for (String groupMember : getGroupMembers(groupName)) {
                groupsAndMembers.add("  " + groupMember);
            }
        }

        return groupsAndMembers;
    }
}
