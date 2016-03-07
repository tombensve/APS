package se.natusoft.osgi.aps.persistentqueue

import se.natusoft.osgi.aps.api.core.filesystem.model.APSFilesystem

/**
 * API for getting the filesystem.
 */
interface FSProvider {
    APSFilesystem getFs();
}
