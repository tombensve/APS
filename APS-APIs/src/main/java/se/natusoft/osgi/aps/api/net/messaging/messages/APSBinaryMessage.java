/*
 *
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         Provides the APIs for the application platform services.
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
 *     tommy ()
 *         Changes:
 *         2015-01-09: Created!
 *
 */
package se.natusoft.osgi.aps.api.net.messaging.messages;

import se.natusoft.osgi.aps.api.net.messaging.exception.APSMessagingException;
import se.natusoft.osgi.aps.api.net.messaging.types.APSDataPacket;
import se.natusoft.osgi.aps.api.net.messaging.types.APSMessage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * This provides a default implementation of APSMessage.
 */
public class APSBinaryMessage implements APSMessage {

    //
    // Private Members
    //

    /** The message type. */
    private String type = "UNTYPED";

    /** The message content. */
    private APSDataPacket content = new APSDataPacket.Default();

    /** The UUID of the sender of the message. */
    private UUID senderUUID;

    //
    // Constructors
    //

    /**
     * Creates a new APSMessage.Default. (For Groovy properties constructor)
     */
    public APSBinaryMessage() {}

    /**
     * Creates a new APSMessage.Default.
     *
     * @param type The type of the message.
     */
    public APSBinaryMessage(String type) {
        this.type = type;
    }

    //
    // Methods
    //

    /**
     * Every service implementation should have a UUID, which also gets passed in messages.
     */
    @Override
    public UUID getSenderUUID() {
        return this.senderUUID;
    }

    /**
     * Sets the sender UUID.
     *
     * @param uuid THE UUID to set.
     */
    @Override
    public void setSenderUUID(UUID uuid) {
        this.senderUUID = uuid;
    }

    /**
     * Sets the type of the message. (For Groovy properties constructor)
     *
     * @param type The type to set.
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Returns the type of the message.
     */
    public String getType() {
        return this.type;
    }

    /**
     * Returns the message bytes.
     */
    @Override
    public byte[] getBytes() {
        write();
        return this.content.getContent();
    }

    /**
     * Sets the message bytes.
     *
     * @param bytes The bytes to set.
     */
    @Override
    public void setBytes(byte[] bytes) {
        this.content.setContent(bytes);
        read();
    }

    /**
     * Reads the content bytes into local model data.
     */
    private void read() {
        try {
            read(this.content.getContentInputStream());
        }
        catch (IOException ioe) {
            throw new APSMessagingException("Failed to read message content!", ioe);
        }
    }

    /**
     * Reads the content bytes into local model data. This method should be overridden by subclasses which
     * should start with super.read(in);.
     *
     * @param in The DataInputStream to read from.
     */
    public void read(DataInputStream in) throws IOException {
        this.type = in.readUTF();
        long leastSignificantBits = in.readLong();
        long mostSignificantBits = in.readLong();
        this.senderUUID = new UUID(mostSignificantBits, leastSignificantBits);
    }

    /**
     * Writes the model data into the content bytes.
     */
    private void write() {
        this.content = new APSDataPacket.Default();
        try {
            write(this.content.getContentOutputStream());
        }
        catch (IOException ioe) {
            throw new APSMessagingException("Failed to write message content!", ioe);
        }
    }

    /**
     * Writes the model data into the content bytes. This method should be overridden by subclasses which
     * should start with super.write(out);.
     *
     * @param out The DataOutputStream to write to.
     */
    public void write(DataOutputStream out) throws IOException {
        out.writeUTF(this.type);
        out.writeLong(this.senderUUID.getLeastSignificantBits());
        out.writeLong(this.senderUUID.getMostSignificantBits());
    }

    //
    // Static tool methods
    //

    /**
     * Returns the message type in the received message data.
     *
     * @param messageData The message data to extract message type from.
     *
     * @return The extracted message type.
     */
    public static String getType(byte[] messageData, UUID matchUUID) {
        APSBinaryMessage msg = new APSBinaryMessage();
        msg.setBytes(messageData);
        if (matchUUID != null && !msg.getSenderUUID().equals(matchUUID)) {
            throw new APSMessagingException("Message received from unknown sender!");
        }
        return msg.getType();
    }

    /**
     * Returns the message type in the received message data.
     *
     * @param messageData The message data to extract message type from.
     *
     * @return The extracted message type.
     */
    public static String getType(byte[] messageData) {
        return getType(messageData, null);
    }

    /**
     * Creates and returns a subclass of APSBinaryMessage for when the type of the message have been determined.
     *
     * @param clazz The class of the APSBinaryMessage subclass to return.
     * @param bytes The message bytes.
     */
    public static <T extends APSMessage> T from(Class<T> clazz, byte[] bytes) {
        try {
            T inst = clazz.newInstance();
            inst.setBytes(bytes);
            return inst;
        }
        catch (Exception e) {
            throw new APSMessagingException("Failed to create APSMessage implementation class '" + clazz.getName() + "'!", e);
        }
    }
}
