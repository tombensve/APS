/* 
 * 
 * PROJECT
 *     Name
 *         APS Filesystem Service Provider
 *     
 *     Code Version
 *         0.9.2
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
package se.natusoft.osgi.aps.core.filesystem.model;

import se.natusoft.osgi.aps.api.core.filesystem.model.APSFile;

import java.io.*;
import java.util.Properties;

/**
 * This represents a file in an APSFilesystemService provided filesystem.
 * <p>
 * Use the createInputStream/OutputStream/Reader/Writer to read and write
 * the file.
 */
public class APSFileImpl /*extends File*/ implements APSFile {
    //
    // Private Members
    //

    /** The filesystem this backingFile belongs to. */
    protected APSFilesystemImpl fs = null;
    
    /** The filesystem root relative path. */
    protected String relPath;
    
    /** The backing java.io.File. */
    protected File backingFile;

    //
    // Constructors
    //
    
    /**
     * Creates a new APSFileImpl instance from the specified filesystem root relative path.
     * 
     * @param fs The APSFilesystemImpl that created this APSFileImpl.
     * @param path The filesystem relative path of the backingFile.
     */
    APSFileImpl(APSFilesystemImpl fs, String path) {
        this.backingFile = new File(fs.toFullSysPath(path));
        this.fs = fs;
        this.relPath = path;
        if (this.relPath == null) {
            this.relPath = "";
        }
    }

    /**
     * Creates a new APSFileImpl instance.
     *
     * @param fs The APSFilesystemImpl this APSFileImpl belongs to.
     * @param parent The parent if this APSFileImpl.
     * @param name The name of this APSFileImpl.
     */
    APSFileImpl(APSFilesystemImpl fs, APSFileImpl parent, String name) {
        this.backingFile = new File(fs.toFullSysPath(parent.getChildPath(name)));
        this.fs = fs;
        this.relPath = parent.getChildPath(name);
    }

    /**
     * Creates a new APSFileImpl instance.
     * 
     * @param copy The APSFileImpl to copy from.
     */
    APSFileImpl(APSFileImpl copy) {
        this.backingFile = new File(copy.fs.toFullSysPath(copy.relPath));
        this.fs = copy.fs;
        this.relPath = copy.relPath;
    }

    //
    // Methods
    //
    
    /** 
     * Returns the backing backingFile. 
     */
    /*package*/ File getBackingFile() {
        return this.backingFile;
    }
    
    /**
     * If this APSFileImpl represents a directory an APSDirectoryImpl instance will be returned.
     * Otherwise null will be returned.
     */
    @Override
    public APSDirectoryImpl toDirectory() {
        if (this.backingFile.isDirectory()) {
            return new APSDirectoryImpl(this);
        }
        return null;
    }

    /**
     * Returns the path of the specified child.
     * 
     * @param name The name of the child to get the path for.
     */
    private String getChildPath(String name) {
        String childPath = getPath();
        if (childPath == null) {
            childPath = "";
        }
        if (childPath.trim().length() != 0) {
            childPath += File.separator;
        }
        childPath += name;

        return childPath;
    }

    /**
     * Creates a new InputStream to this backingFile.
     * 
     * @throws IOException 
     */
    @Override
    public InputStream createInputStream() throws IOException {
        return new FileInputStream(this.backingFile);
    }

    /**
     * Creates a new OutputStream to this backingFile.
     *
     * @throws IOException 
     */
    @Override
    public OutputStream createOutputStream() throws IOException {
        return new FileOutputStream(this.backingFile);
    }

    /**
     * Creates a new Reader to this backingFile.
     *
     * @throws IOException 
     */
    @Override
    public Reader createReader() throws IOException {
        return new InputStreamReader(createInputStream());
    }

    /**
     * Creates a new Writer to this backingFile.
     * 
     * @throws IOException 
     */
    @Override
    public Writer createWriter() throws IOException {
        return new OutputStreamWriter(createOutputStream());
    }

    /**
     * If this backingFile denotes a properties backingFile it is loaded and returned.
     * 
     * @throws IOException on failure or if it is not a properties backingFile.
     */
    @Override
    public Properties loadProperties() throws IOException {
        if (!this.backingFile.getName().endsWith(".properties")) {
            throw new IOException("File '" + getAbsolutePath() + "' is not a properties file!");
        }
        Properties props = new Properties();
        InputStream readStream = createInputStream();
        try {
            props.load(readStream);
        }
        finally {
            readStream.close();
        }
        
        return props;
    }
    
