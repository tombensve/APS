/*
 *
 * PROJECT
 *     Name
 *         APS APIs
 *
 *     Code Version
 *         1.0.0
 *
 *     Description
 *         Provides the APIs for the application platform services.
 *
 * COPYRIGHTS
 *     Copyright (C) 2012 by Natusoft AB All rights reserved.
 *
 * LICENSE
 *     Apache 2.0 (Open Source)
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 * AUTHORS
 *     tommy ()
 *         Changes:
 *         2018-05-26: Created!
 *
 */
package se.natusoft.aps.types;

/**
 * This API can be implemented by services also supporting locking.
 */
public interface APSLockable<ID> {

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
