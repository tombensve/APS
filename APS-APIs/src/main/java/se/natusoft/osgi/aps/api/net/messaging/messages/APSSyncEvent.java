package se.natusoft.osgi.aps.api.net.messaging.messages;

import se.natusoft.osgi.aps.annotations.documentative.Implements;

/**
 * This is a synchronization event.
 */
public interface APSSyncEvent<SyncType> {

    /**
     * Indicates that the timestamp is not provided.
     */
    public static final long NO_TIMESTAMP = 0;

    /**
     * Returns the group of this event came from.
     */
    public String getGroup();

    /**
     * Returns the key of the sync data in this event.
     */
    public String getKey();

    /**
     * Returns the data of this sync event.
     */
    public SyncType getData();

    /**
     * Returns the timestamp of the data in this sync event.
     */
    public long getTimestamp();


    /**
     * A default implementation of the APSSyncEvent.
     */
    static class Default<SyncType> implements APSSyncEvent<SyncType> {

        /** The synchronization group. */
        private String group;

        /** The key of the sync data. */
        private String key;

        /** The sync data. */
        private SyncType data;

        /** A possible timestamp of the sync data. */
        private long _timestamp = NO_TIMESTAMP;

        /**
         * Creates a new SyncEvent.
         */
        public Default() {
        }

        /**
         * Sets the group of the sync event.
         *
         * @param group The group to set.
         */
        public Default group(String group) {
            this.group = group;
            return this;
        }

        /**
         * Sets the key of the sync data in this event.
         *
         * @param key The key to set.
         */
        public Default key(String key) {
            this.key = key;
            return this;
        }

        /**
         * Sets the sync data of this event.
         *
         * @param data The data to set.
         */
        public Default data(SyncType data) {
            this.data = data;
            return this;
        }

        /**
         * Sets the timestamp of the sync data in this event.
         *
         * @param timestamp The timestamp to set.
         */
        public Default timestamp(long timestamp) {
            this._timestamp = timestamp;
            return this;
        }

        /**
         * Returns the group of this event came from.
         */
        @Override
        @Implements(APSSyncEvent.class)
        public String getGroup() {
            return this.group;
        }

        /**
         * Returns the key of the sync data in this event.
         */
        @Override
        @Implements(APSSyncEvent.class)
        public String getKey() {
            return this.key;
        }

        /**
         * Returns the data of this sync event.
         */
        @Override
        @Implements(APSSyncEvent.class)
        public SyncType getData() {
            return this.data;
        }

        /**
         * Returns the timestamp of the data in this sync event.
         */
        @Override
        @Implements(APSSyncEvent.class)
        public long getTimestamp() {
            return this._timestamp;
        }
    }
}
