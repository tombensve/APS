package se.natusoft.osgi.aps.persistentqueue

import se.natusoft.osgi.aps.api.core.filesystem.model.APSDirectory
import se.natusoft.osgi.aps.api.core.filesystem.model.APSFile
import se.natusoft.osgi.aps.api.misc.queue.APSQueue
import se.natusoft.osgi.aps.api.misc.queue.APSQueueException
import se.natusoft.osgi.aps.tools.APSLogger

/**
 *
 */
class PersistentQueue implements APSQueue<Serializable> {

    //
    // Private Members
    //

    private Queue<UUID> queueRefs = new LinkedList<>()

    private Queue<Serializable> itemCache = new LinkedList<>()

    private boolean indexLoaded = false

    //
    // Properties
    //

    /** Logger to log to. */
    APSLogger logger

    /** Provides the filesystem. */
    FSProvider fsProvider

    /** The name of this queue. */
    String queueName

    //
    // Methods
    //

    /**
     * Returns the directory where queue data is stored.
     */
    private APSDirectory getQueueStore() {
        this.fsProvider.fs.getDirectory(queueName)
    }

    private synchronized final void loadIndex() {
        if (!this.indexLoaded) {
            try {
                ObjectInputStream ois = new ObjectInputStream(this.queueStore.getFile("index").createInputStream())
                int size = ois.readInt()

                int read = 0
                while (read < size) {
                    this.queueRefs.add(ueois.readObject() as UUID)
                    ++read
                }

                ois.close()
            }
            catch (IOException ioe) {
                this.logger.error("Failed to load queue index!", ioe)
                throw new APSQueueException("Failed to load queue index!", ioe)
            }

            this.indexLoaded = true
        }
    }

    private synchronized final void saveIndex() {
        try {
            this.queueStore.getFile("index").renameTo(this.queueStore.getFile("index.old"))
            ObjectOutputStream oos = new ObjectOutputStream(this.queueStore.getFile("index").createOutputStream())
            oos.writeInt(this.queueRefs.size())
            this.queueRefs.each { UUID queueRef ->
                oos.writeObject(queueRef)
            }

            oos.close()
        }
        catch (IOException ioe) {
            this.logger.error("Failed to save queue index!", ioe)
            throw new APSQueueException("Failed to save queue index!", ioe)
        }
        finally {
            APSFile oldIndex = this.queueStore.getFile("index.old")
            if (oldIndex.exists()) {
                oldIndex.delete()
            }
        }
    }

    private synchronized void writeItem(UUID itemRef, Serializable item) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(this.queueStore.getFile(itemRef.toString()).createOutputStream())
            oos.writeObject(item)
            oos.close()
        }
        catch (IOException ioe) {
            this.logger.error("Failed to write item '${itemRef}'!", ioe)
            throw new APSQueueException("Failed to write item '${itemRef}'!", ioe)
        }
    }

    private Serializable readItem(UUID itemRef) {
        try {
            ObjectInputStream ois = new ObjectInputStream(this.queueStore.getFile(itemRef.toString()).createInputStream())
            Serializable item = ois.readObject() as Serializable
            ois.close()

            return item
        }
        catch (IOException ioe) {
            this.logger.error("Failed to load item '${itemRef}'!", ioe)
            throw new APSQueueException("Failed to load item '${itemRef}'!", ioe)
        }
    }

    /**
     * Pushes a new item to the end of the list.
     *
     * @param item The item to add to the list.
     *
     * @throws APSQueueException on any failure to do this operation.
     */
    @Override
    void push(Serializable item) throws APSQueueException {
        loadIndex()
    }

    /**
     * Pulls the first item in the queue, removing it from the queue.
     *
     * @return The pulled item.
     *
     * @throws APSQueueException on any failure to do this operation.
     */
    @Override
    Serializable pull() throws APSQueueException {
        loadIndex()
        return null
    }

    /**
     * Looks at, but does not remove the first item in the queue.
     *
     * @return The first item in the queue.
     * @throws APSQueueException
     */
    @Override
    Serializable peek() throws APSQueueException {
        loadIndex()
        return null
    }

    /**
     * Returns the number of items in the queue.
     */
    @Override
    int size() {
        loadIndex()
        return this.queueRefs.size()
    }

    /**
     * Returns true if this queue is empty.
     */
    @Override
    boolean isEmpty() {
        return size() == 0
    }
}
