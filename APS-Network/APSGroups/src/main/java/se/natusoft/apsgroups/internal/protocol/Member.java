/* 
 * 
 * PROJECT
 *     Name
 *         APS Groups
 *     
 *     Code Version
 *         0.10.0
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

import se.natusoft.apsgroups.Debug;
import se.natusoft.apsgroups.internal.protocol.NetTime.Time;

import java.util.Date;
import java.util.UUID;
/**
 * Represents a member of a group. This is the internal member representation. The end user member is GroupMember.
 */
public class Member {
    //
    // Private Members
    //

    /** The group the member belongs to. */
    private Group group = null;

    /** The id of the member. */
    private UUID id = null;

    /** The last time this member showed signs of life. */
    private Time lastHeardFromTime = null;

    /** This is set to true if this is a local member. */
    private boolean localMember = false;

    /** A timestamp of when this member was last announced. */
    private long lastAnnounced = -1;

    //
    // Constructors
    //

    /**
     * Creates a new Member.
     */
    public Member() {
        this.id = UUID.randomUUID();
        this.localMember = true;
        updateLastHeardFrom();
    }

    /**
     * Creates a new remote Member.
     *
     * @param id The id of the member.
     */
    public Member(UUID id) {
        this.id = id;
    }

    //
    // Methods
    //

    /**
     * Sets the group this member is part of.
     *
     * @param group The group to set.
     */
    public synchronized void setGroup(Group group) {
        this.group = group;
        updateLastHeardFrom();
    }

    /**
     * Returns the group this member belongs to.
     */
    public synchronized Group getGroup() {
        return this.group;
    }

    /**
     * Returns true if this is a local member, false it is a remote member.
     */
    public boolean isLocalMember() {
        return this.localMember;
    }

    /**
     * Returns the id of this member.
     */
    public UUID getId() {
        return this.id;
    }

    // TODO: The following 2 methods doesn't really need to use net time! It doesn't matter that they do however.

    /**
     * Updates the internal time this member was last heard from.
     */
    public void updateLastHeardFrom() {
        if (this.group != null && this.group.getNetTime() != null) {
            this.lastHeardFromTime = this.group.getNetTime().getCurrentNetTime();
        }
    }

    /**
     * Returns the last time heard from if this is a remote member.
     */
    public Time getLastHeardFrom() {
        return this.lastHeardFromTime;
    }

    /**
     * Returns true if we believe this member is still kicking (we can never be absolutely sure).
     *
     * @param memberAnnounceInterval The configured member announce interval.
     */
    public boolean stillKicking(int memberAnnounceInterval) {
        if (this.localMember) return true;

        boolean kicking = false;
        if (this.group != null) {
            Time now = this.group.getNetTime().getCurrentNetTime();

            Time expire = this.lastHeardFromTime.clone();
            expire.addToTime(memberAnnounceInterval * 1000);

            kicking = now.compareTo(expire) == 1; // now > lastHeardFromTime + expire interval.


            Debug.println2("Member:" + this.id + ", now:" + now + ", " +
                    "expire:" + expire + ", kicking:" + kicking);
        }
        return kicking;
    }

    /**
     * Tells this member that it was just announced.
     */
    public void announced() {
        this.lastAnnounced = new Date().getTime();
    }

    /**
     * @return timestamp of when this was last announced if this is a local member.
     */
    public long lastAnnounced() {
        return this.lastAnnounced;
    }

    /**
     * Compares members for equality.
     *
     * @param obj The object to compare to.
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof Member)) return false;
        Member member = (Member)obj;
        return this.id.equals(member.id) && this.group.getName().equals(member.getGroup().getName());
    }
}
