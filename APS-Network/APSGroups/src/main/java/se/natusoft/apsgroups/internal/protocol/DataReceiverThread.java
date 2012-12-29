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

import se.natusoft.apsgroups.Debug;
import se.natusoft.apsgroups.internal.net.Transport;
import se.natusoft.apsgroups.internal.protocol.message.MessagePacket;
import se.natusoft.apsgroups.internal.protocol.message.MessagePacketListener;
import se.natusoft.apsgroups.internal.protocol.message.PacketType;
import se.natusoft.apsgroups.internal.protocol.message.exception.BadProtocolIDException;
import se.natusoft.apsgroups.logging.APSGroupsLogger;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.List;

/**
 * This is a thread that listens to incoming MessagePackets and forwards them to
 * interested listeners.
 */
public class DataReceiverThread extends Thread implements DataReceiver {
    //
    // Private Members
    //

    /** The transport to use to listen to incoming data. */
    private Transport transport = null;

    /** The listeners to forwards MessagePackets to. */
    private List<MessagePacketListener> listeners = new LinkedList<>();

    /** Set to true on start and false on stop. */
    private boolean running = false;

    /** The logger to log to. */
    private APSGroupsLogger logger = null;

    //
    // Constructors
    //

    /**
     * Creates a new DataReceiverThread.
     *
     * @param logger The logger for the thread to log on.
     * @param transport The transport to use for reading data messages.
     */
    public DataReceiverThread(APSGroupsLogger logger, Transport transport) {
        this.logger = logger;
        this.transport = transport;
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
     * Adds a MessagePacket listener.
     *
     * @param listener The listener to add.
     */
    @Override
    public synchronized void addMessagePacketListener(MessagePacketListener listener) {
        this.listeners.add(listener);
    }

    /**
     * Removes a MessagePacket listener.
     *
     * @param listener The listener to remove.
     */
    @Override
    public synchronized void removeMessagePacketListener(MessagePacketListener listener) {
        this.listeners.remove(listener);
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
     * The main thread execution.
     */
    @Override
    public void run() {
        while (isRunning()) {
            try {
                Transport.Packet packet = transport.receive();
                MessagePacket messagePacket = new MessagePacket(packet);
                if (messagePacket.getType() != PacketType.MEMBER_ANNOUNCEMENT && Debug.level >= Debug.DEBUG_NORMAL) {
                    Debug.println("Received packet:");
                    Debug.println("    messageId:    " + messagePacket.getMessageId());
                    Debug.println("    packetNumber: " + messagePacket.getPacketNumber());
                    Debug.println("    type:         " + messagePacket.getType());
                    Debug.println("    member:       " + messagePacket.getMember().getId());
                    Debug.println("    length:       " + messagePacket.getData().length);
                }

                for (MessagePacketListener listener : this.listeners) {
                    if (messagePacket.getType() != PacketType.MEMBER_ANNOUNCEMENT) {
                        Debug.println("Delivering packet to listener: " + listener);
                    }
                    listener.messagePacketReceived(messagePacket);
                }
            }
            catch (SocketTimeoutException ste) {
                // This is OK, actually expected since we don't block the receive forever!
            }
            catch (IOException ioe) {
                // We do sometimes get a SocketException on shutdown when the socket is closed while this
                // loop still is waiting for receive on the socket (SO_TIMEOUT has not yet occurred). There
                // is no point in showing that.
                if (isRunning()) {
                    this.logger.error("DataReceiverThread: Communication problem!", ioe);
                }
            }
            catch (BadProtocolIDException bpie) {
                this.logger.warn(String.format("DataReceiverThread: Got packet with bad protocol id (%x)! This might be because two or " +
                        "more different protocols communicate on the same multicast address and port! This packet will be ignored.",
                        bpie.getProtocolId()));
            }
            catch (Exception e) {
                this.logger.error("DataReceiverThread: Unexpected failure!", e);
            }
        }
    }
}
