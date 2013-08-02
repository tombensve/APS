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
package se.natusoft.apsgroups.internal.protocol;

import se.natusoft.apsgroups.Debug;
import se.natusoft.apsgroups.config.APSGroupsConfig;
import se.natusoft.apsgroups.internal.net.Transport;
import se.natusoft.apsgroups.internal.protocol.message.Message;
import se.natusoft.apsgroups.internal.protocol.message.MessagePacket;
import se.natusoft.apsgroups.internal.protocol.message.MessagePacketListener;
import se.natusoft.apsgroups.internal.protocol.message.PacketType;

import java.io.IOException;

/**
 * Sends messages.
 */
public class MessageSender implements MessagePacketListener{
    //
    // Private Members
    //

    /** The message to send. */
    private Message message = null;

    /** The transport to use. */
    private Transport transport = null;

    /** This is set to true in send() to inhibit that it gets sent twice. */
    private boolean sent = false;

    /** Our config. */
    private APSGroupsConfig config = null;

    //
    // Constructors
    //

    /**
     * Creates a new MessageSender.
     *
     * @param message The message to send.
     * @param transport The transport to use.
     * @param config The config to use.
     */
    public MessageSender(Message message, Transport transport, APSGroupsConfig config) {
        this.message = message;
        this.transport = transport;
        this.config = config;
    }

    //
    // Methods
    //

    /**
     * Sends the message. This method can only be called once. After send() returns or an exception is thrown this
     * instance is invalid and any more calls to send will be ignored.
     *
     * @throws IOException
     */
    public void send() throws IOException {
        if (!this.sent) {
            Debug.println("Sending message: " + this.message.getId());
            this.sent = true;

            int timeoutSeconds = this.config.getSendTimeout();
            int resendInterval = this.config.getResendInterval();
            int iterations = timeoutSeconds / resendInterval;
            int waitIterations = resendInterval * 2;
            boolean success = false;
            for (int iteration = 0; iteration < iterations; iteration++) {
                // Every iteration represents <resendInterval> seconds. We resend non acknowledged every iteration.
                // waitForAcknowledgements() sleeps for half a second. Thereby we do that <resendInterval * 2> times
                // before resending.

                sendNonAcknowledged();

                for (int i = 0; i < waitIterations; i++) {
                    success = waitForAcknowledgements();
                    if (success) break;
                }
                if (success) break;
            }
            if (!success) {
                throw new IOException("Timeout: Failed to send message! Not all members acknowledged receive within specified time!");
            }
        }
    }

    /**
     * The first time this is called all packets will be sent, but as acknowledgements are received
     * only the still not completely acknowledged will be resent.
     *
     * @throws IOException
     */
    private void sendNonAcknowledged() throws IOException {
        for (MessagePacket packet : this.message.getAllPackets()) {
            if (!packet.hasAllAcknowledgements()) {
                this.transport.send(packet.getPacketBytes());
            }
        }
    }

    /**
     * Waits for acknowledgements to arrive.
     *
     * @throws IOException
     */
    private boolean waitForAcknowledgements() throws IOException {
        boolean success = false;

        // Yes this first if is a bit of optimism :-)
        if (this.message.isAllPacketsAcknowledged()) {
            success = true;
        }
        else {
            try {
                Thread.sleep(500); // Half a second
            }
            catch (InterruptedException ie) {
                throw new IOException("Unexpectedly interrupted while waiting for receive acknowledgements!");
            }
        }
        if (this.message.isAllPacketsAcknowledged()) {
            success = true;
        }

        return success;
    }

    /**
     * Notification of receive of a new MessagePacket.
     *
     * @param messagePacket The received MessagePacket.
     */
    @Override
    public void messagePacketReceived(MessagePacket messagePacket) {
        if (
                !messagePacket.getMember().equals(this.message.getMember()) &&
                messagePacket.getType() == PacketType.ACKNOWLEDGEMENT &&
                messagePacket.getGroup().getName().equals(this.message.getGroup().getName()) &&
                messagePacket.getMessageId().equals(this.message.getId())
        ) {
            MessagePacket sentPacket = this.message.getPacketByNumber(messagePacket.getPacketNumber());
            sentPacket.acknowledgeMember(messagePacket.getMember());
        }
    }
}
