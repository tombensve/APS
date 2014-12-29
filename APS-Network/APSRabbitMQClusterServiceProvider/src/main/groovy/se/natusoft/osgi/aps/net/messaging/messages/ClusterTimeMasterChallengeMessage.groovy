package se.natusoft.osgi.aps.net.messaging.messages

import se.natusoft.osgi.aps.api.net.messaging.messages.APSRootMessage

/**
 * A message that challenges for time mastership.
 */
class ClusterTimeMasterChallengeMessage extends APSRootMessage {

    public static final String MESSAGE_TYPE = "clusterTimeMasterChallenge"

    //
    // Properties
    //

    //
    // Constructors
    //

    public ClusterTimeMasterChallengeMessage() {
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
    }

    /**
     * Writes the model data into the content bytes.
     *
     * @param outStream The DataOutputStream to write to.
     */
    protected void write(DataOutputStream outStream) throws IOException {
        super.write(outStream)
    }

}
