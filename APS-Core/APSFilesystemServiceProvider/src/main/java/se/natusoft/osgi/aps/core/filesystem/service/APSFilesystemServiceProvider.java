/* 
 * 
 * PROJECT
 *     Name
 *         APS Filesystem Service Provider
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         Provides access to a service/application private filesystem that remains until the
 *         service/application specifically deletes it. This is independent of the OSGi server
 *         it is running in (if configured).
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
 *         2011-08-03: Created!
 *         
 */
package se.natusoft.osgi.aps.core.filesystem.service;

import se.natusoft.osgi.aps.api.core.filesystem.model.APSFilesystem;
import se.natusoft.osgi.aps.api.core.filesystem.service.APSFilesystemService;
import se.natusoft.osgi.aps.core.filesystem.model.APSFilesystemImpl;
import se.natusoft.osgi.aps.tools.APSLogger;

import java.io.File;
import java.io.IOException;

/**
 * Provides an implementation of APSFilesystemService.
 */
public class APSFilesystemServiceProvider implements APSFilesystemService {
    //
    // Private Members
    //
    
    /** Our logger. */
    private APSLogger logger = null;
    
    /** The filesystema root catalog. */
    private String apsFSRoot = null;
    
    //
    // Constructors
    //
    
    /**
     * Creates a new APSFilesystemServiceProvider instance.
     */
    public APSFilesystemServiceProvider(String apsFSRoot, APSLogger logger) {
        this.apsFSRoot = apsFSRoot;
        this.logger = logger;
        
        this.logger.debug("fsRoot: " + this.apsFSRoot);
    }
    
    //
    // Methods
    //

    
    /**
     * Creates a new filesystem for use by an application or service. Where on disk this filesystem resides is irellevant. It is
     * accessed using the "owner", and will exist until it is removed. 
     * 
     * @param owner The owner of the filesystem or rather a unique identifier of it. Concider using application or service package.
     * 
     * @throws IOException on any failure. An already existing filesystem for the "owner" will cause this exception.
     */
    @Override
    public APSFilesystem createFilesystem(String owner) throws IOException {
        APSFilesystemImpl fs = new APSFilesystemImpl(this.apsFSRoot, owner);
        
        this.logger.debug("Created filesystem: " + fs.getRootDirectory());
        
        return fs;
    }

    /**
     * Returns true if the specified owner has a fileystem.
     * 
     * @param owner The owner of the fileystem or rather a unique identifier of it.
     */
    @Override
    public boolean hasFilesystem(String owner) {
        File file = new File(this.apsFSRoot + File.separator + owner);
        return file.exists();
    }

    /**
     * Returns the filesystem for the specified owner.
     * 
     * @param owner The owner of the filesystem or rahter a unique identifier of it.
     * 
     * @throws IOException on any failure.
     */
    @Override
    public APSFilesystem getFilesystem(String owner) throws IOException {
        APSFilesystemImpl fs = new APSFilesystemImpl(this.apsFSRoot, owner);
        if (!fs.getRootDirectory().exists()) {
            throw new IOException("The owner '" + owner + "' has no filesystem! One must be created first!");
        }
        this.logger.debug("Fetched filesystem: " + fs.getRootDirectory());
        
        return fs;
    }

    /**
     * Removes the filesystem and all files in it.
     * 
     * @param owner The owner of the filesystem to delete.
     * 
     * @throws IOException on any failure.
     */
    @Override
    public void deleteFilesystem(String owner) throws IOException {
        APSFilesystem fs = getFilesystem(owner);
        fs.getRootDirectory().recursiveDelete();
    }
    
}
