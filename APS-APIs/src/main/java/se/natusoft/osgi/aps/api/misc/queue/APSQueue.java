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

/**
 * This defines a simple queue api.
 */
public interface APSQueue<T> {

    /**
     * Pushes a new item to the end of the list.
     *
     * @param item The item to add to the list.
     *
     * @throws APSQueueException on any failure to do this operation.
     */
    void push(T item) throws APSQueueException;

    /**
     * Pulls the first item in the queue, removing it from the queue.
     *
     * @return The pulled item.
     *
     * @throws APSQueueException on any failure to do this operation.
     */
    T pull() throws APSQueueException;

    /**
     * Looks at, but does not remove the first item in the queue.
     *
     * @return The first item in the queue.
     * @throws APSQueueException
     */
    T peek() throws APSQueueException;

    /**
     * Returns the number of items in the queue.
     */
    int size();

    /**
     * Returns true if this queue is empty.
     */
    boolean isEmpty();
}
