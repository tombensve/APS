/*
 *
 * PROJECT
 *     Name
 *         APS Configuration Service Provider
 *     
 *     Code Version
 *         0.11.0
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
    private APSFileTool fileTool = null;

    /** The available enviornments */
    private List<APSConfigEnvironment> environments = null;

    /** Holds the environments by name. */
    private Map<String, APSConfigEnvironment> environmentsByName = null;

    /** The current active configuration environment. */
    private APSConfigEnvironment activeConfigEnvironment = null;

    /** The timestamp of last update. */
    private long activeConfigEnvironmentTimestamp = 0;

    /** The registered listeners for updates to config env store. */
    private List<ConfigEnvUpdateListener> updateListeners = new LinkedList<>();

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
            setFromProperties(envProps);
        }
        else {
            Properties envProps = new Properties();
            envProps.setProperty("envs", "0");
            setFromProperties(envProps);
        }

    }

    //
    // Methods
    //

    /**
     * Provides a complete config env store as a java.util.Properties.
     *
     * This will reset any earlier stored values.
     *
     * @param envProps The config env properties to provide.
     */
    public void setFromProperties(Properties envProps) {
        this.environments = new LinkedList<>();
        this.environmentsByName = new HashMap<>();
        this.activeConfigEnvironment = null;

        int noEnvs = Integer.valueOf(envProps.getProperty("envs"));
        for (int i = 0; i < noEnvs; i++) {
            String name = envProps.getProperty("name_" + i);
            String desc = envProps.getProperty("desc_" + i);
            String ts = envProps.getProperty("time_" + i);
            if (name != null && name.trim().length() > 0) {
                APSConfigEnvironmentImpl configEnv = new APSConfigEnvironmentImpl(name, desc, ts != null ? Long.valueOf(ts) : 0);
                if (!this.environments.contains(configEnv)) {
                    this.environments.add(configEnv);
                }
            }
        }
        String activeIdProp = envProps.getProperty("active");
        if (activeIdProp != null) {
            int activeid = -1;
            try {
                activeid = Integer.valueOf(activeIdProp);
            }
            catch (NumberFormatException nfe) { /* OK */ }
            if (activeid >= 0 && activeid < noEnvs) {
                String activeName = envProps.getProperty("name_" + activeid);
                if (activeName == null || activeName.trim().length() == 0) {
                    throw new IllegalStateException("BUG: The config environment specified as active (" + activeid + ") has no name!");
                }
                for (APSConfigEnvironment configEnvironment : this.environments) {
                    if (activeName.equals(configEnvironment.getName())) {
                        this.activeConfigEnvironment = configEnvironment;
                        break;
                    }
                }

                try {
                    this.activeConfigEnvironmentTimestamp = Long.valueOf(envProps.getProperty("active_time")).longValue();
                }
                catch (NumberFormatException nfe2) {}
            }
        }

        if (this.activeConfigEnvironment == null) {
            if (this.environments.size() == 0) {
                APSConfigEnvironment defaultEnv =
                        new APSConfigEnvironmentImpl("default", "This is created when env is asked for and none have been created!", 0);
                if (!this.environments.contains(defaultEnv)) {
                    this.environments.add(defaultEnv);
                }
            }
            this.activeConfigEnvironment = this.environments.get(0);
            this.activeConfigEnvironmentTimestamp = new Date().getTime();
        }
    }

    /**
     * Returns the stored config environments as a java.util.Properties.
     */
    public Properties getAsProperties() {
        Properties envProps = new Properties();
        int envSize = this.environments.size();
        envProps.setProperty("envs", "" + envSize);
        int activeid = -1;

        for (int i = 0; i < envSize; i++) {
            APSConfigEnvironment ce = this.environments.get(i);
            envProps.setProperty("name_" + i, ce.getName());
            envProps.setProperty("desc_" + i, ce.getDescription() != null ? ce.getDescription() : "");
            envProps.setProperty("time_" + i, "" + ce.getTimestamp());

            if (this.activeConfigEnvironment != null && ce.equals(this.activeConfigEnvironment)) {
                activeid = i;
            }
        }
        if (this.environments.size() > 0 && activeid >= 0) {
            envProps.setProperty("active", "" + activeid);
            envProps.setProperty("time_active", "" + this.activeConfigEnvironmentTimestamp);
        }

        return envProps;
    }

    /**
     * Saves the environments.
     *
     * @throws se.natusoft.osgi.aps.api.core.config.service.APSConfigException on failure to save environment configuration.
     */
    public void saveConfigEnvironments() throws APSConfigException {
        this.fileTool.saveProperties("environments", getAsProperties());
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
        for (APSConfigEnvironment cenv : this.environments) {
            if (configEnvironment.getName().equals(cenv.getName())) {
                this.activeConfigEnvironment = cenv;
                this.activeConfigEnvironmentTimestamp = new Date().getTime();
                break;
            }
        }
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
                this.activeConfigEnvironment = new APSConfigEnvironmentImpl("default", "Default (created when asked for and none available!)", 0);
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
        addOrUpdateEnvironment(environment, true);
    }

    /**
     * Adds (or updates) an environment.
     *
     * @param environment The environment to add.
     * @param fireEvent Will only fire change event if this is true.
     *
     * @throws se.natusoft.osgi.aps.api.core.config.service.APSConfigException on failure to add environment.
     */
    private void addOrUpdateEnvironment(APSConfigEnvironment environment, boolean fireEvent) throws APSConfigException {
        APSConfigEnvironment ce = new APSConfigEnvironmentImpl(environment.getName(), environment.getDescription(), new Date().getTime());
        if (!this.environments.contains(environment)) {
            this.environments.add(ce);
        }
        else {
            this.environments.remove(environment);
            this.environments.add(ce);
        }
        this.environmentsByName.put(environment.getName(), ce);
        saveConfigEnvironments();

        if (fireEvent) fireUpdateEvents();
    }

    /**
     * Removes an environment.
     *
     * @param environment The environment to remove.
     *
     * @throws se.natusoft.osgi.aps.api.core.config.service.APSConfigException on failure to remove environment.
     */
    public void removeEnvironment(APSConfigEnvironment environment) throws APSConfigException {
        this.environments.remove(environment);
        if (this.activeConfigEnvironment != null && this.activeConfigEnvironment.equals(environment)) {
            if (this.environments.size() > 0) {
                this.activeConfigEnvironment = this.environments.get(0);
            }
            else {
                this.activeConfigEnvironment = null;
            }
        }
        saveConfigEnvironments();

        this.environmentsByName.remove(environment.getName());

        fireUpdateEvents();
    }

    /**
     * Removes all environments.
     *
     * @throws se.natusoft.osgi.aps.api.core.config.service.APSConfigException on failure to do so.
     */
    public void removeAllEnvironments() throws APSConfigException {
        this.environments = new ArrayList<>();
        this.activeConfigEnvironment = null;
        saveConfigEnvironments();
        this.environmentsByName = new HashMap<>();
        fireUpdateEvents();
    }

    /**
     * Adds an update listener.
     *
     * @param listener The listener to add.
     */
    public void addUpdateListener(ConfigEnvUpdateListener listener) {
        this.updateListeners.add(listener);
    }

    /**
     * Removes an update listener.
     *
     * @param listener The listener to remove.
     */
    public void removeUpdateListener(ConfigEnvUpdateListener listener) {
        this.updateListeners.remove(listener);
    }

    /**
     * Fires an update event to all listeners.
     */
    private void fireUpdateEvents() {
        for (ConfigEnvUpdateListener listener : this.updateListeners) {
            listener.updated(this);
        }
    }

    //
    // Inner Classes
    //

    /**
     * Classes that want to be notified of a config update to the memory store implements
     * this.
     */
    public interface ConfigEnvUpdateListener {

        /**
         * Receives updated configuration.
         *
         * @param configEnvStore The config env store holding the updated value(s).
         */
        public void updated(APSConfigEnvStore configEnvStore);
    }
}
