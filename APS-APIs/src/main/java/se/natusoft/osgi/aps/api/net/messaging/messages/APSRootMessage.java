package se.natusoft.osgi.aps.api.net.messaging.messages;

import se.natusoft.osgi.aps.api.net.messaging.types.APSCluster;
import se.natusoft.osgi.aps.api.net.messaging.types.APSData;
import se.natusoft.osgi.aps.api.net.messaging.types.APSMessage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * This provides a default implementation of APSMessage.
 */
public class APSRootMessage implements APSMessage {

    //
    // Private Members
    //

    /** The message type. */
    private String type = "UNTYPED";

    /** The message content. */
    private APSData content = new APSData.Default();

    //
    // Constructors
    //

    /**
     * Creates a new APSMessage.Default. (For Groovy properties constructor)
     */
    public APSRootMessage() {}

    /**
     * Creates a new APSMessage.Default.
     *
     * @param type The type of the message.
     */
    public APSRootMessage(String type) {
        this.type = type;
    }

    //
    // Methods
    //

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
        catch (IOException ioe) {/* This will not happen since we are reading from a byte array in memory. */}
    }

    /**
     * Reads the content bytes into local model data. This method should be overridden by subclasses which
     * should start with super.read(in);.
     *
     * @param in The DataInputStream to read from.
     */
    protected void read(DataInputStream in) throws IOException {
            this.type = in.readUTF();
    }

    /**
     * Writes the model data into the content bytes.
     */
    private void write() {
        this.content = new APSData.Default();
        try {
            write(this.content.getContentOutputStream());
        }
        catch (IOException ioe) {/* This will not happen since we are writing to a byte array in  memory. */}
    }

    /**
     * Writes the model data into the content bytes. This method should be overridden by subclasses which
     * should start with super.write(out);.
     *
     * @param out The DataOutputStream to write to.
     */
    protected void write(DataOutputStream out) throws IOException {
        out.writeUTF(this.type);
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
    public static String getType(byte[] messageData) {
        APSRootMessage msg = new APSRootMessage();
        msg.setBytes(messageData);
        return msg.getType();
    }

    //
    // Inner Classes
    //

    /**
     * A default MessageResolver for APSRootMessage that can possibly be used for simple cases.
     *
     * Otherwise a resolver need to resolve to an APSRootMessage subclass depending on type.
     */
    public static class DefaultMessageResolver implements APSCluster.MessageResolver {

        /**
         * Returns an APSMessage implementation based on the message data.
         *
         * @param messageData The message data.
         */
        @Override
        public APSMessage resolveMessage(byte[] messageData) {
            APSMessage message = new APSRootMessage();
            message.setBytes(messageData);
            return message;
        }
    }
}
