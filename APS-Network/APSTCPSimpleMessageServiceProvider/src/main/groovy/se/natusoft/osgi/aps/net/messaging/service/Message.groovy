package se.natusoft.osgi.aps.net.messaging.service

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

/**
 * This represents a message to be send and received.
 */
@CompileStatic
@TypeChecked
class Message {

    //
    // Properties
    //

    /** The target of the message. */
    String target

    /** The message content and content type. */
    byte[] data

    //
    // Methods
    //

    /**
     * Reads the message from an InputStream.
     *
     * @param inputStream The stream to read from.
     *
     * @throws IOException on any IO failure.
     */
    public void read(InputStream inputStream) throws IOException {
        DataInputStream dataStream = new DataInputStream(inputStream)
        this.target = dataStream.readUTF()
        int msgSize = dataStream.readInt()
        this.data = new byte[msgSize]
        dataStream.read(this.data, 0, msgSize)
    }

    /**
     * Left shift operator to read from input stream.
     *
     * @param inputStream The stream to read from.
     *
     * @return itself for chaining.
     *
     * @throws IOException
     */
    Message leftShift(InputStream inputStream) throws IOException {
        read(inputStream)
        return this
    }

    /**
     * Writes the message to an OutputStream.
     *
     * @param outputStream The stream to write to.
     *
     * @throws IOException on any IO failure.
     */
    public void write(OutputStream outputStream) throws IOException {
        DataOutputStream dataStream = new DataOutputStream(outputStream)
        dataStream.writeUTF(this.target)
        dataStream.writeInt(data.length)
        dataStream.write(data)
        dataStream.flush()
    }

    /**
     * Right shift operator to write to an output stream.
     *
     * @param outputStream The stream to write to.
     *
     * @throws IOException
     */
    void rightShift(OutputStream outputStream) throws IOException {
        write(outputStream)
    }
}
