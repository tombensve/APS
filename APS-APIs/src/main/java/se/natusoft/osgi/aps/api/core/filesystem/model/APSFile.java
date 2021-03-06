/*
 *
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         1.0.0
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
 *         2011-08-28: Created!
 *
 */
package se.natusoft.osgi.aps.api.core.filesystem.model;

import java.io.*;
import java.util.Properties;

/**
 * This represents a file in an _APSFilesystemService_ provided filesystem.
 * It provides most of the API of _java.io.File_ but is not a _java.io.File_!
 * It never discloses the full path in the host filesystem, only paths
 * relative to its _APSFilesystem_ root.
 *
 * Use the createInputStream/OutputStream/Reader/Writer to read and write
 * the file.
 */
public interface APSFile {

    /**
     * Creates a new _InputStream_ to this file.
     *
     * @throws IOException on failure
     */
    InputStream createInputStream() throws IOException;

    /**
     * Creates a new _OutputStream_ to this file.
     *
     * @throws IOException on failure
     */
    OutputStream createOutputStream() throws IOException;

    /**
     * Creates a new _Reader_ to this file.
     *
     * @throws IOException on failure
     */
    Reader createReader() throws IOException;

    /**
     * Creates a new _Writer_ to this file.
     *
     * @throws IOException on failure
     */
    Writer createWriter() throws IOException;

    /**
     * If this file denotes a properties file it is loaded and returned.
     *
     * @throws IOException on failure or if it is not a properties file.
     */
    Properties loadProperties() throws IOException;

    /**
     * If this file denotes a properties file it is written with the specified properties.
     *
     * @param properties The properties to save.
     *
     * @throws IOException on failure or if it is not a properties file.
     */
    void saveProperties(Properties properties) throws IOException;

    /**
     * If this _APSFile_ represents a directory an _APSDirectory_ instance will be returned.
     * Otherwise _null_ will be returned.
     */
    APSDirectory toDirectory();

    /**
     * @see java.io.File#getAbsoluteFile()
     */
    APSFile getAbsoluteFile();

    /**
     * Returns the absolute path relative to filesystem root.
     */
    String getAbsolutePath();

    /**
     * @see java.io.File#getCanonicalFile()
     */
    APSFile getCanonicalFile() throws IOException;

    /**
     * @see java.io.File#getCanonicalPath()
     */
    String getCanonicalPath() throws IOException;

    /**
     * @see java.io.File#getParent()
     */
    String getParent();

    /**
     * @see java.io.File#getParentFile()
     */
    APSDirectory getParentFile();

    /**
     *
     * @see java.io.File#getPath()
     */
    String getPath();

    /**
     * @see java.io.File#renameTo(File)
     */
    boolean renameTo(APSFile dest);

    /**
     * @see java.io.File#getName()
     */
    String getName();

    /**
     * @see java.io.File#canRead()
     */
    boolean canRead();

    /**
     * @see java.io.File#canWrite()
     */
    boolean canWrite();

    /**
     * @see java.io.File#exists()
     */
    boolean exists();

    /**
     * Checks if the named file/directory exists.
     *
     * @param name The name to check.
     *
     * @return true or false.
     */
    boolean exists(String name);

    /**
     * Checks if the named file exists and is not empty.
     *
     * @param name The name of the file to check.
     *
     * @return true or false.
     */
    boolean existsAndNotEmpty(String name);

    /**
     * @see java.io.File#isDirectory()
     */
    boolean isDirectory();

    /**
     * @see java.io.File#isFile()
     */
    boolean isFile();

    /**
     * @see java.io.File#isHidden()
     */
    boolean isHidden();

    /**
     * @see java.io.File#lastModified()
     */
    long lastModified();

    /**
     * @see java.io.File#length()
     */
    long length();

    /**
     * @see java.io.File#createNewFile()
     */
    boolean createNewFile() throws IOException;

    /**
     * @see java.io.File#delete()
     */
    boolean delete();

    /**
     * @see java.io.File#deleteOnExit()
     */
    void deleteOnExit();

    /**
     * Returns a string representation of this _APSFile_.
     */
    @Override
    String toString();

    /**
     * This API tries to hide the real path and don't allow access outside of its root, but sometimes you just
     * need the real path to pass on to other code requiring it. This provides that. Use it only when needed!
     *
     * @return A File object representing the real/full path to this file.
     */
    File toFile();


}
