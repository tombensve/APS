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
 *         2011-08-28: Created!
 *         
 */
package se.natusoft.osgi.aps.api.core.filesystem.model;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * This represents a directory in an _APSFilesystem_.
 *
 * Use this to create or get directories and files and list contents of directories.
 *
 * Personal comment: I do prefer the term "folder" over "directory" since I think that is 
 * less ambiguous, but since Java uses the term "directory" I decided to stick with that name.
 */
public interface APSDirectory extends APSFile {

    /**
     * Returns a newly created directory with the specified name.
     *
     * @param name The name of the directory to create.
     *
     * @throws IOException on any failure.
     */
    APSDirectory createDir(String name) throws IOException;

    /**
     * Returns a newly created directory with the specified name.
     *
     * @param name The name of the directory to create.
     * @param duplicateMessage The exception message if directory already exists.
     *
     * @throws IOException on any failure.
     */
    APSDirectory createDir(String name, String duplicateMessage) throws IOException;

    /**
     * Creates a new file in the directory represented by the current _APSDirectory_.
     *
     * @param name The name of the file to create.
     *
     * @throws IOException on failure.
     */
    APSFile createFile(String name) throws IOException;

    /**
     * Returns the specified directory.
     *
     * @param dirname The name of the directory to enter.
     *
     * @throws FileNotFoundException
     */
    APSDirectory getDir(String dirname) throws FileNotFoundException;

    /**
     * Returns the named file in this directory.
     *
     * @param name The name of the file to get.
     */
    APSFile getFile(String name);

    /**
     * Performs a recursive delete of the directory represented by this _APSDirectory_
     * and all subdirectories and files.
     *
     * @throws IOException on any failure.
     */
    void recursiveDelete() throws IOException;        
    
    /**
     * @see java.io.File#list()
     */
    String[] list();

    /**
     * @see java.io.File#listFiles()
     */
    APSFile[] listFiles();

    
}
