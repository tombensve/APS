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
package se.natusoft.osgi.aps.log.model;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogEntry;

/**
 * Implementation of standard OSGi LogEntry interface.
 */
public class APSLogEntry implements LogEntry {
    //
    // Private Members
    //
        
    /** The service that made the log. */
    private ServiceReference serviceReference;
    
    /** The log level of the entry. Uses the LogService interface constants. */
    private int level;
    
    /** The log message. */
    private String message;
    
    /** A possible exception being the cause of the log entry. */
    private Throwable exception;
    
    /** The time of the log entry. */
    private long time;
    
    //
    // Constructors
    //
    
    /**
     * Creates a new APSLogEntry instance.
     * 
     * @param serviceReference The service that made the log or null for an anonymous log.
     * @param level The log level of the log.
     * @param message The log message.
     * @param exception An optional exception passed with the log. Can be null.
     */
    public APSLogEntry(ServiceReference serviceReference, int level, String message, Throwable exception) {
        this.serviceReference = serviceReference;
        this.level = level;
        this.message = message;
        this.exception = exception;
        this.time = System.currentTimeMillis();
    }
    
    //
    // Methods
    //

    /**
     * Returns the bundle of the service that made the log or null if anonymous log.
     */
    @Override
    public Bundle getBundle() {
        return this.serviceReference != null ? this.serviceReference.getBundle() : null;
    }

    /**
     * Returns the reference to the service that made the log or null if anonymous log.
     */
    @Override
    public ServiceReference getServiceReference() {
        return this.serviceReference;
    }

    /**
     * Returns the log level.
     */
    @Override
    public int getLevel() {
        return this.level;
    }

    /**
     * Returns the log message.
     */
    @Override
    public String getMessage() {
        return this.message;
    }

    /**
     * Returns the log exception if any, null otherwise.
     */
    @Override
    public Throwable getException() {
        return this.exception;
    }

    /**
     * Returns the log time.
     */
    @Override
    public long getTime() {
        return this.time;
    }
    
}
