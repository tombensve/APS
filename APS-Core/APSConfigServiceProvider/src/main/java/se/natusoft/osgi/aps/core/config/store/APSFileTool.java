/*
 *
 * PROJECT
 *     Name
 *         APS Configuration Service Provider
 *     
 *     Code Version
 *         0.9.1
 *     
 *     Description
 *         A more advanced configuration service that uses annotated interfaces to
 *         describe and provide access to configuration. It supports structured
 *         configuration models.
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
 *         2011-08-11: Created!
 *
 */
package se.natusoft.osgi.aps.core.config.store;

import se.natusoft.osgi.aps.api.core.config.service.APSConfigException;
import se.natusoft.osgi.aps.api.core.filesystem.model.APSFile;
import se.natusoft.osgi.aps.api.core.filesystem.model.APSFilesystem;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Provides tools for loading and saving lists and properties.
 */
public class APSFileTool {
    //
    // Private Members
    //

    /** Our filesystem. */
    private APSFilesystem fs;

    //
    // Constructors
    //

    /**
     * Creates a new APSFileTool instance.
     *
     * @param fs The filesystem to read and write to.
     */
    public APSFileTool(APSFilesystem fs) {
        this.fs = fs;
    }

    //
    // Methods
    //

    /**
     * Tests for file existence.
     *
     * @param name The name to test.
     *
     * @return true if the specified file name exists.
     */
    public boolean fileExists(String name) {
        boolean result = this.fs.getRootDirectory().getFile(name).exists();

        if (!result) {
            result = this.fs.getRootDirectory().getFile(name + ".properties").exists();
        }

        if (!result) {
            result = this.fs.getRootDirectory().getFile(name + ".list").exists();
        }

        return result;
    }

    /**
     * Saves the specified list to the specified file name.
     *
     * @param name The name of the saved list.
     * @param list The list to save.
     *
     * @throws se.natusoft.osgi.aps.api.core.config.service.APSConfigException When things go bad.
     */
    public void saveList(String name, List<String> list) throws APSConfigException {
        BufferedWriter listWriter = null;
        try {
            listWriter = new BufferedWriter(new OutputStreamWriter(this.fs.getRootDirectory().createFile(name + ".list").createOutputStream()));
            for (String item : list) {
                listWriter.write(item);
                listWriter.newLine();
            }
            listWriter.flush();
        }
        catch (IOException ioe) {
            throw new APSConfigException("Failed to save list '" + name + "'");
        }
        finally {
            if (listWriter != null) {
                try {
                    listWriter.close();
                } catch (IOException cioe) {/* OK */}
            }
        }
    }

    /**
     * Reads the specified list file and returns it.
     *
     * @param name The name of the list file to read.
     *
     * @throws se.natusoft.osgi.aps.api.core.config.service.APSConfigException When shit happens.
     */
    public List<String> loadList(String name) throws APSConfigException {
        BufferedReader listReader = null;
        List<String> list = new ArrayList<String>();
        try {
            listReader = new BufferedReader(new InputStreamReader(this.fs.getRootDirectory().getFile(name + ".list").createInputStream()));
            String line;
            while ((line = listReader.readLine()) != null) {
                list.add(line);
            }
        }
        catch (IOException ioe) {
            throw new APSConfigException("Failed to read list '" + name + "!", ioe);
        }
        finally {
            if (listReader != null) {
                try {
                    listReader.close();
                }
                catch (IOException cioe) {
                    // Hmm, no I dont really care here!
                }
            }
        }

        return list;
    }

    /**
     * Saves the specified properties to the specified file name.
     *
     * @param name The name of the properties file to save to.
     * @param properties The properties to save.
     *
     * @throws se.natusoft.osgi.aps.api.core.config.service.APSConfigException When things go bad.
     */
    public void saveProperties(String name, Properties properties) throws APSConfigException {
        BufferedOutputStream propStream = null;
        try {
            propStream = new BufferedOutputStream(this.fs.getRootDirectory().getFile(name + ".properties").createOutputStream());
            properties.store(propStream, "");
            propStream.flush();
        }
        catch (IOException ioe) {
            throw new APSConfigException("Failed to save properties '" + name + "'!", ioe);
        }
        finally {
            if (propStream != null) {
                try {
                    propStream.close();
                }
                catch (IOException cioe) {/*OK*/}
            }
        }
    }

    /**
     * Loads the properties with the specified file name.
     *
     * @param name The name of the properties file to load.
     *
     * @throws se.natusoft.osgi.aps.api.core.config.service.APSConfigException When shit happens.
     */
    public Properties loadProperties(String name) throws APSConfigException {
        BufferedInputStream propStream = null;
        Properties props = new Properties();
        try {
            propStream = new BufferedInputStream(this.fs.getRootDirectory().getFile(name + ".properties").createInputStream());
            props.load(propStream);
        }
        catch (IOException ioe) {
            if (propStream != null) {
                try {
                    propStream.close();
                }
                catch (IOException cioe) {/* OK */}
            }
        }

        return props;
    }

    /**
     * Returns a list of file names that matches beginning and end name criteria.
     *
     * @param begNameFilter The file name have to start with this.
     * @param endNameFilter The end of the name have to match this.
     *
     * @return A List of the matching names minus the end name filter. In other words, the end name filter
     *         should be the file extension and it will be removed from the returned file name. Note that the
     *         load and save methods above provides an automatic extension per type.
     */
    public List<String> getFileList(String begNameFilter, String endNameFilter) {
        List<String> fileList = new ArrayList<String>();

        for (APSFile file : this.fs.getRootDirectory().listFiles()) {
            if (file.isFile() && file.getName().startsWith(begNameFilter) && file.getName().endsWith(endNameFilter)) {
                fileList.add(file.getName().substring(0, file.getName().length() - endNameFilter.length()));
            }
        }

        return fileList;
    }

    /**
     * Removes the file with the specified name.
     *
     * @param name the name of the file to remove.
     */
    public void removeFile(String name) {
        this.fs.getRootDirectory().getFile(name).delete();
    }
}
