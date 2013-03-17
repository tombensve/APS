/* 
 * 
 * PROJECT
 *     Name
 *         APS Configuration Service Provider
 *     
 *     Code Version
 *         0.9.1
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
 *     tommy ()
 *         Changes:
 *         2011-06-04: Created!
 *         
 */
package se.natusoft.osgi.aps.core.test.support.filesystem.model;

import se.natusoft.osgi.aps.api.core.filesystem.model.APSFilesystem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * This represents an APSFilesystemService filesytem.
 */
public class APSFilesystemImpl implements APSFilesystem {
    //
    // Private Members
    //
    
    /** The filesystem root. */
    private String fsRoot = null;
    
    /** The filesystem owner. */
    private String owner = null;

    //
    // Cosntructors
    //
    
    /**
     * Creates a new APSFilesystemImpl instance.
     * 
     * @param fsRoot The root of the fileystem.
     * @param owner The owner of this filesystem.
     * 
     * @throws FileNotFoundException on failure.
     */
    public APSFilesystemImpl(String fsRoot, String owner) throws FileNotFoundException {        
        File root = new File(fsRoot);
        if (!root.exists()) {
            throw new FileNotFoundException("Specified filesystem does not exist! [" + fsRoot + "]");
        }
        if (!root.isDirectory() || !root.canRead() || !root.canWrite()) {
            throw new FileNotFoundException("Bad filesystem root! [" + fsRoot + "]");
        }
        root = new File(root, owner);
        if (!root.exists()) {
            root.mkdirs();
        }
        this.fsRoot = root.getAbsolutePath();
        this.owner = owner;
    }
    
    //
    // Methods
    //

    /**
     * Converts the path for display.
     *
     * @param path The path to be displayed.
     */
    String toDisplayPath(String path) {
        return "APSFS@" + this.owner + ":" + toFSRelativePath(path);
    }
    
    /**
     * Returns the filesystem relative path.
     * 
     * @param path The path to return relative to the filesystem.
     */
    String toFSRelativePath(String path) {
        if (path.startsWith(this.fsRoot)) {
            path = path.substring(this.fsRoot.length());
        }

        return path;
    }

    /** 
     * Takes a filesystem relative path and turns it into a system full path.
     * 
     * @param fsPath The filesystem relative path to convert.
     */
    String toFullSysPath(String fsPath) {
        if (fsPath != null && fsPath.trim().length() != 0) {
            return this.fsRoot + File.separator + fsPath;
        }
        return this.fsRoot;
    }
    
    /**
     * Returns true if the specified path is the filesystem root path.
     * 
     * @param path The path to test.
     */
    boolean isFSRoot(String path) {
        return this.fsRoot.trim().equals(path.trim());
    }

    /**
     * Returns the file or folder of the specifeid path.
     * 
     * @param path The path of the file.
     */
    @Override
    public APSFileImpl getFile(String path) {
        return new APSFileImpl(this, path);
    }
    
    /**
     * Returns a folder at the specified path. 
     * 
     * @param path The path of the folder to get.
     * 
     * @throws IOException on any failure, specifically if the specified path is not a folder or doesn't exist.
     */
    @Override
    public APSDirectoryImpl getDirectory(String path) throws IOException {
        APSDirectoryImpl dir = new APSDirectoryImpl(this, path);
        if (!dir.exists()) {
            throw new IOException("Specified path does not exist! [" + path + "]");
        }
        if (!dir.isDirectory()) {
            throw new IOException("Specified path is not a directory! [" + path + "]");
        }
        
        return dir;
    }
    
    /**
     * Returns the root directory.
     */
    @Override
    public APSDirectoryImpl getRootDirectory() {
        return new APSDirectoryImpl(this, "");
    }
    
}
