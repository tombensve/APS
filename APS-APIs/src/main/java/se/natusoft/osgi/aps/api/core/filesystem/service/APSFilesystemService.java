/* 
 * 
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         0.10.0
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
 *         2011-06-04: Created!
 *         
 */
package se.natusoft.osgi.aps.api.core.filesystem.service;

import se.natusoft.osgi.aps.api.core.filesystem.model.APSFilesystem;

import java.io.IOException;

/**
 * This provides a filesystem for use by services/applications. Each filesystem has its own root that cannot be navigated
 * outside of. 
 *
 * Services or application using this should do something like this in their activators:
 *
 *     APSFilesystemService fss; 
 *     APSFilesystem fs;
 * 
 *     if (fss.hasFilesystem("my.file.system")) {
 *         fs = fss.getFilsystem("my.file.system");
 *     }
 *     else {
 *         fs = fss.createFilesystem("my.file.system");
 *     }
 *
 */
public interface APSFilesystemService {
    
    // 
    // Constants
    //
        
    /** The configuration key of the filesystem root catalog. */
    static final String CONF_APS_FILESYSTEM_ROOT = "aps.filesystem.root";
    
    //
    // Methods
    //
    
    /**
     * Creates a new filesystem for use by an application or service. Where on disk this filesystem resides is irrelevant. It is
     * accessed using the "owner", and will exist until it is removed. 
     * 
     * @param owner The owner of the filesystem or rather a unique identifier of it. Consider using application or service package.
     * 
     * @throws IOException on any failure. An already existing filesystem for the "owner" will cause this exception.
     */
    APSFilesystem createFilesystem(String owner) throws IOException;
        
    /**
     * Returns true if the specified owner has a filesystem.
     * 
     * @param owner The owner of the filesystem or rather a unique identifier of it.
     */
    boolean hasFilesystem(String owner);
    
    /**
     * Returns the filesystem for the specified owner.
     * 
     * @param owner The owner of the filesystem or rather a unique identifier of it.
     * 
     * @throws IOException on any failure.
     */
    APSFilesystem getFilesystem(String owner) throws IOException;
        
    /**
     * Removes the filesystem and all files in it.
     * 
     * @param owner The owner of the filesystem to delete.
     * 
     * @throws IOException on any failure.
     */
    void deleteFilesystem(String owner) throws IOException;
}
