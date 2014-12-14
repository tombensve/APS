package se.natusoft.osgi.aps.api.net.messaging.types;

import se.natusoft.osgi.aps.codedoc.Optional;

import java.util.Date;

/**
 * This represents a basic messaging that can be sent and received. More high level
 * types can possibly be build on top of this.
 */
public interface APSMessage {

    /** Indicates that timestamp is not available. */
    public static final long NO_TIMESTAMP = 0;

    /**
     * Sets content content.
     *
     * @param content The content content to set.
     */
    APSMessage setContent(APSData content);

    /**
     * Gets content content.
     */
    APSData getContent();

    /**
     * Returns the timestamp of when the content data was set. This can be
     * because of received data or that data is about to be sent.
     *
     * This is a local value and is entirely optional. A value of 0 means not supported.
     */
    @Optional
    long getLocalDataTimestamp();

    /**
     * Returns the "localDataTimestamp" value from the sender, passed along with the
     * content.
     *
     * This value is entirely optional. A value of 0 means not supported.
     */
    @Optional
    long getRemoteDataTimestamp();

    /**
     * This provides a default implementation of APSMessage.
     */
    public static class Default implements APSMessage {

        //
        // Private Members
        //

        /** The timestamp of when the messaging was set locally. */
        private long localDataTimestamp = NO_TIMESTAMP;

        /** The timestamp of when the messaging was set remotely. */
        private long remoteDataTimestamp = NO_TIMESTAMP;

        /** The messaging bytes. */
        private APSData content = null;

        //
        // Methods
        //

        /**
         * Sets messaging bytes to send.
         *
         * @param content The content content to set.
         */
        public APSMessage setContent(APSData content) {
            this.content = content;
            this.localDataTimestamp = new Date().getTime();
            return this;
        }

        /**
         * Returns content content.
         */
        @Override
        public APSData getContent() {
            return this.content;
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
