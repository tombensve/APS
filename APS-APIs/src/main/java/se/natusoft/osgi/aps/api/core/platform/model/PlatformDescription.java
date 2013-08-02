/* 
 * 
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         0.9.2
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
package se.natusoft.osgi.aps.api.core.platform.model;

/**
 * This model provides information about a platform installation.
 */
public class PlatformDescription {
    
    //
    // Private Members
    //
    
    /** Returns the platform identifier. */
    private String identifier = "";

    /** Returns the type of the platform. */
    private String type = "";

    /** Returns the description of the platform. */
    private String description = "";
    
    //
    // Constructors
    //

    /**
     * Creates a new PlatformDescription.
     */
    public PlatformDescription() {}

    /**
     * Creates a new PlatformDescription.
     *
     * @param identifier An identifying name for the platform.
     * @param type The type of the platform, for example "Development", "SystemTest".
     * @param description A short description of the platform instance.
     */
    public PlatformDescription(String identifier, String type, String description) {
        this.setIdentifier(identifier);
        this.setType(type);
        this.setDescription(description);
    }

    //
    // Methods
    //


    /**
     * Returns the platform identifier.
     */
    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Returns the type of the platform.
     */
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * Returns the description of the platform.
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
