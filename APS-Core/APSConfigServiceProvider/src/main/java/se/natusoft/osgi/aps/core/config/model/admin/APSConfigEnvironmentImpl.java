/* 
 * 
 * PROJECT
 *     Name
 *         APS Configuration Service Provider
 *     
 *     Code Version
 *         0.11.0
 *     
 *     Description
 *         A more advanced configuration service that uses annotated interfaces to
 *         describe and provide access to configuration. It supports structured
 *         configuration models.
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
 *         2011-05-29: Created!
 *         
 */
package se.natusoft.osgi.aps.core.config.model.admin;

import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigEnvironment;

/**
 * This represents a configuration environment. For example: Development, Systemtest, Acceptancetest, Production.
 * <p>
 * It is however seldom that simple, so instead of making an enum defining the above, this model represents one
 * environment, and can be instantiated for as many environment alternatives as are available. 
 * <p>
 * As an example there might be more than one development environment due to different stages of development
 * uses different backend services. 
 * <p>
 * A configuration can be published for each defined environment. 
 */
public class APSConfigEnvironmentImpl implements APSConfigEnvironment {
    //
    // Private Members
    //
    
    /** The name of the environment. */
    private String name;
    
    /** A description of the environment. */    
    private String description;

    /** The timestamp of this config env. */
    private long timestamp = 0;
    
    //
    // Constructors
    //
    
    /**
     * Creates a new APSConfigEnvironmentImpl insance.
     *
     * @param name The name of the environment.
     * @param description The description of the environment.
     * @param timestamp
     */
    public APSConfigEnvironmentImpl(String name, String description, long timestamp) {
        this.name = name;
        this.description = description;
        this.timestamp = timestamp;
    }
    
    //
    // Methods
    //
    
    /**
     * Returns the name of the environment.
     */
    @Override
    public String getName() {
        return this.name;
    }
    
    /**
     * Returns the description of the environment.
     */
    @Override
    public String getDescription() {
        return this.description;
    }

    /**
     * Returns the timestamp of the environment.
     */
    @Override
    public long getTimestamp() {
        return this.timestamp;
    }

    /**
     * Returns the hash code for this object
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }

    /**
     * Compares this object with another for equality.
     * 
     * @param obj The object to compare with this.
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof APSConfigEnvironmentImpl)) {
            return false;
        }
        APSConfigEnvironmentImpl ce = (APSConfigEnvironmentImpl)obj;
        return ce.name.equals(this.name);
    }
    
    /**
     * Returns the name.
     */
    @Override
    public String toString() {
        return getName();
    }
}
