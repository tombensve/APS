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
     * Returns an InputStream for reading the index.
     *
     * @throws APSIOException on failure.
     */
    InputStream getIndexInputStream(String queueName) throws APSIOException

    /**
     * Returns an OutputStream for writing the index.
     *
     * @throws APSIOException on failure.
     */
    OutputStream getIndexOutputStream(String queueName) throws APSIOException

    /**
     * Backups the current index. Should be done before writing a new.
     *
     * @param queueName The queue to backup index for.
     *
     * @throws APSIOException on any failure.
     */
    void backupIndex(String queueName) throws APSIOException

    /**
     * Restores a backed up index.
     *
     * @param queueName The queue to restore the backed up index for.
     *
     * @throws APSIOException on any failure.
     */
    void restoreBackupIndex(String queueName) throws APSIOException

    /**
     * Removes a backup index. This should be called after successful rewrite of the index.
     *
     * @param queueName The queue to remove the backup index for.
     *
     * @throws APSIOException on any failure.
     */
    void removeBackupIndex(String queueName) throws APSIOException

    /**
     * Returns an InputStream for reading the specified item.
     *
     * @param item The item to read.
     *
     * @throws APSIOException on failure.
     */
    InputStream getItemInputStream(String queueName, UUID item) throws APSIOException

    /**
     * Returns an OutputStream for writing the specified item.
     *
     * @param item The item to write.
     *
     * @throws APSIOException on failure.
     */
    OutputStream getItemOutputStream(String queueName, UUID item) throws APSIOException

    /**
     * Deletes the specified item.
     *
     * @param queueName The queue the item belongs to.
     * @param item The item id.
     *
     * @throws APSIOException on failure.
     */
    void deleteItem(String queueName, UUID item) throws APSIOException

    /**
     * Closes the calling instance making it invalid. After this a new call to getQueue() is needed to get
     * a new instance.
     *
     * @param name The name of the queue to release.
     */
    void releaseQueue(String name)
}
