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
 *         2016-02-27: Created!
 *
 */
package se.natusoft.osgi.aps.api.misc.queue;

import se.natusoft.osgi.aps.exceptions.APSResourceNotFoundException;

import java.io.IOException;
import java.io.Serializable;
import java.util.Queue;

/**
 * A named queue as a service. How long lived it is depends on the implementation.
 */
public interface APSNamedQueueService {

    /**
     * Creates a new queue.
     *
     * @param name The name of the queue to create. If the named queue already exists, it is just returned,
     *             that is, this will work just like getQueue(name) then.
     *
     * @throws APSResourceNotFoundException on failure to create or get existing queue.
     */
    APSQueue createQueue(String name) throws APSResourceNotFoundException;

    /**
     * Returns the named queue. If it does not exist, it is created.
     *
     * @param name The name of the queue to get.
     *
     * @throws APSResourceNotFoundException on failure to get queue.
     */
    APSQueue getQueue(String name) throws APSResourceNotFoundException;

    /**
     * Removes the named queue.
     *
     * @param name The name of the queue to remove.
     *
     * @throws APSResourceNotFoundException on failure to remove the queue.
     */
    void removeQueue(String name) throws APSResourceNotFoundException;
}
