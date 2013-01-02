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
package se.natusoft.apsgroups.internal.protocol;

import se.natusoft.apsgroups.config.APSGroupsConfig;
import se.natusoft.apsgroups.internal.protocol.message.Message;

import java.util.*;

/**
 * This represents a group.
 */
public class Group {
    //
    // Private Members
    //

    /** The name of the group. */
    private String name = null;

    /** The more or less common time of the group. */
    private NetTime time = null;

    /** The members of this group. */
    private Map<UUID, Member> members = new HashMap<>();

    /** Listeners of which members comes and goes. */
    private List<MemberListener> memberListeners = new LinkedList<>();

    /** Our config. */
    private APSGroupsConfig config = null;

    //
    // Constructors
    //

    /**
     * Creates a group with the specified name.
     *
     * @param name The name of the group.
     */
    public Group(String name) {
        this.name = name;
    }

    //
    // Methods
    //

    /**
     * Sets the time diff decreasing net time object.
     *
     * @param netTime The net time object to set.
     */
    public void setNetTime(NetTime netTime) {
        this.time = netTime;
    }

    /**
     * Provides a config.
     *
     * @param config The provided config.
     */
    public void setConfig(APSGroupsConfig config) {
        this.config = config;
    }

    /**
     * Returns the current config.
     */
    public APSGroupsConfig getConfig() {
        return this.config;
    }

    /**
     * Returns the name of the group.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the more or less common time of the group.
     */
    public NetTime getNetTime() {
        return this.time;
    }

    /**
     * Adds a member to the group.
     *
     * @param member The member to add.
     */
    public synchronized void addMember(Member member) {
        this.members.put(member.getId(), member);
        member.setGroup(this);
        for (MemberListener memberListener : this.memberListeners) {
            memberListener.memberAdded(member);
        }
    }

    /**
     * Removed a member from the group.
     *
     * @param member the member to remove.
     */
    public synchronized void removeMember(Member member) {
        this.members.remove(member.getId());
//        member.setGroup(null); -- This causes problems.
        for (MemberListener memberListener : this.memberListeners) {
            memberListener.memberRemoved(member);
        }
    }

    /**
     * Returns true if the specified member is already part of the group.
     *
     * @param member The member to check.
     */
    public synchronized boolean hasMember(Member member) {
        return this.members.containsKey(member.getId());
    }

    /**
     * @return The group members as a Map.
     */
    public synchronized Map<UUID, Member> getMembers() {
        Map<UUID, Member> membersCopy = new HashMap<>();
        membersCopy.putAll(this.members);
        return membersCopy;
    }

    /**
     * @return The group members as a List.
     */
    public synchronized List<Member> getListOfMembers() {
        List<Member> memberList = new LinkedList<>();

        for (UUID key : this.members.keySet()) {
            Member member = this.members.get(key);
            memberList.add(member);
        }

        return memberList;
    }

    /**
     * Returns a member by its id.
     *
     * @param id The id to get member for.
     */
    public synchronized Member getMemberById(UUID id) {
        return this.members.get(id);
    }

    /**
     * Checks all members if they are still kicking and if not they are evicted.
     */
    public synchronized void evictExpiredMembers() {
        if (this.config == null) return;

        List<UUID> removeKeys = new LinkedList<>();

        for (UUID key : this.members.keySet()) {
            Member member = this.members.get(key);
            if (!member.isLocalMember() && !member.stillKicking(this.config.getMemberAnnounceInterval())) {
                removeKeys.add(key);
            }
        }
        for (UUID removeKey: removeKeys) {
            this.members.remove(removeKey);
        }
    }

    /**
     * Adds a listener for member change information.
     *
     * @param memberListener The listener to add.
     */
    public synchronized void addMemberListener(MemberListener memberListener) {
        this.memberListeners.add(memberListener);
    }

    /**
     * Removes a previously added member listener.
     *
     * @param memberListener The listener to remove.
     */
    public synchronized void removeMemberListener(MemberListener memberListener) {
        this.memberListeners.remove(memberListener);
    }

    /**
     * @return The number of members.
     */
    public synchronized int getNumberOfMembers() {
        return this.members.size() - 1; // We exclude ourself!
    }

    /**
     * Creates and returns a new Message belonging to this group.
     *
     * @param member The member creating the message.
     */
    public Message createNewMessage(Member member) {
        return new Message(this, member);
    }

    /**
     * Compares for equality.
     *
     * @param obj The object to compare to.
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Group)) return false;
        Group grp = (Group)obj;
        return this.name.equals(grp.getName());
    }

    //
    // Inner Classes
    //

    /**
     * API for member listeners.
     */
    public static interface MemberListener {
        /**
         * Called when a new member is added.
         *
         * @param member The added member.
         */
        public void memberAdded(Member member);

        /**
         * Called when a member is removed.
         *
         * @param member The removed member.
         */
        public void memberRemoved(Member member);
    }
}
