/* 
 * 
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         0.9.0
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
 *         2011-05-29: Created!
 *         
 */
package se.natusoft.osgi.aps.api.core;

/**
 * This represents a general version.
 */
public class APSVersion {
    //
    // Private Members
    //
    
    /** The major version number. */
    private int major = 0;
    
    /** The minor version number. */
    private int minor = 0;
    
    /** The build number. */
    private int build = 0;
    
    /** A description of the version. For example "May Release". */    
    private String description;
        
    //
    // Constructors
    //
    
    /**
     * Creates a new APSVersion insance.
     * 
     * @param major The major part of the version number.
     * @param minor The minor part of the version number.
     * @param build The build part of the version number.
     * @param description The description of the version.
     */
    public APSVersion(int major, int minor, int build, String description) {
        this.major = major;
        this.minor = minor;
        this.build = build;
        this.description = description;
    }
        
    //
    // Methods
    //
    
    /**
     * Returns the version number.
     */
    public String getVersion() {
        return this.major + "." + this.minor + "." + this.build;
    }
    
    /**
     * Returns the major version number.
     */
    public int getMajor() {
        return this.major;
    }
    
    /**
     * Returns the minor version number.
     */
    public int getMinor() {
        return this.minor;
    }
    
    /**
     * Returns the build number.
     */
    public int getBuild() {
        return this.build;
    }
    
    /**
     * Returns the description of the version.
     */
    public String getDescription() {
        return this.description;
    }
    
    /**
     * Compares for equality with specified object.
     * 
     * @param obj Object to compare to.
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof APSVersion)) {
            return false;
        }
        
        APSVersion cto = (APSVersion)obj;
        return cto.major == this.major && cto.minor == this.minor && cto.build == this.build;
    }

    /**
     * Returns the hash code for this object.
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + this.major;
        hash = 61 * hash + this.minor;
        hash = 61 * hash + this.build;
        return hash;
    }
}
