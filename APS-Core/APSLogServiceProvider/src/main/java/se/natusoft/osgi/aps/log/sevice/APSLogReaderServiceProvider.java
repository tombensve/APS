/* 
 * 
 * PROJECT
 *     Name
 *         APSLogServiceProvider OSGi Bundle
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         Application Platform Services root maven parent.
 *         
 * COPYRIGHTS
 *     Copyright (C) 2011 by Biltmore Group AB All rights reserved.
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
 *         2011-08-05: Created!
 *         
 */
package se.natusoft.osgi.aps.log.sevice;

import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogReaderService;

import java.util.Enumeration;

/**
 * Implements the OSGi standard LogReaderServcie interface. 
 */
public class APSLogReaderServiceProvider implements LogReaderService {
    //
    // Private Members
    //
    
    
    //
    // Constructors
    //
    
    public APSLogReaderServiceProvider() {
        
    }
    
    //
    // Methods
    //

    /**
     * Adds a log listener that will receive all new logs made until
     * it is removed.
     * 
     * @param listener The listener to add.
     */
    @Override
    public void addLogListener(LogListener listener) {

    }

    /**
     * Removes a log listener from receiving logs.
     * 
     * @param listener The listener to remove.
     */
    @Override
    public void removeLogListener(LogListener listener) {

    }

    /**
     * Returns all the entries in the in memory log cache. It will not
     * return older entries on disk! The size of the in memory cahce is
     * configurable.
     */
    @Override
    public Enumeration getLog() {
        return null;
    }
    
}
