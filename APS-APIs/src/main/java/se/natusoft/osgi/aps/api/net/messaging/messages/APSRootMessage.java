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

    @Override
    public byte[] getBytes() {
        return this.content.getContent();
    }

    @Override
    public void setBytes(byte[] bytes) {
        this.content.setContent(bytes);
    }

    /**
     * Returns the data content as an InputStream.
     */
    public DataInputStream getContentInputStream() {
        DataInputStream in = this.content.getContentInputStream();
        try {
            this.type = in.readUTF();
        }
        catch (IOException ioe) {/* This will not happen since we are reading from a byte array in memory. */}
        return in;
    }

    /**
     * Returns an OutputStream for writing data content.
     * <p/>
     * The content will be set on close() of stream.
     */
    public DataOutputStream getContentOutputStream() {
        DataOutputStream out = this.content.getContentOutputStream();
        try {
            out.writeUTF(this.type);
        }
        catch (IOException ioe) {/* This will not happen since we are writing to a byte array in  memory. */}
        return out;
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
