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
package se.natusoft.apsgroups.internal.protocol;

import se.natusoft.apsgroups.Debug;
import se.natusoft.apsgroups.config.APSGroupsConfig;
import se.natusoft.apsgroups.internal.net.Transport;
import se.natusoft.apsgroups.internal.protocol.message.MessagePacket;
import se.natusoft.apsgroups.internal.protocol.message.MessagePacketListener;
import se.natusoft.apsgroups.internal.protocol.message.PacketType;
import se.natusoft.apsgroups.logging.APSGroupsLogger;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * This thread handles repeated announcements of members added to the thread. It also
 * handles eviction of expired members for each members group.
 */
public class MemberManagerThread extends Thread implements MemberManager, MessagePacketListener {
    //
    // Private Members
    //

    /** The member to announce. */
    private List<Member> members = new LinkedList<>();

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
     * @param transport The transport to use for reading data messages.
     * @param config The config to use.
     */
    public MemberManagerThread(APSGroupsLogger logger, Transport transport, APSGroupsConfig config) {
        this.logger = logger;
        this.transport = transport;
        this.config = config;
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
     * Starts the thread.
     */
    @Override
    public void start() {
        this.running = true;
        super.start();
    }

    /**
     * Terminates the thread.
     */
    public synchronized void terminate() {
        this.running = false;
    }

    /**
     * Adds a member.
     *
     * @param member The member to add.
     */
    public synchronized void addMember(Member member) {
        this.members.add(member);
    }

    /**
     * Removes a member.
     *
     * @param member The member to remove.
     */
    public synchronized void removeMember(Member member) {
        this.members.remove(member);
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
        while (isRunning()) {
            try {
                for (Member member : members) {
                    if (member.lastAnnounced() < 0) {
                        announceMember(member);
                    }
                    else {
                        long now = new Date().getTime();
                        long lastAnnounce = member.lastAnnounced();
                        long interval = this.config.getMemberAnnounceInterval() * 1000l;
                        Debug.println2("" + now + " >= " + (lastAnnounce + interval) + " : " + (now >= (lastAnnounce + interval)));
                        if (now >= (lastAnnounce + interval)) {
                            announceMember(member);
                        }
                    }

                    // Lets also evict old member of the members group.
                    member.getGroup().evictExpiredMembers();
                }
                try {Thread.sleep(1000);} catch (InterruptedException ie) {
                    this.logger.warn("MemberManagerThread: Thread.sleep() was unexpectedly interrupted! If this happens consecutively " +
                        "too often it can cause highly increased CPU usage!");
                }
            }
            catch (IOException ioe) {
                this.logger.error("MemberManagerThread: Communication problem!", ioe);
            }
            catch (Exception e) {
                this.logger.error("MemberManagerThread: Unknown failure!", e);
            }
        }
    }

    /**
     * Notification of receive of a new MessagePacket.
     *
     * @param messagePacket The received MessagePacket.
     */
    @Override
    public void messagePacketReceived(MessagePacket messagePacket) {
        if (messagePacket.getType() == PacketType.MEMBER_LEAVING) {
            for (Member member : this.members) {
                if (member.getGroup().equals(messagePacket.getGroup())) {
                    Group group = member.getGroup();
                    group.removeMember(messagePacket.getMember());
                }
            }
        }
    }
}
