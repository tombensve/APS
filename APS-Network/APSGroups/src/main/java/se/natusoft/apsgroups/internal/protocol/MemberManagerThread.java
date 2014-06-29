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
package se.natusoft.apsgroups.internal.protocol;

import se.natusoft.apsgroups.Debug;
import se.natusoft.apsgroups.config.APSGroupsConfig;
import se.natusoft.apsgroups.internal.net.MulticastTransport;
import se.natusoft.apsgroups.internal.net.Transport;
import se.natusoft.apsgroups.internal.protocol.message.MessagePacket;
import se.natusoft.apsgroups.internal.protocol.message.MessagePacketListener;
import se.natusoft.apsgroups.internal.protocol.message.PacketType;
import se.natusoft.apsgroups.logging.APSGroupsLogger;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

/**
 * This thread handles repeated announcements of members added to the thread. It also
 * handles eviction of expired members for each members group.
 */
public class MemberManagerThread extends Thread implements MessagePacketListener {
    //
    // Singleton instance
    //

    private static MemberManagerThread instance = null;

    //
    // Static access methods
    //

    public static void init(APSGroupsLogger logger, APSGroupsConfig config) {
        if (MemberManagerThread.instance == null) {
            MemberManagerThread.instance = new MemberManagerThread(logger, config);
            MemberManagerThread.instance.start();
        }
    }

    public static MemberManagerThread get() {
        return MemberManagerThread.instance;
    }

    //
    // Private Members
    //

    /** The transport to use to listen to incoming data. */
    private Transport transport = null;

    /** The logger to log to. */
    private APSGroupsLogger logger = null;

    /** Our config. */
    private APSGroupsConfig config = null;

    /** Set to true on start and false on stop. */
    private boolean running = false;

    //
    // Constructors
    //

    /**
     * Creates a new DataReceiverThread.
     *
     * @param logger The logger for the thread to log on.
     * @param config The config to use.
     */
    private MemberManagerThread(APSGroupsLogger logger,  APSGroupsConfig config) {
        super("APSGroups:MemberManagerThread");
        this.logger = logger;
        this.config = config;
        this.transport = new MulticastTransport(this.logger, this.config);
    }

    //
    // Methods
    //

    /**
     * This to avoid having to save the transport outside of this class just to be able to
     * close it again.
     */
    public Transport getTransport() {
        return this.transport;
    }

    /**
     * Terminates the thread.
     */
    public synchronized void terminate() {
        this.running = false;
    }

    /**
     * @return true if we are still supposed to be running.
     */
    private synchronized boolean isRunning() {
        return this.running;
    }

    /**
     * Announces a member.
     *
     * @param member The member to announce.
     *
     * @throws IOException
     */
    private void announceMember(Member member) throws IOException {
        MessagePacket mp = new MessagePacket(member.getGroup(), member, UUID.randomUUID(),0 , PacketType.MEMBER_ANNOUNCEMENT);
        this.transport.send(mp.getPacketBytes());
        member.announced();
    }

    /**
     * The main thread execution.
     */
    @Override
    public void run() {
        try {
            this.transport.open();
            this.running = true;
        }
        catch (IOException ioe) {
            this.logger.error("Failed to open transport! This thread will terminate directly! " +
                    "This is very serious, the APSGroups service will not work correctly due to this!", ioe);
        }
        this.logger.info(
                "==================================\n" +
                "MemberManagerThread Started!\n" +
                "=================================="
        );
        while (isRunning()) {
            try {
                for (String groupName : Groups.getAvailableGroups()) {
                    Group group = Groups.getGroup(groupName);

                    for (Member member : group.getListOfMembers()) {
                        if (member.isLocalMember()) {
                            if (member.lastAnnounced() < 0) {
                                announceMember(member);
                                Debug.println(
                                        "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n" +
                                        "Announced member: " + member.getId() + "\n" +
                                        "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@"
                                );
                            }
                            else {
                                long now = new Date().getTime();
                                long lastAnnounce = member.lastAnnounced();
                                long interval = this.config.getMemberAnnounceInterval() * 1000l;
                                if (now >= (lastAnnounce + interval)) {
                                    announceMember(member);
                                    Debug.println(
                                            "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n" +
                                            "Announced member: " + member.getId() + "\n" +
                                            "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@"
                                    );
                                }
                            }
                        }

                        // Lets also evict old member of the members group.
                        if (member.getGroup() != null) {
                            member.getGroup().evictExpiredMembers();
                        }
                    }
                }

                sleep(2000);
            }
            catch (IOException ioe) {
                this.logger.error("MemberManagerThread: Communication problem!", ioe);
            }
            catch (InterruptedException ie) {
                this.logger.warn("MemberManagerThread: Thread.sleep() was unexpectedly interrupted!");
            }
            catch (Exception e) {
                this.logger.error("MemberManagerThread: Unknown failure!", e);
            }
        }

        try {
            this.transport.close();
        }
        catch (IOException ioe) {
            this.logger.error("Failed to close transport!", ioe);
        }

        MemberManagerThread.instance = null;
    }

    /**
     * Notification of receive of a new MessagePacket.
     *
     * @param messagePacket The received MessagePacket.
     */
    @Override
    public void messagePacketReceived(MessagePacket messagePacket) {
        if (messagePacket.getType() == PacketType.MEMBER_LEAVING) {
            Group msgGroup = messagePacket.getGroup();
            Group localGroup = Groups.getGroup(msgGroup.getName());
            if (localGroup != null) {
                localGroup.removeMember(messagePacket.getMember());
            }
            else {
                this.logger.error("MEMBER_LEAVING packet contained group '" + msgGroup + "' which for some reason does not exist locally!");
            }
        }
    }
}
