package se.natusoft.osgi.aps.persistentqueue

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.osgi.aps.api.core.filesystem.model.APSDirectory
import se.natusoft.osgi.aps.api.core.filesystem.model.APSFilesystem
import se.natusoft.osgi.aps.api.core.filesystem.service.APSFilesystemService
import se.natusoft.osgi.aps.api.misc.queue.APSNamedQueueService
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.annotation.activator.Managed
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiProperty
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiService
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiServiceProvider

/**
 * Implementation of APSNamedQueueService that is also persistent.
 */
@CompileStatic
@TypeChecked
@OSGiServiceProvider(properties = [@OSGiProperty(name="provider", value="aps-persistent-named-queue-service-provider")])
class APSPersistentNamedQueueProvider implements APSNamedQueueService, FSProvider {

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
     * Gets the filesystem for this service, creating it if it does not exist yet.
     */
    APSFilesystem getFs() {
        if (!this.fsService.hasFilesystem(FS_OWNER)) {
            this.fsService.createFilesystem(FS_OWNER)
        }

        this.fsService.getFilesystem(FS_OWNER)
    }

    /**
     * Returns true if there is a queue with the specified name.
     *
     * @param name The name of the queue to check for.
     */
    @Override
    boolean hasQueue(String name) {
        this.fs.getDirectory(name).exists()
    }

    /**
     * Returns the named queue. If it does not exist, it is created.
     *
     * @param name The name of the queue to get.
     */
    @Override
    Queue<? extends Serializable> getQueue(String name) {
        return null
    }

    /**
     * Removes the named queue.
     *
     * @param name The name of the queue to remove.
     */
    @Override
    boolean removeQueue(String name) {
        return false
    }
}
