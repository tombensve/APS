package se.natusoft.osgi.aps.persistentqueue

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.osgi.aps.api.misc.queue.APSQueue
import se.natusoft.osgi.aps.codedoc.Implements
import se.natusoft.osgi.aps.exceptions.APSIOException
import se.natusoft.osgi.aps.tools.APSLogger

/**
 * This represents a specific named queue.
 */
@CompileStatic
@TypeChecked
class PersistentQueue implements APSQueue {

    //
    // Private Members
    //

    private LinkedList<UUID> queueRefs = new LinkedList<>()

    private boolean indexLoaded = false

    //
    // Properties
    //

    /** Logger to log to. */
    APSLogger logger

    /** The name of the queue. */
    String queueName

    /** Provides the filesystem. */
    QueueStore queueStore

    private boolean modified = false

    private Timer timer = new Timer()

    //
    // Constructors
    //

    public PersistentQueue() {
        this.timer.scheduleAtFixedRate(new SaveTask(), 5000, 5000)
    }

    //
    // Inner Classes
    //

    private class SaveTask extends TimerTask {
        public void run() {
            if (PersistentQueue.this.modified) {
                saveIndex()
            }
        }
    }

    //
    // Methods
    //

    /**
     * Cleans up in this instance.
     */
    void release() {
        this.timer.cancel()
    }

    /**
     * Loads the index from disk.
     */
    private synchronized final void loadIndex() {
        if (!this.indexLoaded) {
            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(this.queueStore.getIndexInputStream(this.queueName))
                int size = ois.readInt()

                int read = 0
                while (read < size) {
                    this.queueRefs.add(ois.readObject() as UUID)
                    ++read
                }
            }
            catch (EOFException eofe) {
                // This happens when the index have been newly created and does not yet even contain a size.
            }
            catch (IOException ioe) {
                this.logger.error("Failed to load queue index!", ioe)
                throw new APSIOException("Failed to load queue index!", ioe)
            }
            finally {
                if (ois != null) ois.close()
            }

            this.indexLoaded = true
        }
    }

    /**
     * Saves the index to disk.
     *
     * @param undo This is executed on failure.
     */
    private synchronized final void saveIndex() {
        ObjectOutputStream oos = null
        try {
            this.queueStore.backupIndex(this.queueName)
            oos = new ObjectOutputStream(this.queueStore.getIndexOutputStream(this.queueName))
            oos.writeInt(this.queueRefs.size())
            this.queueRefs.each { UUID queueRef ->
                oos.writeObject(queueRef)
            }
        }
        catch (IOException ioe) {
            this.queueStore.restoreBackupIndex(this.queueName)
            this.indexLoaded = false
            this.logger.error("Failed to save queue index!", ioe)
            throw new APSIOException("Failed to save queue index!", ioe)
        }
        finally {
            if (oos != null) oos.close()
        }

        this.queueStore.removeBackupIndex(this.queueName)
    }

    /**
     * Writes an item to the queue.
     *
     * @param itemRef The item reference.
     * @param item The item to write.
     */
    private void writeItem(UUID itemRef, byte[] item) {
        ObjectOutputStream itemStream = null
        try {
            itemStream = new ObjectOutputStream(this.queueStore.getItemOutputStream(this.queueName, itemRef))
            itemStream.writeInt(item.length)
            itemStream.write(item)
        }
        catch (IOException ioe) {
            this.logger.error("Failed to write item!", ioe)
            throw new APSIOException("Failed to write item!", ioe)
        }
        finally {
            if (itemStream != null) itemStream.close()
        }
    }

    /**
     * Reads an item from the queue.
     *
     * @param itemRef The item reference to read.
     */
    private byte[] readItem(UUID itemRef) {
        ObjectInputStream itemStream = null
        try {
            itemStream = new ObjectInputStream(this.queueStore.getItemInputStream(this.queueName, itemRef))
            int length = itemStream.readInt();
            byte[] bytes = new byte[length]
            itemStream.read(bytes)

            return bytes
        }
        catch (IOException ioe) {
            this.logger.error("Failed to load item!", ioe)
            throw new APSIOException("Failed to load item!", ioe)
        }
        finally {
            if (itemStream != null) itemStream.close()
        }
    }

    /**
     * Deletes an item.
     *
     * @param itemRef The reference of the item to delete.
     */
    private void deleteItem(UUID itemRef) {
        this.queueStore.deleteItem(this.queueName, itemRef)
    }

    /**
     * Executes the closure hiding all exceptions.
     *
     * @param quiteOp The closure to execute.
     */
    private static void silently(Closure quiteOp) {
        try {
            quiteOp.call()
        }
        catch (Exception ignore) {}
    }

    /**
     * Peeks the next entry in the queue and throws exception if the queue is empty.
     */
    private UUID peekNotEmpty() {
        UUID polledItemRef = this.queueRefs.peek()
        if (polledItemRef == null) throw new APSIOException("The queue is empty!")

        polledItemRef
    }

    /**
     * Pushes a new item to the end of the list.
     *
     * @param item The item to add to the list.
     *
     * @throws APSIOException on any failure to do this operation.
     */
    @Override
    @Implements(APSQueue.class)
    synchronized void push(byte[] item) throws APSIOException {
        loadIndex()

        UUID newItemRef = UUID.randomUUID()
        this.queueRefs.offer(newItemRef)

        try {
            writeItem(newItemRef, item)
        }
        catch (APSIOException aioe) {
            silently { deleteItem(newItemRef) }
            throw aioe
        }
    }

    /**
     * Pulls the first item in the queue, removing it from the queue.
     *
     * @return The pulled item.
     *
     * @throws APSIOException on any failure to do this operation.
     */
    @Override
    @Implements(APSQueue.class)
    synchronized byte[] pull() throws APSIOException {
        loadIndex()

        UUID itemRef = peekNotEmpty()

        byte[] itemBytes = readItem(itemRef)
        this.queueRefs.poll() // Now we can remove it.

        itemBytes
    }

    /**
     * Looks at, but does not remove the first item in the queue.
     *
     * @return The first item in the queue.
     * @throws APSIOException
     */
    @Override
    @Implements(APSQueue.class)
    synchronized byte[] peek() throws APSIOException {
        loadIndex()

        UUID itemRef = peekNotEmpty()

        readItem(itemRef)
    }

    /**
     * Returns the number of items in the queue.
     */
    @Override
    @Implements(APSQueue.class)
    synchronized int size() {
        loadIndex()
        return this.queueRefs.size()
    }

    /**
     * Returns true if this queue is empty.
     */
    @Override
    @Implements(APSQueue.class)
    synchronized boolean isEmpty() {
        return size() == 0
    }
}
