package se.natusoft.osgi.aps.net.messaging.service

import se.natusoft.osgi.aps.api.net.util.TypedData

/**
 * This represents a message to be send and received.
 */
class Message {

    //
    // Properties
    //

    /** The topic of the message. */
    String topic

    /** The message content and content type. */
    TypedData typedData

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
        this.topic = dataStream.readUTF()
        String contentType = dataStream.readUTF()
        int msgSize = dataStream.readInt()
        byte[] content = new byte[msgSize]
        dataStream.read(content, 0, msgSize)
        this.typedData = new TypedData.Provider(content, contentType)
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
        dataStream.writeUTF(this.topic)
        dataStream.writeUTF(typedData.contentType)
        dataStream.writeInt(typedData.content.length)
        dataStream.write(typedData.content)
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
