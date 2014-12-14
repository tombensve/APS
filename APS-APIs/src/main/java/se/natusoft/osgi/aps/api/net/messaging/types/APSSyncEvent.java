package se.natusoft.osgi.aps.api.net.messaging.types;

import se.natusoft.osgi.aps.codedoc.Implements;

/**
 * This is a synchronization event.
 */
public interface APSSyncEvent {

    /**
     * Indicates that the timestamp is not provided.
     */
    public static final long NO_TIMESTAMP = 0;

    /**
     * Returns the key of the sync content in this event.
     */
    public String getKey();

    /**
     * Returns the content of this sync event.
     */
    public APSData getContent();

    /**
     * Returns the timestamp of the content in this sync event.
     */
    public long getTimestamp();


    /**
     * A default implementation of the APSSyncEvent.
     */
    static class Default implements APSSyncEvent {

        /** The key of the sync content. */
        private String key;

        /** The sync content. */
        private APSData content;

        /** A possible timestamp of the sync content. */
        private long _timestamp = NO_TIMESTAMP;

        /**
         * Creates a new APSSyncEvent.Default.
         */
        public Default() {
        }

        /**
         * Sets the key of the sync content in this event.
         *
         * @param key The key to set.
         */
        public Default key(String key) {
            this.key = key;
            return this;
        }

        /**
         * Sets the sync content of this event.
         *
         * @param content The content to set.
         */
        public Default content(APSData content) {
            this.content = content;
            return this;
        }

        /**
         * Sets the timestamp of the sync content in this event.
         *
         * @param timestamp The timestamp to set.
         */
        public Default timestamp(long timestamp) {
            this._timestamp = timestamp;
            return this;
        }

        /**
         * Returns the key of the sync content in this event.
         */
        @Override
        @Implements(APSSyncEvent.class)
        public String getKey() {
            return this.key;
        }

        /**
         * Returns the content of this sync event.
         */
        @Override
        @Implements(APSSyncEvent.class)
        public APSData getContent() {
            return this.content;
        }

        /**
         * Returns the timestamp of the content in this sync event.
         */
        @Override
        @Implements(APSSyncEvent.class)
        public long getTimestamp() {
            return this._timestamp;
        }
    }
}