    /**
     * If this backingFile denotes a properties backingFile it is written with the specified properties.
     * 
     * @param properties The properties to save.
     * 
     * @throws IOException on failure or if it is not a properties backingFile.
     */
    @Override
    public void saveProperties(Properties properties) throws IOException {
        if (!this.backingFile.getName().endsWith(".properties")) {
            throw new IOException("File '" + getAbsolutePath() + "' is not a properties file!");
        }
        OutputStream writeStream = createOutputStream();
        try {
            properties.store(writeStream, "");
        }
        finally {
            writeStream.close();
        }
    }
    
    /** 
     * @see java.io.File#getPath() 
     */
    @Override
    public String getPath() {
        return this.relPath;
    }

    /**
     * @see java.io.File#getParent()
     */
    @Override
    public String getParent() {
        if (this.fs.isFSRoot(this.backingFile.getPath())) {
            return null;
        }
        return this.fs.toFSRelativePath(this.backingFile.getParent());
    }

    /**
     * @see java.io.File#getParentFile()
     */
    @Override
    public APSDirectoryImpl getParentFile() {
        String parent = this.getParent();

        if (parent == null) {
            return null;
        }

        return new APSDirectoryImpl(this.fs, parent);
    }

    /**
     * @see java.io.File#getAbsoluteFile() 
     */
    @Override
    public String getAbsolutePath() {
        return this.fs.toFSRelativePath(this.backingFile.getAbsolutePath());
    }

    /**
     * @see java.io.File#getAbsoluteFile() 
     */
    @Override
    public APSFileImpl getAbsoluteFile() {
        File path = this.backingFile.getAbsoluteFile();
        APSFileImpl file = new APSFileImpl(this.fs, path.getPath());
        if (file.backingFile.isDirectory()) {
            file = file.toDirectory();
        }
        return file;
    }

    /**
     * @see java.io.File#getCanonicalPath() 
     */
    @Override
    public String getCanonicalPath() throws IOException {
        return this.fs.toFSRelativePath(this.backingFile.getCanonicalPath());
    }

    /**
     * @see java.io.File#getCanonicalFile() 
     */
    @Override
    public APSFileImpl getCanonicalFile() throws IOException {
        File canonFile = this.backingFile.getCanonicalFile();
        APSFileImpl file = new APSFileImpl(fs, canonFile.getPath());
        if (file.backingFile.isDirectory()) {
            file = file.toDirectory();
        }

        return file;
    }

    /**
     * @see java.io.File#renameTo(java.io.File)
     */
    @Override
    public boolean renameTo(APSFile dest) {
        return this.backingFile.renameTo(((APSFileImpl)dest).backingFile);
    }

    /**
     * @see java.io.File#getName()
     */
    @Override
    public String getName() {
        return this.backingFile.getName();
    }

    /**
     * @see java.io.File#canRead()
     */
    @Override
    public boolean canRead() {
        return this.backingFile.canRead();
    }

    /**
     * @see java.io.File#canWrite()
     */
    @Override
    public boolean canWrite() {
        return this.backingFile.canWrite();
    }

    /**
     * @see java.io.File#exists()
     */
    @Override
    public boolean exists() {
        return this.backingFile.exists();
    }

    /**
     * @see java.io.File#isDirectory()
     */
    @Override
    public boolean isDirectory() {
        return this.backingFile.isDirectory();
    }

    /**
     * @see java.io.File#isFile()
     */
    @Override
    public boolean isFile() {
        return this.backingFile.isFile();
    }

    /**
     * @see java.io.File#isHidden()
     */
    @Override
    public boolean isHidden() {
        return this.backingFile.isHidden();
    }

    /**
     * @see java.io.File#lastModified()
     */
    @Override
    public long lastModified() {
        return this.backingFile.lastModified();
    }

    /**
     * @see java.io.File#length()
     */
    @Override
    public long length() {
        return this.backingFile.length();
    }

    /**
     * @see java.io.File#createNewFile()
     */
    @Override
    public boolean createNewFile() throws IOException {
        return this.backingFile.createNewFile();
    }

    /**
     * @see java.io.File#delete()
     */
    @Override
    public boolean delete() {
        return this.backingFile.delete();
    }

    /**
     * @see java.io.File#deleteOnExit()
     */
    @Override
    public void deleteOnExit() {
        this.backingFile.deleteOnExit();
    }

    /**
     * Returns a string representation of this APSFileImpl.
     */
    @Override
    public String toString() {
        return this.fs.toDisplayPath(this.backingFile.getPath());
    }
    
}
