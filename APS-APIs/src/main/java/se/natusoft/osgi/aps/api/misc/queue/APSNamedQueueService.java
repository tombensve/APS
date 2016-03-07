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

import java.io.Serializable;
import java.util.Queue;

/**
 * A named queue as a service. How long lived it is depends on the implementation.
 */
public interface APSNamedQueueService {
    /**
     * Returns true if there is a queue with the specified name.
     *
     * @param name The name of the queue to check for.
     */
    boolean hasQueue(String name);

    /**
     * Returns the named queue. If it does not exist, it is created.
     *
     * @param name The name of the queue to get.
     */
    Queue<? extends Serializable> getQueue(String name);

    /**
     * Removes the named queue.
     *
     * @param name The name of the queue to remove.
     */
    boolean removeQueue(String name);
}
