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
 *     Tommy Svensson (tommy@natusoft.se)
 *         Changes:
 *         2012-12-28: Created!
 *         
 */
package se.natusoft.osgi.aps.groups.service;

import se.natusoft.apsgroups.config.APSGroupsConfig;
import se.natusoft.apsgroups.internal.net.*;
import se.natusoft.apsgroups.internal.protocol.*;
import se.natusoft.apsgroups.logging.APSGroupsLogger;
import se.natusoft.osgi.aps.api.core.config.event.APSConfigChangedEvent;
import se.natusoft.osgi.aps.api.core.config.event.APSConfigChangedListener;
import se.natusoft.osgi.aps.api.net.groups.service.APSGroupsService;
import se.natusoft.osgi.aps.api.net.groups.service.GroupMember;
import se.natusoft.osgi.aps.groups.config.APSGroupsServiceConfig;

import java.io.IOException;
import java.util.Properties;

/**
 * This is the service API implementation.
 */
public class APSGroupsServiceProvider implements APSGroupsService, APSConfigChangedListener {
    //
    // Private Members
    //

    /** Manages members. */
    private MemberManagerThread memberManagerThread = null;

    /** Handles receiving of all network packets. */
    private DataReceivers receivers = null;

    /** Manages net time with other groups on possible other hosts. */
    private NetTimeThread netTimeThread = null;

    /** This instance holds our copy of net time. This gets updated by the NetTimeThread. */
    private NetTime netTime = null;

    /** The configured transports.  */
    private Transports transports = null;

    /** Will be set to true on first connect. */
    private boolean connected = false;


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
     * Event listener callback when event occurs.
     *
     * @param event information about the event.
     */
    @Override
    public void apsConfigChanged(APSConfigChangedEvent event) {
        try {
            disconnect();
            connect();
        }
        catch (IOException ioe) {
            this.logger.error("disconnect+(re)connect due to config change failed!", ioe);
        }
    }

    private APSGroupsServiceConfig getConfig() {
        if (!APSGroupsServiceConfig.managed.isManaged()) {
            APSGroupsServiceConfig.managed.waitUtilManaged();
        }

        return APSGroupsServiceConfig.managed.get();
    }

    /**
     * Starts upp and connects the groups engine. Nothing will work until this has been called and
     * should therefore be the first thing called after construction of instance.
     *
     * @throws java.io.IOException on failure to connect.
     */
    private synchronized void connect() throws IOException {
        getConfig().addConfigChangedListener(this);

        this.transports = new Transports(this.logger);
        for (APSGroupsConfig.TransportConfig transportConfig : this.config.getTransports()) {
            switch (transportConfig.getTransportType()) {
                case MULTICAST:
                    this.transports.addMulticastTransport(new MulticastTransport(this.logger, transportConfig));
                    break;

                case TCP_SENDER:
                    this.transports.addSendingTCPTransport(new TCPSendTransport(this.logger, transportConfig));
                    break;

                case TCP_RECEIVER:
                    this.transports.addReceivingTCPTransport(new TCPReceiveTransport(this.logger, transportConfig));
                    break;
            }
        }
        this.transports.openTransports();

        this.receivers = new DataReceivers();
        for (Transport transport : this.transports.getReceivingTransports()) {
            DataReceiverThread receiver = new DataReceiverThread(this.logger, transport, this.config);
            receiver.start();
            this.receivers.add(receiver);
        }

        this.memberManagerThread = new MemberManagerThread(this.logger, this.transports, this.config);
        this.receivers.addMessagePacketListener(this.memberManagerThread);
        this.memberManagerThread.start();

        this.netTime = new NetTime();
        this.netTimeThread = new NetTimeThread(this.netTime, this.logger, this.transports);
        this.receivers.addMessagePacketListener(this.netTimeThread);
        this.netTimeThread.start();
        this.connected = true;
    }

    /**
     * Shuts down and disconnects the groups engine. After this call the functionality is dead until
     * connect() is called again.
     *
     * @throws IOException
     */
    public synchronized void disconnect() throws IOException{
        this.connected = false;
        getConfig().removeConfigChangedListener(this);
        if (this.receivers != null) {
            for (DataReceiver receiver : this.receivers) {
                ((DataReceiverThread)receiver).terminate();
            }
        }
        if (this.memberManagerThread != null) this.memberManagerThread.terminate();
        if (this.netTimeThread != null) this.netTimeThread.terminate();
        if (transports != null) this.transports.closeTransports();
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
    private synchronized void checkConnect() throws IOException {
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
        return joinGroup(name, new Properties());
    }

    /**
     * Joins a group.
     *
     * @param name           The name of the group to join.
     * @param memberUserData Data provided by users of the service.
     * @return A GroupMember that provides the API for sending and receiving data in the group.
     * @throws java.io.IOException The unavoidable one!
     */
    @Override
    public GroupMember joinGroup(String name, Properties memberUserData) throws IOException {
        if (name.equals("[net time]")) {
            throw new IOException("The group name '[net time]' is reserved for internal use and cannot be used.");
        }

        checkConnect();

        Group group = Groups.getGroup(name);
        group.setNetTime(this.netTime);
        Member member = new Member(memberUserData);
        group.addMember(member);
        this.memberManagerThread.addMember(member);
        GroupMemberProvider groupMember = new GroupMemberProvider(member, this.receivers, this.transports, logger);
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
        this.memberManagerThread.removeMember(member);
        member.getGroup().removeMember(member);
    }

}
