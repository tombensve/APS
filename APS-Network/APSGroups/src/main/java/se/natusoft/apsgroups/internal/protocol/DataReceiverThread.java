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
import se.natusoft.apsgroups.config.APSGroupsConfig;
import se.natusoft.apsgroups.internal.net.MulticastTransport;
import se.natusoft.apsgroups.internal.net.Transport;
import se.natusoft.apsgroups.internal.protocol.message.MessagePacket;
import se.natusoft.apsgroups.internal.protocol.message.MessagePacketListener;
import se.natusoft.apsgroups.internal.protocol.message.exception.BadProtocolIDException;
import se.natusoft.apsgroups.logging.APSGroupsLogger;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * This is a thread that listens to incoming MessagePackets and forwards them to
 * interested listeners.
 */
public class DataReceiverThread extends Thread implements DataReceiver {
    //
    // Singleton instance
    //

    private static DataReceiverThread instance;

    //
    // Static Access Methods
    //

    public static void init(APSGroupsLogger logger, APSGroupsConfig config) {
        if (DataReceiverThread.instance == null) {
            DataReceiverThread.instance = new DataReceiverThread(logger, config);
            DataReceiverThread.instance.start();
        }
    }

    public static DataReceiver get() {
        return DataReceiverThread.instance;
    }



    //
    // Private Members
    //

    /** The transport to use to listen to incoming data. */
    private Transport transport = null;

    /** The listeners to forwards MessagePackets to. */
    private List<MessagePacketListener> listeners = null;

    /** Set to true on start and false on stop. */
    private boolean running = false;

    /** The logger to log to. */
    private APSGroupsLogger logger = null;

    /** Our configuration. */
    private APSGroupsConfig config = null;

    //
    // Constructors
    //

    /**
     * Creates a new DataReceiverThread.
     *
     * @param logger The logger for the thread to log on.
     * @param config Our config.
     */
    private DataReceiverThread(APSGroupsLogger logger, APSGroupsConfig config) {
        super("APSGroups:DataReceiverThread");
        this.logger = logger;
        this.config = config;
        this.transport = new MulticastTransport(this.logger, this.config);

        this.listeners = Collections.synchronizedList(new LinkedList<MessagePacketListener>());
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
    public void addMessagePacketListener(MessagePacketListener listener) {
        this.listeners.add(listener);
        Debug.println(
                "==================================",
                "++++ Adding listener: " + listener,
                "=================================="
        );
    }

    /**
     * Removes a MessagePacket listener.
     *
     * @param listener The listener to remove.
     */
    @Override
    public void removeMessagePacketListener(MessagePacketListener listener) {
        this.listeners.remove(listener);
        Debug.println(
                "==================================",
                "---- Removing listener: " + listener,
                "=================================="
        );
    }

    /**
     * Returns a current copy of the listeners.
     */
    private  List<MessagePacketListener> getCurrentListeners() {
        List<MessagePacketListener> listenersCopy = new LinkedList<>();
        synchronized (this.listeners) {
            listenersCopy.addAll(this.listeners);
        }

        return listenersCopy;
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
    public synchronized boolean isRunning() {
        return this.running;
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
                "========================================\n" +
                "DataReceiverThread Started!\n" +
                "========================================"
        );
        while (isRunning()) {
            try {
                Transport.Packet packet = this.transport.receive();
                MessagePacket messagePacket = new MessagePacket(packet);
                if (messagePacket.getGroup().getConfig() == null) {
                    messagePacket.getGroup().setConfig(this.config);
                }
                Debug.println(
                        "==========================================\n" +
                        "Received packet:\n" +
                        "    messageId:    " + messagePacket.getMessageId() + "\n" +
                        "    packetNumber: " + messagePacket.getPacketNumber() + "\n" +
                        "    type:         " + messagePacket.getType() + "\n" +
                        "    member:       " + messagePacket.getMember().getId() + "\n" +
                        "    length:       " + messagePacket.getData().length + "\n" +
                        "=========================================="
                );

                for (MessagePacketListener listener : getCurrentListeners()) {
                    Debug.println("---- Delivering packet to listener: " + listener);
                    new MessageListenerDeliveryThread(listener, messagePacket).start();
                    //listener.messagePacketReceived(messagePacket);
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
        try {
            this.transport.close();
        }
        catch (IOException ioe) {
            this.logger.error("Failed to close transport!", ioe);
        }

        DataReceiverThread.instance = null;
    }


    //
    // Inner Classes
    //

    /**
     * This is a support thread to do listener callbacks in a separate thread that also catches all exceptions and logs them.
     * This will not break the receiver thread if a callback does something bad.
     */
    private class MessageListenerDeliveryThread extends Thread {
        private MessagePacketListener listener = null;
        private MessagePacket packet = null;

        public MessageListenerDeliveryThread(MessagePacketListener listener, MessagePacket packet) {
            super("APSGroups:MessageListenerDeliveryThread->" + listener);
            this.listener = listener;
            this.packet = packet;
        }

        @Override
        public void run() {
            try {
                this.listener.messagePacketReceived(this.packet);
                //Debug.println("---- Packet delivered!");
            }
            catch (Exception e) {
                DataReceiverThread.this.logger.error("Failed to deliver packet [" + this.packet + "] to listener [" + this.listener + "]", e);
            }
        }
    }
}
