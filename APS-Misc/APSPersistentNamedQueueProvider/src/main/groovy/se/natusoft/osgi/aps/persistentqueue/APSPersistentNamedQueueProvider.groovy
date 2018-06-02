package se.natusoft.osgi.aps.persistentqueue

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.docutations.Implements
import se.natusoft.osgi.aps.activator.annotation.BundleStop
import se.natusoft.osgi.aps.activator.annotation.Managed
import se.natusoft.osgi.aps.activator.annotation.OSGiProperty
import se.natusoft.osgi.aps.activator.annotation.OSGiService
import se.natusoft.osgi.aps.activator.annotation.OSGiServiceProvider

import se.natusoft.osgi.aps.api.core.filesystem.service.APSFilesystemService
import se.natusoft.osgi.aps.api.misc.queue.APSNamedQueueService
import se.natusoft.osgi.aps.api.misc.queue.APSQueue
import se.natusoft.osgi.aps.exceptions.APSIOException
import se.natusoft.osgi.aps.exceptions.APSResourceNotFoundException
import se.natusoft.osgi.aps.util.APSLogger
import se.natusoft.osgi.aps.tools.annotation.activator.*

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Implementation of APSNamedQueueService that is also persistent.
 */
@SuppressWarnings("GroovyUnusedDeclaration")
@CompileStatic
@TypeChecked
@OSGiServiceProvider(properties = [@OSGiProperty(name="provider", value="aps-persistent-named-queue-service-provider")])
class APSPersistentNamedQueueProvider implements APSNamedQueueService, QueueStore {

    //
    // Constants
    //

    private static final String FS_OWNER = "aps-persistent-named-queue-service-provider"

    //
    // Private Members
    //

    @Managed
    private APSLogger logger

    @OSGiService
    private APSFilesystemService fsService

    /** A cached version of APSFilesystemService root filesystem for this "owner". */
    private APSFilesystem filesystem = null

    /** We cache APSQueue instances here using the queue name as key. */
    private Map<String, APSQueue> activeQueues = new HashMap<>()

    /**
     * This runs delete jobs in background so that pull() calls does not have to wait for that.
     * We however only allow one job at a time so that it does not interfere too much. Increasing this
     * number actually makes the whole read/delete of files take longer!
     */
    private ExecutorService deleteService = Executors.newFixedThreadPool(1)

    //
    // Methods
    //

    /**
     * Called by APSActivator when the bundle is stopped.
     */
    @BundleStop
    void shutdown() {
        this.deleteService.shutdown()
        this.deleteService.awaitTermination(5, TimeUnit.MINUTES)
    }

    /**
     * This is for when wrapping indexes around to the low limit again. The odds that there are still entries
     * in the delete queue that might get overwritten by a new entry before the old identical entry has had a
     * chance to be  deleted, and which delete then will delete the new entry is very tiny unless you are close
     * to the limit what the queue can hold.
     *
     * But to be safe we create a new ExecutorService for forthcoming deletes and then stop and wait for
     * the old to finnish before returning.
     */
    public void waitForCurrentDeletesToFinnish() {
        ExecutorService currDeletes = this.deleteService
        this.deleteService = Executors.newFixedThreadPool(1)
        currDeletes.shutdown()
        currDeletes.awaitTermination(5, TimeUnit.MINUTES)
    }

    /**
     * Provides a getter for the root filesystem of the service.
     */
    private APSFilesystem getFs() {
        if (this.filesystem == null) {
            if (this.fsService.hasFilesystem(FS_OWNER)) {
                this.filesystem = this.fsService.getFilesystem(FS_OWNER)
            } else {
                this.filesystem = this.fsService.createFilesystem(FS_OWNER)
            }
        }
        return this.filesystem
    }

    /**
     * Translates an IOException to an APSIOException.
     *
     * @param resultClass Identifies return type.
     * @param closure The code to execute.
     *
     * @return Whatever the closure returns.
     */
    private <T> T apsio(Class<T> resultClass, Closure closure) {
        T result
        try {
            result = (T)closure.call()
        }
        catch (IOException ioe) {
            this.logger.error(ioe.getMessage(), ioe)
            throw new APSIOException(ioe.getMessage(), ioe)
        }

        result
    }

    /**
     * Returns true if a queue exists, false otherwise.
     *
     * @param name The name of the queue to test.
     */
    private boolean queueExists(String name) {
        try {
            this.fs.getDirectory(name)
            return true
        }
        catch (IOException ignore) {}
        return false
    }

    /**
     * Creates a new queue.
     *
     * @param name The name of the queue to create. If the named queue already exists, it is just returned,
     *             that is, this will work just like getQueue(name) then.
     *
     * @throws APSIOException on failure to create new queue.
     */
    @Override
    @Implements(APSNamedQueueService.class)
    APSQueue createQueue(String name) throws APSIOException {
        if (!queueExists(name)) {
            this.fs.rootDirectory.createDir(name)
        }
        return getQueue(name)
    }

