package se.natusoft.osgi.aps.api.net.messaging.messages;

import se.natusoft.osgi.aps.api.net.messaging.service.APSDataSyncService;

/**
 * This is a synchronization event.
 */
public class APSSyncEvent<SyncType> {

    /** Indicates that the timestamp is not provided. */
    public static final long NO_TIMESTAMP = 0;

    /** The synchronization group. */
    private String _group;

    /** The key of the sync data. */
    private String _key;

    /** The sync data. */
    private SyncType _data;

    /** A possible timestamp of the sync data. */
    private long _timestamp = NO_TIMESTAMP;

    /**
     * Creates a new SyncEvent.
     */
    public APSSyncEvent() {}

    /**
     * Sets the group of the sync event.
     *
     * @param group The group to set.
     */
    public APSSyncEvent group(String group) { this._group = group; return this; }

    /**
     * Sets the key of the sync data in this event.
     *
     * @param key The key to set.
     */
    public APSSyncEvent key(String key) { this._key = key; return this; }

    /**
     * Sets the sync data of this event.
     *
     * @param data The data to set.
     */
    public APSSyncEvent data(SyncType data) { this._data = data; return this; }

    /**
     * Sets the timestamp of the sync data in this event.
     *
     * @param timestamp The timestamp to set.
     */
    public APSSyncEvent timestamp(long timestamp) { this._timestamp = timestamp; return this; }

    /**
     * Returns the group of this event came from.
     */
    public String group() { return this._group; }

    /**
     * Returns the key of the sync data in this event.
     */
    public String key() { return this._key; }

    /**
     * Returns the data of this sync event.
     */
    public SyncType data() { return this._data; }

    /**
     * Returns the timestamp of the data in this sync event.
     */
    public long timestamp() { return this._timestamp; }
}
