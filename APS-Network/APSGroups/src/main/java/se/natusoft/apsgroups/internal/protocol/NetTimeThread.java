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
package se.natusoft.apsgroups.internal.protocol;

import se.natusoft.apsgroups.Debug;
import se.natusoft.apsgroups.internal.net.Transport;
import se.natusoft.apsgroups.internal.protocol.message.MessagePacket;
import se.natusoft.apsgroups.internal.protocol.message.MessagePacketListener;
import se.natusoft.apsgroups.internal.protocol.message.PacketType;
import se.natusoft.apsgroups.logging.APSGroupsLogger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

/**
 * This thread listens for and possibly sends net time.
 * <p/>
 * It works like this:
 * <ul>
 *     <li>
 *         It can operate in 2 modes:
 *         <ul>
 *             <li>
 *                 Listening mode. In this mode it listens to time packets and updates the NetTime with the diff to local time.
 *             </li>
 *             <li>
 *                 Sender mode. In this mode it takes its own time and sends to other groups and members. Please note that
 *                 the time sent is not the local time, but the net time. In case no time packets have been received from
 *                 elsewhere when sending the sent net time will match the local time. Otherwise the sent time will be the
 *                 net time based on the last received time.
 *             </li>
 *         </ul>
 *     </li>
 *     <li>
 *         It listens to time packets from network and updates local time diff in NetTime when a packet is received.
 *         This will also set it in listening mode.
 *     </li>
 *     <li>
 *         At start it waits for 20 seconds and if no time has been received by then it switches to sender mode.
 *     </li>
 *     <li>
 *         If it is in listening mode and no new time packet has arrived for 20 seconds it switches over to sender mode.
 *     </li>
 * </ul>
 */
public class NetTimeThread extends Thread implements MessagePacketListener {
    //
    // Private Members
    //

    /** The NetTime instance representing network group time. */
    private NetTime netTime = null;

    /** The transport to use to listen to incoming data. */
    private Transport transport = null;

    /** The logger to log to. */
    private APSGroupsLogger logger = null;

    /** Set to true on start and false on stop. */
    private boolean running = false;

    /** The time we last received a NET_TIME packet. */
    private long lastReceivedTimeTime = -1;

    /** The mode. If true we are in sender mode. If false we are in listening mode. */
    private boolean timeSender = false;

    /** This is a member not belonging to any group, only representing net time. */
    private Member netTimeMember = null;

    /** The net time group.  */
    private Group netTimeGroup = null;

    //
    // Constructors
    //

    /**
     * Creates a new DataReceiverThread.
     *
     * @param netTime The network time representative.
     * @param logger The logger for the thread to log on.
     * @param transport The transport to use for reading data messages.
     */
    public NetTimeThread(NetTime netTime, APSGroupsLogger logger, Transport transport) {
        this.netTime = netTime;
        this.logger = logger;
        this.transport = transport;

        // The net time group and member are kind of dummies needed since the net time packet is a MessagePacket
        // which is expected to have both a group and a member. No messages are specifically sent on this group.
        this.netTimeGroup = Groups.getGroup("[net time]");
        this.netTimeMember = new Member();
        this.netTimeGroup.addMember(netTimeMember);
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
     * @return true if we are still supposed to be running.
     */
    private synchronized boolean isRunning() {
        return this.running;
    }

    /**
     * Send the local time to other members in all groups.
     *
     * @throws java.io.IOException
     */
    private void sendTime() throws IOException {
        MessagePacket mp = new MessagePacket(netTimeGroup, netTimeMember, UUID.randomUUID(), 0, PacketType.NET_TIME);
        DataOutputStream dos = new DataOutputStream(mp.getOutputStream());
        dos.writeLong(this.netTime.getCurrentNetTime().getTimeValue());
        dos.close();
        this.transport.send(mp.getPacketBytes());
    }

    /**
     * The main thread execution.
     */
    @Override
    public void run() {
        // At start we wait 20 seconds.
        try {Thread.sleep(20000);} catch (InterruptedException ie) {
            this.logger.error("NetTimeThread: Unexpected interruption of Thread.sleep() at startup!");
        }

        // If we haven't received any time during this wait then we start sending instead.
        if (lastReceivedTimeTime == -1) {
            this.timeSender = true;
        }

        while (isRunning()) {
            try {
                if (this.timeSender) {
                    sendTime();
                }
                else {
                    // If we haven't received a time in 20 seconds, take over as sender until we get time from someone else.
                    long now = new Date().getTime();
                    if (now > (this.lastReceivedTimeTime + 20000)) {
                        this.timeSender = true;
                    }
                }
            }
            catch (IOException ioe) {
                this.logger.error("NetTimeThread: Communication problem!", ioe);
            }
            catch (Exception e) {
                this.logger.error("NetTimeThread unknown failure!", e);
            }

            Debug.println2("-- Thread sleeping ...");
            try {Thread.sleep(10000);} catch (InterruptedException ie) {
                this.logger.error("NetTime send loop sleep got unexpectedly interrupted!");
            }
            Debug.println2("-- Thread awake again!");

        }
    }

    /**
     * Notification of receive of a new MessagePacket.
     *
     * @param messagePacket The received MessagePacket.
     */
    @Override
    public void messagePacketReceived(MessagePacket messagePacket) {

        if (messagePacket.getType() == PacketType.NET_TIME && !messagePacket.getMember().equals(netTimeMember)) {
            this.lastReceivedTimeTime = new Date().getTime();
            this.timeSender = false;
            try {
                DataInputStream dis = new DataInputStream(messagePacket.getInputStream());
                long time = dis.readLong();
                dis.close();
                this.netTime.updateNetTime(time);
            }
            catch (IOException ioe) {
                this.logger.error("Received a bad NET_TIME packet!", ioe);
            }

        }
    }
}