    /**
     * Returns the named queue. If it does not exist, it is created.
     *
     * @param name The name of the queue to get.
     */
    @Override
    @Implements(APSNamedQueueService.class)
    APSQueue getQueue(String name) {
        if (!queueExists(name)) throw new APSResourceNotFoundException("No queue with name '$name' exists!")

        APSQueue queue = this.activeQueues.get(name);

        if (queue == null) {
            queue = new PersistentQueue(
                logger: this.logger,
                queueName: name,
                queueStore: this
            )
            this.activeQueues.put(name, queue)
        }
        return queue
    }

    /**
     * Removes the named queue.
     *
     * @param name The name of the queue to remove.
     *
     * @throws APSIOException on failure.
     */
    @Override
    @Implements(APSNamedQueueService.class)
    void removeQueue(String name) throws APSIOException {
        apsio Void.class, {
            if (this.fs.getDirectory(name).exists()) {
                this.fs.getDirectory(name).recursiveDelete()
            }
        }
    }

    /**
     * Releases the named queue from active useage. This should be called when the client is done
     * with the queue so that service cache memory can be released.
     *
     * @param name The name of the queue to release.
     */
    @Override
    @Implements(APSNamedQueueService.class)
    void releaseQueue(String name) {
        // Currently there is nothing to do here. This is kept for possible future use.
    }

    /**
     * Reads an index value from the queue.
     *
     * @param queueName The name of the queue to read from.
     * @param index The name of the index to read.
     *
     * @return The read index.
     *
     * @throws APSIOException on failure to read the index.
     */
    private long readIndex(String queueName, String index) throws APSIOException {
        apsio Long.class, {
            long readIx

            if (!this.fs.getDirectory(queueName).exists(index)) {
                APSFile indexFile = this.fs.getDirectory(queueName).createFile(index)
                ObjectOutputStream oos = new ObjectOutputStream(indexFile.createOutputStream())
                try { oos.writeLong(Long.MIN_VALUE) }
                finally {oos.close() }
                readIx = Long.MIN_VALUE
            }
            else {
                APSFile indexFile = this.fs.getDirectory(queueName).getFile(index)
                ObjectInputStream ois = new ObjectInputStream(indexFile.createInputStream())
                readIx = ois.readLong()
                ois.close()
            }

            readIx
        }
    }

    /**
     * Writes and index value to the queue.
     *
     * @param queueName The name of the queue to write index to.
     * @param index The name of the index to write.
     * @param value The new index value to write.
     *
     * @throws APSIOException on failure to write the index.
     */
    private void writeIndex(String queueName, String index, long value) throws APSIOException {
        apsio Void.class, {
            APSFile indexFile

            if (!this.fs.getDirectory(queueName).exists(index)) {
                indexFile = this.fs.getDirectory(queueName).createFile(index)
            }
            else {
                indexFile = this.fs.getDirectory(queueName).getFile(index)
            }

            ObjectOutputStream oos = new ObjectOutputStream(indexFile.createOutputStream())
            try { oos.writeLong(value) }
            finally { oos.close() }
        }
    }

    /**
     * Returns the current read index.
     *
     * @param queueName The queue to get the read index for.
     */
    @Override
    @Implements(QueueStore.class)
    long getReadIndex(String queueName) {
        return readIndex(queueName, "read")
    }

    /**
     * Sets the current read index.
     *
     * @param queueName The queue to set the read index for.
     * @param index The new index to set.
     */
    @Override
    @Implements(QueueStore.class)
    void setReadIndex(String queueName, long index) {
        writeIndex(queueName, "read", index)
    }

    /**
     * Gets the last written index.
     *
     * @param queueName The queue to get the last written index for.
     */
    @Override
    @Implements(QueueStore.class)
    long getWriteIndex(String queueName) {
        return readIndex(queueName, "write")
    }

    /**
     * Sets the last written index.
     *
     * @param queueName The queue to set the last written index for.
     * @param index The index to set.
     */
    @Override
    @Implements(QueueStore.class)
    void setWriteIndex(String queueName, long index) {
        writeIndex(queueName, "write", index)
    }

   /**
     * Returns an InputStream for reading the specified item.
     *
     * @param item The item to read.
     *
     * @throws APSIOException on failure.
     */
    @Override
    @Implements(QueueStore.class)
    InputStream getItemInputStream(String queueName, long item) throws APSIOException {
        apsio InputStream.class, { return this.fs.getDirectory(queueName).getFile("${item}").createInputStream() }
    }

    /**
     * Returns an OutputStream for writing the specified item.
     *
     * @param item The item to write.
     *
     * @throws APSIOException on failure.
     */
    @Override
    @Implements(QueueStore.class)
    OutputStream getItemOutputStream(String queueName, long item) throws APSIOException {
        apsio OutputStream.class, { return this.fs.getDirectory(queueName).getFile("${item}").createOutputStream() }
    }

    /**
     * Deletes the specified item.
     *
     * @param queueName The queue the item belongs to.
     * @param item The item id.
     *
     * @throws APSIOException on failure.
     */
    @Override
    @Implements(QueueStore.class)
    void deleteItem(String queueName, long item) throws APSIOException {
        this.deleteService.submit({
            this.fs.getDirectory(queueName).getFile("${item}").delete()
        })
    }
}
