/* 
 * 
 * PROJECT
 *     Name
 *         APS Administration Web Registration Service
 *     
 *     Code Version
 *         0.9.1
 *     
 *     Description
 *         The service for registering admin webs with aps-admin-web.
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
 *         2011-08-27: Created!
 *         
 */
package se.natusoft.osgi.aps.apsadminweb.service.model;

/**
 * This model holds information about a registered admin web application.
 */
public class AdminWebReg {
    //
    // Private Members
    //
    
    /** A (short) name of the admin web. */
    private String name;
    
    /** The version of the admin web. */
    private String version;
    
    /** A longer description of the admin web. */
    private String description;
    
    /** The deployment url of the admin web. */
    private String url;
    
    //
    // Constructors
    //
    
    /**
     * Creates a new AdminWebReg instance.
     * 
     * @param name A (short) name of the admin web.
     * @param version The version of the admin web.
     * @param description A longer description of the admin web.
     * @param url The deployment url of the admin web.
     */
    public AdminWebReg(String name, String version, String description, String url) {
        this.name = name;
        this.version = version;
        this.description = description;
        this.url = url;
    }
    
    //
    // Methods
    //
    
    /**
     * @return The (short) name of the admin web.
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * @return The version of the admin web.
     */
    public String getVersion() {
        return this.version;
    }
    
    /**
     * @return The description of the admin web.
     */
    public String getDescription() {
        return this.description;
    }
    
    /**
     * @return The deployment url of the admin web.
     */
    public String getUrl() {
        return this.url;
    }
    
    /**
     * Compares this object with specifed object for equlity.
     * 
     * @param obj The object to compare to.
     * 
     * @return true if they are equal, false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AdminWebReg)) {
            return false;
        }
        AdminWebReg awrObj = (AdminWebReg)obj;
        return this.name.equals(awrObj.name) && this.version.equals(awrObj.version);
    }

    /**
     * @return The hash code of this object.
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 29 * hash + (this.version != null ? this.version.hashCode() : 0);
        return hash;
    }
    
    /**
     * @return a String representation of this object (name:version).
     */
    @Override
    public String toString() {
        return this.name + ":" + this.version;
    }
}
