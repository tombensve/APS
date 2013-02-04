/* 
 * 
 * PROJECT
 *     Name
 *         APS Web Tools
 *     
 *     Code Version
 *         0.9.0
 *     
 *     Description
 *         This provides some utility classes for web applications.
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
 *     Tommy Svensson (tommy@natusoft.se)
 *         Changes:
 *         2012-03-17: Created!
 *         
 */
package se.natusoft.osgi.aps.tools.web.vaadin.tools;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Manages a set of Refreshable objects.
 */
public class Refreshables implements Iterable<Refreshable> {
    //
    // Private Members
    //

    /** Holds the refreshables. */
    private List<Refreshable> refreshableList = new LinkedList<Refreshable>();


    //
    // Constructors
    //

    /**
     * Creates a new Refreshables instance.
     */
    public Refreshables() {}

    //
    // Methods
    //

    /**
     * Adds a refreshable to the refreshables.
     *
     * @param refreshable The refreshable to add.
     */
    public void addRefreshable(Refreshable refreshable) {
        this.refreshableList.add(refreshable);
    }

    /**
     * Returns an iterator over a set of elements of type T.
     *
     * @return an Iterator.
     */
    @Override
    public Iterator<Refreshable> iterator() {
        return this.refreshableList.iterator();
    }

    /**
     * Calls on the managed refreshables to do refresh.
     */
    public void refresh() {
        for (Refreshable refreshable : this) {
            refreshable.refresh();
        }
    }
}
