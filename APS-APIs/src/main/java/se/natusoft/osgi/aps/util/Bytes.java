package se.natusoft.osgi.aps.util;

import java.io.*;
import java.util.Arrays;

/**
 * This is a generic object holding a byte array and provides different ways of setting and getting the byte data.
 */
public class Bytes {
    //
    // Private Members
    //

    /** The byte data held by this object. */
    private byte[] bytes = new byte[0];

    //
    // Constructors
    //

    /**
     * Creates a new empty object.
     */
    public Bytes() {}

    //
    // Methods
    //

    /**
     * Sets bytes.
     *
     * @param bytes The bytes to set.
     */
    public void setBytes(byte[] bytes) {
        this.bytes = Arrays.copyOf(bytes, bytes.length);
    }

    /**
     * Gets the bytes.
     */
    public byte[] getBytes() {
        return Arrays.copyOf(this.bytes, this.bytes.length);
    }

    /**
     * Returns an OutputStream that can be used to fill this object with data. This object
     * will be updated when the stream is closed!
     */
    public OutputStream getContentWriteStream() {
        return new ByteArrayOutputStream() {
            @Override
            public void close() throws IOException {
                super.close();
                bytes = super.toByteArray();
            }
        };
    }

    /**
     * Returns an InputStream to read the content of this object.
     */
    public InputStream getContentReadStream() {
        return new ByteArrayInputStream(this.bytes);
    }

    /**
     * Returns content as a String.
     */
    public String toString() {
        return new String(this.bytes);
    }

    /**
     * Sets content from a String.
     *
     * @param str The String to set content from.
     */
    public void fromString(String str) {
        this.bytes = str.getBytes();
    }
}
