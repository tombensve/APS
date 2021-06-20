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
 *         2011-06-04: Created!
 *
 */
package se.natusoft.aps.core.filesystem.model;

import se.natusoft.aps.api.core.filesystem.model.APSDirectory;
import se.natusoft.aps.api.core.filesystem.model.APSFile;
import se.natusoft.aps.api.core.filesystem.model.APSFilesystem;
import se.natusoft.aps.exceptions.APSIOException;

import java.io.File;
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
    // Constructors
    //

    /**
     * Creates a new APSFilesystemImpl instance.
     *
     * @param fsRoot The root of the fileystem.
     * @param owner The owner of this filesystem.
     *
     * @throws APSIOException on failure.
     */
    public APSFilesystemImpl(String fsRoot, String owner) {
        File root = new File(fsRoot);
        root = new File(root, owner);

        if (!root.exists()) {
            if (!root.mkdirs())
                throw new APSIOException( "Specified filesystem is not creatable! [" + fsRoot + "]" );
        }

        if (!root.isDirectory() || !root.canRead() || !root.canWrite()) {
            throw new APSIOException("Bad filesystem root! [" + fsRoot + "]");
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
    public APSFile getFile( String path) {
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
    public APSDirectory getDirectory( String path) throws IOException {
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
    public APSDirectory getRootDirectory() {
        return new APSDirectoryImpl(this, "");
    }

}
