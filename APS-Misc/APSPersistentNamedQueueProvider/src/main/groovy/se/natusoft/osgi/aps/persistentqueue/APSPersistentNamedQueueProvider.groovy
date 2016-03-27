package se.natusoft.osgi.aps.persistentqueue

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.osgi.aps.api.core.filesystem.model.APSFile
import se.natusoft.osgi.aps.api.core.filesystem.model.APSFilesystem
import se.natusoft.osgi.aps.api.core.filesystem.service.APSFilesystemService
import se.natusoft.osgi.aps.api.misc.queue.APSNamedQueueService
import se.natusoft.osgi.aps.api.misc.queue.APSQueue
import se.natusoft.osgi.aps.exceptions.APSIOException
import se.natusoft.osgi.aps.exceptions.APSResourceNotFoundException
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.annotation.activator.Managed
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiProperty
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiService
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiServiceProvider

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

    //
    // Methods
    //

    /**
     * Provides a getter for the root filesystem of the service.
     */
    private APSFilesystem getFs() {
        APSFilesystem _fs
        if (this.fsService.hasFilesystem(FS_OWNER)) {
            _fs = this.fsService.getFilesystem(FS_OWNER)
        }
        else {
            _fs = this.fsService.createFilesystem(FS_OWNER)
        }

        return _fs
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
     * Creates a new queue.
     *
     * @param name The name of the queue to create. If the named queue already exists, it is just returned,
     *             that is, this will work just like getQueue(name) then.
     *
     * @throws APSResourceNotFoundException on failure to create or get existing queue.
     */
    @Override
    APSQueue createQueue(String name) throws APSResourceNotFoundException {
        if (!this.fs.getDirectory(name).exists()) {
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
    APSQueue getQueue(String name) {
        return new PersistentQueue(
                logger: this.logger,
                queueName: name,
                queueStore: this
        )
    }

    /**
     * Removes the named queue.
     *
     * @param name The name of the queue to remove.
     *
     * @throws APSIOException on failure.
     */
    @Override
    void removeQueue(String name) throws APSIOException {
        apsio Void.class, {
            if (this.fs.getDirectory(name).exists()) {
                this.fs.getDirectory(name).recursiveDelete()
            }
        }
    }

    /**
     * Returns an InputStream for reading the index.
     *
     * @throws IOException on failure.
     */
    @Override
    InputStream getIndexInputStream(String queueName) throws APSIOException {
        apsio InputStream.class, { this.fs.getDirectory(queueName).getFile("index").createInputStream() }
    }

    /**
     * Returns an OutputStream for writing the index.
     *
     * @throws IOException on failure.
     */
    @Override
    OutputStream getIndexOutputStream(String queueName) throws APSIOException {
        apsio OutputStream.class, { this.fs.getDirectory(queueName).getFile("index").createOutputStream() }
    }

    /**
     * Backups the current index. Should be done before writing a new.
     *
     * @param queueName The queue to backup index for.
     *
     * @throws APSIOException on any failure.
     */
    @Override
    void backupIndex(String queueName) throws APSIOException {
        apsio Void.class, {
            this.fs.getDirectory(queueName).getFile("index").renameTo(this.fs.getDirectory(queueName).getFile("index.bup"))
        }
    }

    /**
     * Restores a backed up index.
     *
     * @param queueName The queue to restore the backed up index for.
     *
     * @throws APSIOException on any failure.
     */
    @Override
    void restoreBackupIndex(String queueName) throws APSIOException {
        apsio Void.class, {
            APSFile currentIx = this.fs.getDirectory(queueName).getFile("index")
            if (currentIx.exists()) {
                currentIx.delete()
            }
            this.fs.getDirectory(queueName).getFile("index.bup").renameTo(this.fs.getDirectory(queueName).getFile("index"))
        }
    }

    /**
     * Removes a backup index. This should be called after successful rewrite of the index.
     *
     * @param queueName The queue to remove the backup index for.
     *
     * @throws APSIOException on any failure.
     */
    @Override
    void removeBackupIndex(String queueName) throws APSIOException {
        apsio Void.class, {
            APSFile bupIX = this.fs.getDirectory(queueName).getFile("index.bup")
            if (bupIX.exists()) {
                bupIX.delete()
            }
        }
    }

   /**
     * Returns an InputStream for reading the specified item.
     *
     * @param item The item to read.
     *
     * @throws APSIOException on failure.
     */
    @Override
    InputStream getItemInputStream(String queueName, UUID item) throws APSIOException {
        apsio InputStream.class, { return this.fs.getDirectory(queueName).getFile(item.toString()).createInputStream() }
    }

    /**
     * Returns an OutputStream for writing the specified item.
     *
     * @param item The item to write.
     *
     * @throws APSIOException on failure.
     */
    @Override
    OutputStream getItemOutputStream(String queueName, UUID item) throws APSIOException {
        apsio OutputStream.class, { return this.fs.getDirectory(queueName).getFile(item.toString()).createOutputStream() }
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
    void deleteItem(String queueName, UUID item) throws APSIOException {
        apsio Void.class, { this.fs.getDirectory(queueName).getFile(item.toString()).delete() }
    }
}
