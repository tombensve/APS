package se.natusoft.osgi.aps.api.net.messaging.types;

import se.natusoft.osgi.aps.codedoc.Optional;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * This represents a base message that can be sent and received. Extend this for more detailed messages.
 */
public interface APSMessage {

    /**
     * Returns the type of the message.
     */
    String getType();

    /**
     * Returns the complete message as a byte array.
     */
    byte[] getBytes();

    /**
     * Sets the message bytes.
     *
     * @param bytes The bytes to set.
     */
    void setBytes(byte[] bytes);

}
