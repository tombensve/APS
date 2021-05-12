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
 *         2011-06-04: Created!
 *
 */
package se.natusoft.aps.api.core.filesystem.service;

import se.natusoft.aps.api.core.filesystem.model.APSFilesystem;
import se.natusoft.aps.exceptions.APSIOException;
import se.natusoft.aps.types.APSHandler;
import se.natusoft.aps.types.APSResult;

/**
 * This provides a filesystem for use by services/applications. Each filesystem has its own root that cannot be navigated
 * outside of.
 *
 * Services or application using this should do something like this in their activators:
 *
 *     APSFilesystemService fss;
 *     APSFilesystem fs;
 *
 *     fss.getFilesystem("my.file.system", (result) -> {
 *         if (result.success()) {
 *             fs = result.result();
 *         }
 *     });
 *
 */
public interface APSFilesystemService {

    //
    // Constants
    //

    /** The configuration key of the filesystem root catalog. */
    String CONF_APS_FILESYSTEM_ROOT = "aps.filesystem.root";

    //
    // Methods
    //

    /**
     * Returns the filesystem for the specified owner. If the filesystem does not exist it is created.
     *
     * @param owner The owner of the filesystem or rather a unique identifier of it.
     * @param handler Called with the filesystem.
     *
     * @exception APSIOException on failure.
     */
    void getFilesystem( String owner, APSHandler<APSResult<APSFilesystem>> handler);

    /**
     * Removes the filesystem and all files in it.
     *
     * @param owner The owner of the filesystem to delete.
     *
     * @exception APSIOException on any failure.
     */
    void deleteFilesystem(String owner, APSHandler<APSResult<Void>> handler);
}
