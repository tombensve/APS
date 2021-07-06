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
package se.natusoft.aps.api.core.filesystem.model;

import java.io.IOException;

/**
 * This represents an _APSFilesystemService_ filesystem.
 */
public interface APSFilesystem {

    /**
     * Returns a folder at the specified path.
     *
     * @param path The path of the folder to get.
     *
     * @throws IOException on any failure, specifically if the specified path is not a folder or doesn't exist.
     */
    APSDirectory getDirectory(String path) throws IOException;

    /**
     * Returns the file or folder of the specified path.
     *
     * @param path The path of the file.
     */
    APSFile getFile(String path);

    /**
     * Returns the root directory.
     */
    APSDirectory getRootDirectory();

}
