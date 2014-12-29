package se.natusoft.osgi.aps.net.messaging.messages

import se.natusoft.osgi.aps.api.net.messaging.messages.APSRootMessage

/**
 * A message containing a new time value.
 */
class ClusterTimeValueMessage extends APSRootMessage {

    public static final String MESSAGE_TYPE = "clusterTimeValue"

    //
    // Properties
    //

    /** A date time value. */
    long dateTime

    //
    // Constructors
    //

    public ClusterTimeValueMessage() {
        setType(MESSAGE_TYPE)
    }

    //
    // Methods
    //

    /**
     * Reads the content bytes into local model data.
     *
     * @param in The DataInputStream to read from.
     */
    protected void read(DataInputStream inStream) throws IOException {
        super.read(inStream)
        this.dateTime = inStream.readLong()
    }

    /**
     * Writes the model data into the content bytes.
     *
     * @param outStream The DataOutputStream to write to.
     */
    protected void write(DataOutputStream outStream) throws IOException {
        super.write(outStream)
        outStream.writeLong(this.dateTime)
    }

}
