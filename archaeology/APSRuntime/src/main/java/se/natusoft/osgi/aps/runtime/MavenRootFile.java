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
 *         2017-02-19: Created!
 *
 */
package se.natusoft.osgi.aps.runtime;

import java.io.File;

/**
 * This finds the maven root of a multi-module build.
 *
 * Be warned that this assumes that this is called during a maven build from an executing test! Only then
 * is it guaranteed that new File("."); will be at a maven project root.
 */
class MavenRootFile {

    //
    // Private Members
    //

    /** The found root. */
    private File mavenRoot = null;

    //
    // Constructors
    //

    /**
     * Creates a new MavenRootFile.
     */
    MavenRootFile() {
        find();
    }

    //
    // Methods
    //

    /**
     * This does the actual root finding.
     */
    private void find() {
        File dir = new File(".").getAbsoluteFile();

        File check = new File(dir, "pom.xml");
        while (dir != null && check.exists()) {
            this.mavenRoot = dir;
            dir = dir.getParentFile();
            check = new File(dir, "pom.xml");
        }
    }

    /**
     * Returns the found root.
     */
    File getRoot() {
        return this.mavenRoot;
    }
}
