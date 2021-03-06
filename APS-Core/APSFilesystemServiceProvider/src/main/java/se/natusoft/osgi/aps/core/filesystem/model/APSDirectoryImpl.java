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
 *     Tommy Svensson (tommy@natusoft.se)
 *         Changes:
 *         2011-06-05: Created!
 *
 */
package se.natusoft.osgi.aps.core.filesystem.model;

import se.natusoft.osgi.aps.api.core.filesystem.model.APSDirectory;
import se.natusoft.osgi.aps.api.core.filesystem.model.APSFile;

import java.io.*;

/**
 * This represents a directory in an APSFilesystem.
 * <p>
 * Use this to create or get directories and files and list contents of directories.
 * <p>
 * Personal comment: I do prefer the term "folder" over "directory" since I think that is
 * less ambigous, but since Java uses the term "directory" I decided to stick with that name.
 */
public class APSDirectoryImpl extends APSFileImpl implements APSDirectory {

    /**
     * Creates a new APSDirectoryImpl instance from the specified filesystem root relative path.
     *
     * @param fs The APSFilesystemImpl that created this APSFileImpl.
     * @param path The filesystem relative path of the backingFile.
     */
    APSDirectoryImpl(APSFilesystemImpl fs, String path) {
        super(fs, path);
    }

    /**
     * Creates a new APSDirectoryImpl instance.
     *
     * @param fs The APSFilesystemImpl this APSFileImpl belongs to.
     * @param parent The parent if this APSFileImpl.
     * @param name The name of this APSFileImpl.
     */
    APSDirectoryImpl(APSFilesystemImpl fs, APSFileImpl parent, String name) {
        super(fs, parent, name);
    }

    /**
     * Creates a new APSDirectoryImpl instance from an APSFileImpl instance.
     *
     * @param copy The APSFileImpl to copy.
     */
    APSDirectoryImpl(APSFileImpl copy) {
        super(copy);
    }

    /**
     * Performs a recursive delete of the directory represented by this APSDirectoryImpl
     * and all subdirectories and files.
     *
     * @throws IOException on any failure.
     */
    @Override
    public void recursiveDelete() throws IOException {
        APSFile[] files = listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                ((APSDirectoryImpl)files[i]).recursiveDelete();
            } else {
                files[i].delete();
            }
        }
        delete();
    }

    /**
     * Creates a new APSFileImpl using a path relative to this backingFile.
     *
     * @param path APSFileImpl (of type directory) relative path for new APSFileImpl.
     */
    private APSFile newAPSFile(String path) {
        String newPath = "";
        if (this.relPath.trim().length() != 0) {
            newPath = this.relPath + File.separator;
        }
        newPath = newPath + path;
        return new APSFileImpl(this.fs, newPath);
    }

    /**
     * Creates a new APSDirectoryImpl using a path relative to this APSDirectory.
     *
     * @param path A relative path for the new APSDirectoryImpl.
     */
    private APSDirectory newAPSDirectory(String path) {
        String newPath = "";
        if (this.relPath.trim().length() != 0) {
            newPath = this.relPath + File.separator;
        }
        newPath = newPath + path;
        return new APSDirectoryImpl(this.fs, newPath);
    }


    /**
     * Returns a newly created directory with the specified name.
     *
     * @param name The name of the directory to create.
     *
     * @throws IOException on any failurel.
     */
    @Override
    public APSDirectory createDir(String name) throws IOException {
        return createDir(name, null);
    }

    /**
     * Returns a newly created directory with the specified name.
     *
     * @param name The name of the directory to create.
     * @param duplicateMessage The exception message if directory already exists.
     *
     * @throws IOException on any failurel.
     */
    @Override
    public APSDirectory createDir(String name, String duplicateMessage) throws IOException {
        APSDirectoryImpl dir = (APSDirectoryImpl)newAPSDirectory(name);
        if (dir.exists()) {
            if (duplicateMessage == null) {
                duplicateMessage = "Path of directory to create already exists!";
            }
            throw new IOException(duplicateMessage);
        }
        if (!dir.backingFile.mkdirs()) {
            throw new IOException("Failed to create directory!");
        }
        return dir;
    }

    /**
     * Returns the specified directory.
     *
     * @param dirname The name of the directory to enter.
     *
     * @throws FileNotFoundException
     */
    @Override
    public APSDirectory getDir(String dirname) throws FileNotFoundException {
        APSDirectory dir = newAPSDirectory(dirname);
        if (!dir.exists()) {
            throw new FileNotFoundException("Dir not found: " + toString());
        }
        return dir;
    }

    /**
     * Creates a new backingFile in the directory represented by the current APSFileImpl.
     *
     * @param name The name of the backingFile to create.
     *
     * @throws IOException on failure.
     */
    @Override
    public APSFile createFile(String name) throws IOException {
        APSFile file = newAPSFile(name);
        if (file.exists()) {
            throw new IOException("File '" + file + "' already exist!");
        }
        return file;
    }

//    /**
//     * Unzips an InputStream and returns its root as an APSDirectory.
//     *
//     * @param name          The root of the unzipped file tree.
//     * @param zipFile The zip file to unzip. This will be closed when all is unzipped!
//     * @return an APSDirectory pointing to name.
//     */
//    @Override
//    public APSDirectory unzip(String name, ZipFile zipFile) throws IOException {
//        APSDirectory unzipDir = createDir(name);
//
//        byte[] buffer = new byte[2000];
//
//        Enumeration entries = zipFile.entries();
//        while (entries.hasMoreElements()) {
//            ZipEntry entry = (ZipEntry)entries.nextElement();
//            File destFile = new File(unzipDir.toFile().getAbsolutePath(), entry.getName());
//
//            if (!entry.isDirectory()) {
//                InputStream src = zipFile.getInputStream(entry);
//                OutputStream dest = new FileOutputStream(destFile);
//                try {
//                    int read = src.read(buffer);
//                    while (read != -1) {
//                        dest.write(buffer, 0, read);
//                        read = src.read(buffer);
//                    }
//                }
//                finally {
//                    dest.close();
//                    src.close();
//                }
//            }
//            else {
//                destFile.mkdir();
//            }
//        }
//
//        zipFile.close();
//
//        return unzipDir;
//    }

    /**
     * Returns the named backingFile in this directory.
     *
     * @param name The name of the backingFile to get.
     */
    @Override
    public APSFile getFile(String name) {
        return new APSFileImpl(this.fs, this, name);
    }

    @Override
    public String[] list() {
        return this.backingFile.list();
    }

    /**
     * @see java.io.File#listFiles()
     */
    @Override
    public APSFile[] listFiles() {
        String[] files = this.backingFile.list();
        if (files == null) {
            return null;
        }

        APSFileImpl[] apsFiles = new APSFileImpl[files.length];
        for (int i = 0; i < files.length; i++) {
            apsFiles[i] = new APSFileImpl(this.fs, this, files[i]);
            if (apsFiles[i].backingFile.isDirectory()) {
                apsFiles[i] = (APSFileImpl)apsFiles[i].toDirectory();
            }
        }

        return apsFiles;
    }


}
