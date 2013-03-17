/* 
 * 
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         0.9.1
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
 *     Tommy Svensson (tommy@natusoft.se)
 *         Changes:
 *         2012-02-17: Created!
 *         
 */
package se.natusoft.osgi.aps.api.core.config.model;

/**
 * This represents a list of a sub-configuration (A config class extending APSConfig, but only referenced by another such class).
 *
 * @param <Subclass> The subclass of APSConfig held by the list.
 */
public interface APSConfigList<Subclass> extends Iterable <Subclass> {

    /**
     * Returns the value at the specified index.
     *
     * @param index The index to return value form.
     */
    public Subclass get(int index);

    /**
     * @return The number of entries in the list.
     */
    public int size();

    /**
     * Returns true if this list is emtpy.
     */
    public boolean isEmpty();

}
