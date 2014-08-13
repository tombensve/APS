/* 
 * 
 * PROJECT
 *     Name
 *         APS Groups
 *     
 *     Code Version
 *         1.0.0
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
package se.natusoft.osgi.aps.groups.service;

import se.natusoft.apsgroups.config.APSGroupsConfig;
import se.natusoft.apsgroups.internal.net.MulticastTransport;
import se.natusoft.apsgroups.internal.net.Transport;
import se.natusoft.apsgroups.internal.protocol.*;
import se.natusoft.apsgroups.logging.APSGroupsLogger;
import se.natusoft.osgi.aps.api.net.groups.service.APSGroupsService;
import se.natusoft.osgi.aps.api.net.groups.service.GroupMember;

import java.io.IOException;
import java.util.Properties;

/**
 * This is the service API implementation.
 */
public class APSGroupsServiceProvider implements APSGroupsService {
    //
    // Private Members
    //

    /** Manages net time with other groups on possible other hosts. */
    private NetTimeThread netTimeThread = null;

    /** This instance holds our copy of net time. This gets updated by the NetTimeThread. */
    private NetTime netTime = null;

    /** Will be set to true on first connect. */
    private boolean connected = false;

    /** The transport to send with. */
    private Transport sendTransport = null;

    /** For logging. */
    private APSGroupsLogger logger = null;

    /** Our config. */
    private APSGroupsConfig config = null;

    //
    // Constructors
    //

    /**
     * Creates a new APSNetworkGroupService instance.
     *
     * @param config The config to use.
     * @param logger For logging to.
     */
    public APSGroupsServiceProvider(APSGroupsConfig config, APSGroupsLogger logger) {
        this.config = config;
        this.logger = logger;
    }

    //
    // Methods
    //


    /**
     * Starts upp and connects the groups engine. Nothing will work until this has been called and
     * should therefore be the first thing called after construction of instance.
     *
     * @throws java.io.IOException on failure to connect.
     */
    private void connect() throws IOException {
        this.sendTransport = new MulticastTransport(this.logger, this.config);
        this.sendTransport.open();

        DataReceiverThread.get().addMessagePacketListener(MemberManagerThread.get());

        Transport netTimeTransport = new MulticastTransport(this.logger, this.config);
        netTimeTransport.open();
        this.netTime = new NetTime();
        this.netTimeThread = new NetTimeThread(this.netTime, this.logger, netTimeTransport);
        DataReceiverThread.get().addMessagePacketListener(this.netTimeThread);
        this.netTimeThread.start();

        this.connected = true;
    }

    /**
     * Shuts down and disconnects the groups engine. After this call the functionality is dead until
     * connect() is called again.
     *
     * @throws IOException
     */
    public void disconnect()  {

        DataReceiverThread.get().removeMessagePacketListener(MemberManagerThread.get());

        if (this.netTimeThread != null) {
            DataReceiverThread.get().removeMessagePacketListener(this.netTimeThread);
            this.netTimeThread.terminate();
            try {this.netTimeThread.getTransport().close();} catch (IOException ioe) {
                this.logger.error("Failed to close net time transport!", ioe);
            }
            this.netTimeThread = null;
        }

        try {
            this.sendTransport.close();
        }
        catch (IOException ioe ) {
            this.logger.error("Failed to close send transport! [" + this.sendTransport + "]", ioe);
        }

        this.connected = false;
    }

    /**
     * Setting up the connection requires reading the config, which is using an APSConfigService configuration
     * model that is managed by the APSConfigService. Since it is managed by the service the config cannot
     * be accessed until it *is* managed by the service. This service might start before the config is managed.
     * Thereby the APSGroupsConfigRelay wrapper calls managed.waitUntilManaged() to make sure it is managed
     * before accessing the config values. This call cannot be made on construction of this service since
     * that is called by the bundle activator and would hang in a deadlock. So the safest time to assure
     * we have a managed config is on first service call. By then the config is almost surely managed, but
     * if it isn't yet, it is safe to wait for it to become managed.
     */
    private void checkConnect() throws IOException {
        if (!this.connected) {
            try {
                connect();
            }
            catch (IOException ioe) {
                disconnect();
                throw ioe;
            }
        }
    }

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

        checkConnect();

        Group group = Groups.getGroup(name);
        group.setNetTime(this.netTime);
        Member member = new Member();
        group.addMember(member);
        GroupMemberProvider groupMember = new GroupMemberProvider(member, this.sendTransport, logger);
        groupMember.open();

        return groupMember;
    }

    /**
     * Joins a group.
     *
     * @param name The name of the group to join.
     * @param props Properties of group. Not yet supported!
     *
     * @return A GroupMember that provides the API for sending and receiving data in the group.
     *
     * @throws IOException The unavoidable one!
     */
    @Override
    public GroupMember joinGroup(String name, Properties props) throws IOException {
        return joinGroup(name);
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
        member.getGroup().removeMember(member);
    }
}
