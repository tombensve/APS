package se.natusoft.osgi.aps.api.net.messaging.messages;

import se.natusoft.osgi.aps.annotations.Optional;

import java.util.Date;

/**
 * This represents a basic messaging that can be sent and received. More high level
 * messages can possibly be build on top of this.
 */
public interface APSMessage {

    /**
     * Sets messaging bytes to send.
     *
     * @param data The messaging bytes to set.
     */
    void setData(byte[] data);

    /**
     * Gets messaging bytes.
     *
     * @return The bytes.
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

        /** The timestamp of when the messaging was set locally. */
        private long localDataTimestamp = 0;

        /** The timestamp of when the messaging was set remotely. */
        private long remoteDataTimestamp = 0;

        /** The messaging bytes. */
        private byte[] message = new byte[0];

        //
        // Constructors
        //

        /**
         * Creates a new Provider instance.
         */
        public Provider() {}

        /**
         * Creates a new Provider instance.
         *
         * @param remoteDataTimestamp The received remote data timestamp.
         */
        public Provider(long remoteDataTimestamp) {
            this.remoteDataTimestamp = remoteDataTimestamp;
        }

        //
        // Methods
        //

        /**
         * Sets messaging bytes to send.
         *
         * @param data The messaging bytes to set.
         */
        @Override
        public void setData(byte[] data) {
            this.message = data;
            this.localDataTimestamp = new Date().getTime();
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
