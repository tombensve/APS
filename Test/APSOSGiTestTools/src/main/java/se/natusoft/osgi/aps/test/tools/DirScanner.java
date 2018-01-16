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
package se.natusoft.osgi.aps.test.tools;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Scans a directory returning a list of File object for each file in the directory and subdirectories.
 */
public class DirScanner {
    //
    // Private Members
    //

    private List<String> entries = new LinkedList<>();
    private int rootDirLength = 0;

    //
    // Constructors
    //

    /**
     * Creates a new DirScanner instance and does the complete scan.
     *
     * @param root The directory to start scanning at.
     */
    public DirScanner(File root) {
        scanDir(root);
    }

    //
    // Methods
    //

    /**
     * Scans the provided root dir for all files recursively.
     *
     * @param dir The dir to scan.
     */
    private void scanDir(File dir) {
        if (dir == null) throw new IllegalArgumentException("dir cannot be null!");

        if (this.rootDirLength == 0) {
            this.rootDirLength = dir.getAbsolutePath().length();
        }

        //noinspection ConstantConditions
        for (File file : dir.listFiles()) {
            if (file.isFile()) {
                String path = file.getAbsolutePath().substring(this.rootDirLength);
                this.entries.add(path);
            }
            else if (file.isDirectory()) {
                scanDir(file);
            }
        }
    }

    /**
     * Returns the result of the scan. All file paths are relative to the root!
     */
    @SuppressWarnings("unused")
    public List<String> getEntries() {
        return this.entries;
    }

    /**
     * Returns the entries as a stream of strings containing root relative paths.
     */
    public Stream<String> stream() {
        // Yes, I've mostly copied this from java.util.zip.JarFile.stream()!
        return StreamSupport.stream(Spliterators.spliterator(
                this.entries.iterator(), this.entries.size(),
                Spliterator.ORDERED | Spliterator.DISTINCT |
                        Spliterator.IMMUTABLE | Spliterator.NONNULL), /* parallel */ false);
    }
}
