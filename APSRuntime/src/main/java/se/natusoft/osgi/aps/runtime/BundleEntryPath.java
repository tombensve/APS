/*
 *
 * PROJECT
 *     Name
 *         APS OSGi Test Tools
 *
 *     Code Version
 *         1.0.0
 *
 *     Description
 *         Provides tools for testing OSGi services.
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
 *         2018-05-25: Created!
 *
 */
package se.natusoft.osgi.aps.runtime;

import java.util.jar.JarFile;

/**
 * Since test bundles can be loaded from both filesystem and jar files, this holds all variants
 * of path and file info to handle both cases.
 */
public class BundleEntryPath {

    private String fullPath;
    private String relativePath;
    private JarFile jarFile;

    /**
     * This constructor is useful for a local filesystem path where we keep both full path to
     * file in filesystem and bundle relative path.
     *
     * @param fullPath The full filesystem path.
     * @param relativePath The bundle relative path.
     */
    public BundleEntryPath( String fullPath, String relativePath) {
        this.fullPath = fullPath;
        this.relativePath = relativePath;
    }

    /**
     * This only provides a bundle relative path. OK to use for providing local classpath entries.
     * Use this with care and only if you are 100% sure what you are doing! Can have side effects.
     *
     * @param path The bundle relative path.
     */
    public BundleEntryPath( String path) {
        this.relativePath = path;
    }

    /**
     * This provides a path in a jar file.
     *
     * @param jarFile The actual loaded JarFile to extract content from.
     * @param relativePath The relative path within the jar of the entry.
     */
    public BundleEntryPath( JarFile jarFile, String relativePath) {
        this.jarFile = jarFile;
        this.relativePath = relativePath;
    }

    /**
     * @return The full filesystem path of the entry.
     */
    public String getFullPath() {
        return this.fullPath;
    }

    /**
     * @return The bundle relative path of the entry.
     */
    public String getRelativePath() {
        return this.relativePath;
    }

    /**
     * @return A JarFile entry.
     */
    public JarFile getJarFile() {
        return this.jarFile;
    }

    /**
     * @return String representation which is the relative path.
     */
    public String toString() {
        return getRelativePath();
    }
}
