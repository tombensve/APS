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

import se.natusoft.osgi.aps.codedoc.Optional;
import se.natusoft.osgi.aps.exceptions.APSIOException;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * This defines a simple queue api.
 */
@SuppressWarnings("WeakerAccess")
public interface APSQueue {

    /**
     * Pushes a new item to the end of the list.
     *
     * @param item The item to add to the list.
     *
     * @throws APSIOException on any failure to do this operation.
     */
    void push(byte[] item) throws APSIOException;

    /**
     * Pulls the first item in the queue, removing it from the queue.
     *
     * @return The pulled item.
     *
     * @throws APSIOException on any failure to do this operation.
     */
    byte[] pull() throws APSIOException;

    /**
     * Looks at, but does not remove the first item in the queue.
     *
     * @return The first item in the queue.
     *
     * @throws APSIOException on any failure to do this operation.
     * @throws UnsupportedOperationException If this operation is not supported by the implementation.
     */
    @Optional
    byte[] peek() throws APSIOException, UnsupportedOperationException;

    /**
     * Returns the number of items in the queue.
     *
     * @throws APSIOException on any failure to do this operation.
     * @throws UnsupportedOperationException If this operation is not supported by the implementation.
     */
    @Optional
    int size() throws APSIOException, UnsupportedOperationException;

    /**
     * Returns true if this queue is empty.
     *
     * @throws APSIOException on any failure to do this operation.
     */
    boolean isEmpty() throws APSIOException;
}
