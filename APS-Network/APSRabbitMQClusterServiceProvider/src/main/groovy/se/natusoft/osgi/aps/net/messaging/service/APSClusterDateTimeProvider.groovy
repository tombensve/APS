package se.natusoft.osgi.aps.net.messaging.service

import se.natusoft.osgi.aps.api.net.messaging.types.APSCluster
import se.natusoft.osgi.aps.api.net.messaging.types.APSClusterDateTime
import se.natusoft.osgi.aps.api.net.messaging.types.APSMessage
import se.natusoft.osgi.aps.codedoc.Implements
import se.natusoft.osgi.aps.net.messaging.messages.ClusterTimeMasterChallengeMessage
import se.natusoft.osgi.aps.net.messaging.messages.ClusterTimeMasterMessage
import se.natusoft.osgi.aps.net.messaging.messages.ClusterTimeRequestMessage
import se.natusoft.osgi.aps.net.messaging.messages.ClusterTimeValueMessage
import se.natusoft.osgi.aps.tools.APSLogger

/**
 * Provides cluster time.
 */
class APSClusterDateTimeProvider implements APSClusterDateTime, APSCluster.Listener {
    //
    // Private Members
    //

    /** The current diff between local time and cluster time. */
    private long timeDiff = 0

    /** True if we are time master. */
    private boolean master = false

    /**
     * This is set to true before a master challenge is sent. Receive of ClusterTimeMasterMessage will set this to false again.
     * If this still is true after a random sleep then the master challenge has been won and a ClusterTimeMasterMessage is sent.
     */
    private boolean masterChallenge = false

    /**
     * This is cleared to false before sending a ClusterTimeRequestMessage. This will be set to true when a
     * ClusterTimeValueMessage is received. If this is true after 10 second delay then there is another master
     * that has answered the request message, otherwise a new ClusterTimeMasterChallengeMessage is sent.
     */
    private boolean receivedTime = false

    //
    // Properties
    //

    /** For sending control messages. */
    ControlChannelSender controlChannelSender

    /** Logger to log to. */
    APSLogger logger

    //
    // Constructors
    //


    //
    // Methods
    //

    /**
     * Get "now" in cluster time.
     */
    @Override
    @Implements(APSClusterDateTime.class)
    long getClusterDateTime() {
        return new Date().getTime() + this.timeDiff
    }

    /**
     * Converts a cluster time to a local time.
     *
     * @param clusterDateTime The cluster time to convert.
     *
     * @return The equivalent local time.
     */
    @Override
    @Implements(APSClusterDateTime.class)
    long toLocalDateTime(long clusterDateTime) {
        return clusterDateTime - this.timeDiff
    }

    /**
     * Converts local time to cluster time.
     *
     * @param localDateTime The local time to convert.
     *
     * @return The equivalent cluster time.
     */
    @Override
    @Implements(APSClusterDateTime.class)
    long toClusterDateTime(long localDateTime) {
        return localDateTime + this.timeDiff
    }

    /**
     * This is called when a message is received.
     *
     * @param message The received message.
     */
    @Override
    @Implements(APSCluster.Listener.class)
    void messageReceived(APSMessage message) {
        switch (message.getClass()) {
            case ClusterTimeValueMessage.class:
                handle(((ClusterTimeValueMessage)message))
                break

            case ClusterTimeRequestMessage.class:
                handle((ClusterTimeRequestMessage)message)
                break

            case ClusterTimeMasterMessage.class:
                handle((ClusterTimeMasterMessage)message)
                break;

            case ClusterTimeMasterChallengeMessage.class:
                handle((ClusterTimeMasterChallengeMessage)message)
                break;
        }
    }

    private void handle(ClusterTimeValueMessage timeValueMessage) {
        long clusterTimeValue = timeValueMessage.dateTime
        long now = new Date().getTime()
        this.timeDiff = now - clusterTimeValue
    }

    private void handle(ClusterTimeRequestMessage timeRequestMessage) {
        if (this.master) {
            this.controlChannelSender.sendControlMessage(new ClusterTimeValueMessage(dateTime: getClusterDateTime()))
        }
    }

    private void handle(ClusterTimeMasterMessage timeMasterMessage) {
        this.masterChallenge = false
    }

    private void handle(ClusterTimeMasterChallengeMessage timeMasterChallengeMessage) {
        this.masterChallenge = true
        Random random = new Random()
        try { Thread.sleep(random.nextInt(1000 * 10)) } catch (InterruptedException ie) {
            this.logger.error("DateTimeProvider: Failed to sleep during time master challenge!")
        }
        if (this.masterChallenge) {
            this.controlChannelSender.sendControlMessage(new ClusterTimeMasterMessage())
            this.master = true
        }
        else {
            this.master = false
        }
    }

    public void sendTimeRequest() {
        this.receivedTime = false
        this.controlChannelSender.sendControlMessage(new ClusterTimeRequestMessage())
        try {Thread.sleep(1000 * 10)} catch (InterruptedException ie) {
            this.logger.error("DateTimeProvider: Failed to sleep during sendTimeRequest!")
        }
        if (!this.receivedTime) {
            sendTimeMasterChallenge()
        }
    }

    public void sendTimeMasterChallenge() {
        this.controlChannelSender.sendControlMessage(new ClusterTimeMasterChallengeMessage())
    }

    //
    // Inner Classes
    //

    /**
     * Sends messages on the control channel.
     */
    public static interface ControlChannelSender {

        /**
         * Sends a message on the control channel.
         *
         * @param message The message to send.
         */
        public void sendControlMessage(APSMessage message);
    }
}
