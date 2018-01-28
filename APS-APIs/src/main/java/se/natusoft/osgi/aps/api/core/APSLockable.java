package se.natusoft.osgi.aps.api.core;

import se.natusoft.osgi.aps.api.reactive.APSHandler;
import se.natusoft.osgi.aps.api.reactive.APSResult;

/**
 * This API can be implemented by services also supporting locking.
 */
public interface APSLockable<ID> {

    /**
     * @return true if locking is supported, false otherwise.
     */
    boolean supportsLocking();

    /**
     * Acquires a lock, and on success also provides an APSLock instance. Do note that even if the APSLock instance
     * is not used to release the lock, the lock should be released before the return of this method call!
     *
     * @param lockId Something that identifies what to be locked.
     * @param resultHandler A handler in which whatever is locked can be used if result indicates success.
     */
    void lock(ID lockId, APSHandler<APSResult<APSLock>> resultHandler);

    /**
     * This represents a lock.
     */
    interface APSLock {

        /**
         * Releases a lock.
         *
         * @param resultHandler Provides and APSResult indicating success or failure. It is possible to provide some object
         *                      also, but not required. If no object use without generics rather than specifying Object as
         *                      generic type! And definitely do not specify 'Void' or 'void' :-).
         */
        void release(APSHandler<APSResult> resultHandler);

    }
}
