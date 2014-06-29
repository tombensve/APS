/* 
 * 
 * PROJECT
 *     Name
 *         APS Groups
 *     
 *     Code Version
 *         0.11.0
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
package se.natusoft.apsgroups;

import se.natusoft.apsgroups.config.APSGroupsConfig;
import se.natusoft.apsgroups.config.APSGroupsConfigProvider;
import se.natusoft.apsgroups.internal.GroupMemberProvider;
import se.natusoft.apsgroups.internal.net.MulticastTransport;
import se.natusoft.apsgroups.internal.net.Transport;
import se.natusoft.apsgroups.internal.protocol.*;
import se.natusoft.apsgroups.logging.APSGroupsLogger;
import se.natusoft.apsgroups.logging.APSGroupsSystemOutLogger;

import java.io.IOException;

/**
 * This is the main API when using APSGroups as a library.
 * <p/>
 * Create an instance of this. Call connect() before using, and disconnect() when done.
 */
public class APSGroups {
    //
    // Private Members
    //

    /** Manages net time with other groups on possible other hosts. */
    private NetTimeThread netTimeThread = null;

    /** For logging. */
    private APSGroupsLogger logger = null;

    /** Our config. */
    private APSGroupsConfig config = null;

    /** This instance holds our copy of net time. This gets updated by the NetTimeThread. */
    private NetTime netTime = null;

    //
    // Constructors
    //

    /**
     * Creates a new APSGroups instance.
     *
     * @param config The configuration to use.
     * @param logger The logger to log to.
     */
    public APSGroups(APSGroupsConfig config, APSGroupsLogger logger) {
        this.config = config;
        this.logger = logger;
        DataReceiverThread.init(this.logger, this.config);
        MemberManagerThread.init(this.logger, this.config);
    }

    /**
     * Creates a new APSGroups instance with default config and that logs to System.out.
     */
    public APSGroups() {
        this.config = new APSGroupsConfigProvider();
        this.logger = new APSGroupsSystemOutLogger();
    }

    //
    // Methods
    //

    /**
     * Starts upp and connects the groups engine. Nothing will work until this has been called and
     * should therefore be the first thing called after construction of instance.
     *
     * @throws IOException on failure to connect.
     */
    public void connect() throws IOException {
        Transport transport = new MulticastTransport(this.logger, this.config);
        transport.open();

        Transport memberTransport = new MulticastTransport(this.logger, this.config);
        memberTransport.open();
        DataReceiverThread.get().addMessagePacketListener(MemberManagerThread.get());

        Transport netTimeTransport = new MulticastTransport(this.logger, this.config);
        netTimeTransport.open();
        this.netTime = new NetTime();
        this.netTimeThread = new NetTimeThread(this.netTime, this.logger, netTimeTransport);
        DataReceiverThread.get().addMessagePacketListener(this.netTimeThread);
        this.netTimeThread.start();

    }

    /**
     * Shuts down and disconnects the groups engine. After this call the functionality is dead until
     * connect() is called again.
     *
     * @throws IOException
     */
    public void disconnect() {
        this.netTimeThread.terminate();
        try {this.netTimeThread.getTransport().close();} catch (IOException ioe) {
            this.logger.error("Failed to close net time transport!", ioe);
        }
    }

    /**
     * Joins a group.
     *
     * @param name The name of the group to join.
     *
     * @return A GroupMember that provides the API for sending and receiving data in the group.
     *
     * @throws java.io.IOException The unavoidable one!
     */
    public GroupMember joinGroup(String name) throws IOException {
        if (name.equals("[net time]")) {
            throw new IOException("The group name '[net time]' is reserved for internal use and cannot be used.");
        }
        Group group = Groups.getGroup(name);
        if (group.getNetTime() == null) {
            group.setNetTime(this.netTime);
        }
        if (group.getConfig() == null) {
            group.setConfig(this.config);
        }
        Member member = new Member();
        group.addMember(member);
        GroupMemberProvider groupMember = new GroupMemberProvider(member, logger, this.config);
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
    public void leaveGroup(GroupMember groupMember) throws IOException {
        GroupMemberProvider gmp = (GroupMemberProvider)groupMember;
        gmp.close();
        Member member = gmp.getMember();
        member.getGroup().removeMember(member);
    }
}
