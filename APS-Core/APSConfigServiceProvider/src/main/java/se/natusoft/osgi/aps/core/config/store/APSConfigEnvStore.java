/*
 *
 * PROJECT
 *     Name
 *         APS Configuration Service Provider
 *     
 *     Code Version
 *         0.9.0
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
 *         2011-08-05: Created!
 *
 */
package se.natusoft.osgi.aps.core.config.store;

import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigEnvironment;
import se.natusoft.osgi.aps.api.core.config.service.APSConfigException;
import se.natusoft.osgi.aps.core.config.model.ConfigEnvironmentProvider;
import se.natusoft.osgi.aps.core.config.model.admin.APSConfigEnvironmentImpl;

import java.util.*;

/**
 * Manages the environments.
 */
public class APSConfigEnvStore implements ConfigEnvironmentProvider {
    //
    // Constants
    //

    /** Filename of configuration environments file. */
    public static final String ENVS_FILE = "environments";

    //
    // Private Members
    //

    /** The APSFileTool instance to use for reading and writing environment data. */
    private APSFileTool fileTool;

    /** The available enviornments */
    private List<APSConfigEnvironment> environments = new ArrayList<APSConfigEnvironment>();

    /** Holds the environments by name. */
    private Map<String, APSConfigEnvironment> environmentsByName = new HashMap<String, APSConfigEnvironment>();

    /** The current active configuration environment. */
    private APSConfigEnvironment activeConfigEnvironment = null;

    //
    // Constructors
    //

    /**
     * Creates a new APSConfigEnvStore instance.
     *
     * @param fileTool Used for reading and writing enviornment data to/from disk.
     *
     * @throws se.natusoft.osgi.aps.api.core.config.service.APSConfigException on failure to read enviornment configuration.
     */
    public APSConfigEnvStore(APSFileTool fileTool) throws APSConfigException {
        this.fileTool = fileTool;

        if (this.fileTool.fileExists(ENVS_FILE)) {
            Properties envProps = this.fileTool.loadProperties(ENVS_FILE);

            int noEnvs = Integer.valueOf(envProps.getProperty("envs"));
            for (int i = 0; i < noEnvs; i++) {
                String name = envProps.getProperty("name_" + i);
                String desc = envProps.getProperty("desc_" + i);
                APSConfigEnvironmentImpl configEnv = new APSConfigEnvironmentImpl(name, desc);
                addEnvironment(configEnv);
            }
            String activeIdProp = envProps.getProperty("active");
            if (activeIdProp != null) {
                int activeid = -1;
                try {
                    activeid = Integer.valueOf(activeIdProp);
                }
                catch (NumberFormatException nfe) { /* OK */ }
                if (activeid >= 0 && activeid < this.environments.size()) {
                    this.activeConfigEnvironment = this.environments.get(activeid);
                }
            }
        }

        if (this.activeConfigEnvironment == null) {
            if (this.environments.size() == 0) {
                APSConfigEnvironment defaultEnv = new APSConfigEnvironmentImpl("default", "This is created when env is asked for and none have been created!");
                addEnvironment(defaultEnv);
            }
            setActiveConfigEnvironment(this.environments.get(0));
        }
    }

    //
    // Methods
    //

    /**
     * Saves the environments.
     *
     * @throws se.natusoft.osgi.aps.api.core.config.service.APSConfigException on failure to save environment configuration.
     */
    private void saveConfigEnvironments() throws APSConfigException {
        Properties props = new Properties();
        int envSize = this.environments.size();
        props.setProperty("envs", "" + envSize);
        int activeid = -1;

        for (int i = 0; i < envSize; i++) {
            APSConfigEnvironment ce = this.environments.get(i);
            props.setProperty("name_" + i, ce.getName());
            props.setProperty("desc_" + i, ce.getDescription());

            if (this.activeConfigEnvironment != null && ce.equals(this.activeConfigEnvironment)) {
                activeid = i;
            }
        }
        if (this.environments.size() > 0 && activeid >= 0) {
            props.setProperty("active", "" + activeid);
        }

        this.fileTool.saveProperties("environments", props);
    }

    /**
     * @return The available environments.
     */
    public List<APSConfigEnvironment> getConfigEnvironments() {
        return this.environments;
    }

    /**
     * Returns an environment by its name.
     *
     * @param name The name of the environment to get.
     *
     * @return The named environment or null if it does not exist.
     */
    public APSConfigEnvironment getConfigEnvironmentByName(String name) {
        return this.environmentsByName.get(name);
    }

    /**
     * Sets the active configuration environment.
     *
     * @param configEnvironment The configuration environment to set.
     *
     * @throws APSConfigException on failure to set active config environment. Most likely due to save failure.
     */
    public void setActiveConfigEnvironment(APSConfigEnvironment configEnvironment) throws APSConfigException {
        this.activeConfigEnvironment = configEnvironment;
        saveConfigEnvironments();
    }

    /**
     * @return The active configuration environment or null if none have been set.
     */
    public APSConfigEnvironment getActiveConfigEnvironment() {
        if (this.activeConfigEnvironment == null) {
            if (!this.environments.isEmpty()) {
                this.activeConfigEnvironment = this.environments.get(0);
            }
            else {
                this.activeConfigEnvironment = new APSConfigEnvironmentImpl("default", "Default (created when asked for and none available!)");
                this.environments.add(this.activeConfigEnvironment);
            }
        }

        return this.activeConfigEnvironment;
    }

    /**
     * Adds an environment.
     *
     * @param environment The environment to add.
     *
     * @throws se.natusoft.osgi.aps.api.core.config.service.APSConfigException on failure to add environment.
     */
    public void addEnvironment(APSConfigEnvironment environment) throws APSConfigException {
        if (!this.environments.contains(environment)) {
            this.environments.add(environment);
            saveConfigEnvironments();
        }
        this.environmentsByName.put(environment.getName(), environment);
    }

    /**
     * Removes an environement.
     *
     * @param environment The environment to remove.
     *
     * @throws se.natusoft.osgi.aps.api.core.config.service.APSConfigException on failure to remove environment.
     */
    public void removeEnvironment(APSConfigEnvironment environment) throws APSConfigException {
        if (this.activeConfigEnvironment != null && this.activeConfigEnvironment.equals(environment)) {
            if (this.environments.size() > 0) {
                this.activeConfigEnvironment = this.environments.get(0);
            }
            else {
                this.activeConfigEnvironment = null;
            }
        }
        this.environments.remove(environment);
        saveConfigEnvironments();

        this.environmentsByName.remove(environment.getName());
    }

    /**
     * Removes all environmetns.
     *
     * @throws se.natusoft.osgi.aps.api.core.config.service.APSConfigException on failure to do so.
     */
    public void removeAllEnvironments() throws APSConfigException {
        this.environments = new ArrayList<APSConfigEnvironment>();
        this.activeConfigEnvironment = null;
        saveConfigEnvironments();
        this.environmentsByName = new HashMap<String, APSConfigEnvironment>();
    }
}
