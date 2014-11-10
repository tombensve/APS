package se.natusoft.osgi.aps.api.net.messaging.messages;

import se.natusoft.osgi.aps.annotations.Optional;

import java.util.Date;

/**
 * This represents a basic messaging that can be sent and received. More high level
 * messages can possibly be build on top of this.
 */
public interface APSMessage {

    /** Indicates that timestamp is not available. */
    public static final long NO_TIMESTAMP = 0;

    /**
     * Set the group the message belongs to.
     *
     * @param group The group to set.
     */
    APSMessage group(String group);

    /**
     * Returns the group the message belongs to.
     */
    String getGroup();

    /**
     * Sets message bytes to send.
     *
     * @param data The message bytes to set.
     */
    APSMessage data(byte[] data);

    /**
     * Gets message bytes.
     */
    byte[] getData();

    /**
     * Returns the timestamp of when the messaging data was set. This can be
     * because of received data or that data is about to be sent.
     *
     * This is a local value and is entirely optional. A value of 0 means not supported.
     */
    @Optional
    long getLocalDataTimestamp();

    /**
     * Sets the remote data timestamp.
     *
     * @param remoteDataTimestamp The timestamp to set.
     */
    @Optional
    public APSMessage remoteDataTimestamp(long remoteDataTimestamp);

    /**
     * Returns the "localDataTimestamp" value from the sender, passed along with the
     * messaging.
     *
     * This value is entirely optional. A value of 0 means not supported.
     */
    @Optional
    long getRemoteDataTimestamp();

    /**
     * This provides a default implementation of this messaging.
     */
    public static class Provider implements APSMessage {

        //
        // Private Members
        //

        /** The group the message belongs to. */
        private String group = "<unknown>";

        /** The timestamp of when the messaging was set locally. */
        private long localDataTimestamp = NO_TIMESTAMP;

        /** The timestamp of when the messaging was set remotely. */
        private long remoteDataTimestamp = NO_TIMESTAMP;

        /** The messaging bytes. */
        private byte[] message = new byte[0];

        //
        // Methods
        //

        /**
         * Sets the group this message belongs to.
         *
         * @param group The group to set.
         */
        public APSMessage group(String group) {
            this.group = group;
            return this;
        }

        /**
         * Returns the group this message belongs to.
         */
        public String getGroup() {
            return this.group;
        }

        /**
         * Sets messaging bytes to send.
         *
         * @param data The messaging bytes to set.
         */
        @Override
        public APSMessage data(byte[] data) {
            this.message = data;
            this.localDataTimestamp = new Date().getTime();
            return this;
        }

        /**
         * Gets messaging bytes.
         *
         * @return The bytes.
         */
        @Override
        public byte[] getData() {
            return this.message;
        }

        /**
         * Returns the timestamp of when the messaging data was set. This can be
         * because of received data or that data is about to be sent.
         * <p/>
         * This is a local value and is entirely optional. A value of 0 means not supported.
         */
        @Override
        @Optional
        public long getLocalDataTimestamp() {
            return this.localDataTimestamp;
        }

        /**
         * Sets the remote data timestamp.
         *
         * @param remoteDataTimestamp The timestamp to set.
         */
        @Optional
        public APSMessage remoteDataTimestamp(long remoteDataTimestamp) {
            this.remoteDataTimestamp = remoteDataTimestamp;
            return this;
        }

        /**
         * Returns the "localDataTimestamp" value from the sender, passed along with the
         * messaging.
         * <p/>
         * This value is entirely optional. A value of 0 means not supported.
         */
        @Override
        @Optional
        public long getRemoteDataTimestamp() {
            return this.remoteDataTimestamp;
        }

    }
}
