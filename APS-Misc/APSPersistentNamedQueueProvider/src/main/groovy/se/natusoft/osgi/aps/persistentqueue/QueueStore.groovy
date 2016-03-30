package se.natusoft.osgi.aps.persistentqueue

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.osgi.aps.exceptions.APSIOException

/**
 * API for accessing filesystem.
 */
@CompileStatic
@TypeChecked
interface QueueStore {

    /**
     * Returns an InputStream for reading the specified item.
     *
     * @param item The item to read.
     *
     * @throws APSIOException on failure.
     */
    InputStream getItemInputStream(String queueName, long item) throws APSIOException

    /**
     * Returns an OutputStream for writing the specified item.
     *
     * @param item The item to write.
     *
     * @throws APSIOException on failure.
     */
    OutputStream getItemOutputStream(String queueName, long item) throws APSIOException

    /**
     * Deletes the specified item.
     *
     * @param queueName The queue the item belongs to.
     * @param item The item id.
     *
     * @throws APSIOException on failure.
     */
    void deleteItem(String queueName, long item) throws APSIOException

    /**
     * Closes the calling instance making it invalid. After this a new call to getQueue() is needed to get
     * a new instance.
     *
     * @param name The name of the queue to release.
     */
    void releaseQueue(String name)

    /**
     * Returns the current read index.
     *
     * @param queueName The queue to get the read index for.
     */
    long getReadIndex(String queueName)

    /**
     * Sets the current read index.
     *
     * @param queueName The queue to set the read index for.
     * @param index The new index to set.
     */
    void setReadIndex(String queueName, long index)

    /**
     * Gets the last written index.
     *
     * @param queueName The queue to get the last written index for.
     */
    long getWriteIndex(String queueName)

    /**
     * Sets the last written index.
     *
     * @param queueName The queue to set the last written index for.
     * @param index The index to set.
     */
    void setWriteIndex(String queueName, long index)

    /**
     * Does just what the method says :-). This *MUST* be called from a synchronized method!
     */
    void waitForCurrentDeletesToFinnish();

}
