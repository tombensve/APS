/*
 *
 * PROJECT
 *     Name
 *         APS Configuration Service Provider
 *     
 *     Code Version
 *         1.0.0
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
 *         2011-08-13: Created!
 *
 */
package se.natusoft.osgi.aps.core.config.store;

import se.natusoft.osgi.aps.api.core.config.APSConfig;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigDescription;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigAdmin;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigEnvironment;
import se.natusoft.osgi.aps.api.core.config.service.APSConfigException;
import se.natusoft.osgi.aps.core.config.model.APSConfigInstanceMemoryStoreImpl;
import se.natusoft.osgi.aps.core.config.model.APSConfigObjectFactory;
import se.natusoft.osgi.aps.core.config.model.admin.APSConfigAdminImpl;
import se.natusoft.osgi.aps.core.config.model.admin.APSConfigEditModelImpl;
import se.natusoft.osgi.aps.tools.APSLogger;

import java.util.*;

/**
 * Handles reading and writing of configurations.
 */
public class APSConfigPersistentStore implements ConfigStoreInfo {
    //
    // Private Members
    //

    /** The logger to log to. */
    private APSLogger logger = null;

    /** Used for reading and writing properties files. */
    private APSFileTool fileTool = null;

    /** The environment store. */
    private APSConfigEnvStore envStore = null;

    /** Holds information about available configurations. */
    private Configurations configurations = null;

    //
    // Constructors
    //

    /**
     * Creates a new APSConfigPersistentStore instance.
     *
     * @param fileTool Used to reading and writing config files.
     * @param envStore Used to get currently active environment.
     * @param logger The logger to log to.
     *
     * @throws APSConfigException On failure to read config information.
     */
    public APSConfigPersistentStore(APSFileTool fileTool, APSConfigEnvStore envStore, APSLogger logger) throws APSConfigException {
        this.fileTool = fileTool;
        this.envStore = envStore;
        this.logger = logger;

        this.configurations = new Configurations();
    }

    //
    // Methods
    //

    /**
     * Constructs a configuration file name.
     *
     * @param configId The configuraiton id of the configuration whose name to construct.
     * @param version The version of the configuration whose name to construct.
     *
     * @return The constructed configuration file name.
     */
    private String getConfigName(String configId, String version) {
        String confName = "apsconfig";
        if (configId != null && version != null) {
            confName = confName + "-" + configId + "-" + version;
        }
        return confName;
    }

    /**
     * Returns the available configuration ids.
     */
    public Set<String> getAvailableConfigurationIds() {
        return this.configurations.getAvailableConfigurationIds();
    }

    /**
     * Returns the available versions for a specified configuration id.
     *
     * @param configId The configuration id to get available versions for.
     */
    public List<String> getAvailableVersions(String configId) {
        return this.configurations.getAvailableVersions(configId);
    }

    /**
     * Returns the configuration with the specified config id and version.
     *
     * @param configClass The configClass of the configuration to load.
     *
     * @throws APSConfigException On failure to get the configuration.
     */
    public APSConfigAdminImpl loadConfiguration(Class<? extends APSConfig> configClass) throws APSConfigException {
        return loadConfiguration(this.envStore.getActiveConfigEnvironment(), configClass);
    }

    /**
     * Returns the configuration with the specified config id and version.
     *
     * @param configEnv The configuraiton enviornment to load configuration for.
     * @param configClass The configClass of the configuration to load.
     *
     * @throws APSConfigException On failure to get the configuration.
     */
    public APSConfigAdminImpl loadConfiguration(APSConfigEnvironment configEnv, Class<? extends APSConfig> configClass) throws APSConfigException {
        APSConfigDescription configAnn = configClass.getAnnotation(APSConfigDescription.class);
        String configId= configAnn.configId();
        String version = configAnn.version();

        boolean gotPrevConfig = false;
        Properties confProps = null;
        if (this.fileTool.fileExists(getConfigName(configId, version))) {
            confProps = this.fileTool.loadProperties(getConfigName(configId, version));
        }
        else {
            if (configAnn.prevVersion().trim().length() > 0) {
                String prevVersion = configAnn.prevVersion();
                if (this.fileTool.fileExists(getConfigName(configId, prevVersion))) {
                    confProps = this.fileTool.loadProperties(getConfigName(configId, prevVersion));
                    gotPrevConfig = true;
                }

            }
        }
        if (confProps == null) {
            confProps = new Properties();
        }
        APSConfigInstanceMemoryStoreImpl configValueStore = new APSConfigInstanceMemoryStoreImpl(confProps);
        APSConfigObjectFactory configObjectFactory = new APSConfigObjectFactory(envStore, configValueStore);
        APSConfigEditModelImpl configModel = new APSConfigEditModelImpl(configClass, configObjectFactory);
        APSConfigAdminImpl configuration = new APSConfigAdminImpl(configModel, configValueStore);
        if (gotPrevConfig) {
            saveConfiguration(configuration);
        }

        return configuration;
    }

    /**
     * Persistently saves the specified configuration.
     *
     * @param configuration The configuraiton to save.
     *
     * @throws APSConfigException on failure to save the configuration.
     */
    public void saveConfiguration(APSConfigAdminImpl configuration) throws APSConfigException {
        this.fileTool.saveProperties(getConfigName(configuration.getConfigId(), configuration.getVersion()), configuration.getConfigInstanceMemoryStore().getProperties());
    }

    /**
     * Deletes a configuration.
     *
     * @param configuration The configuration to delete.
     *
     * @throws APSConfigException on failure to delete configuration.
     */
    public void deleteConfiguration(APSConfigAdmin configuration) throws APSConfigException {
        deleteConfiguration(configuration.getConfigId(), configuration.getVersion());
    }

    /**
     * Deletes a configuration.
     *
     * @param configId The id of the configuration to remove.
     * @param version The version of the configuration to remove.
     *
     * @throws APSConfigException on failure to delete configuration.
     */
    public void deleteConfiguration(String configId, String version) throws APSConfigException {
        this.fileTool.removeFile(getConfigName(configId, version));
    }

    //
    // Inner Classes
    //

    /**
     * Handles configurations for a specified configuration enviornment.
     */
    private class Configurations {
        //
        // Private Members
        //

        /** Holds information about available configurations. */
        private Map<String /*configId*/, List<String /*version*/>> configurations = new HashMap<String, List<String>>();

        //
        // Constructors
        //

        /**
         * Creates a new Configurations instance.
         */
        public Configurations() {
            for (String configFileName : APSConfigPersistentStore.this.fileTool.getFileList(getConfigName(null, null), ".properties")) {
                String[] nameParts = configFileName.split("-");
                if (nameParts.length == 3) {
                    String configId = nameParts[1];
                    String version = nameParts[2];
                    List<String> versions = this.configurations.get(configId);
                    if (versions == null) {
                        versions = new ArrayList<String>();
                    }
                    versions.add(version);
                    this.configurations.put(configId, versions);
                }
                else {
                    APSConfigPersistentStore.this.logger.error("Ignoring non valid config properties file: " + configFileName);
                }
            }
        }

        //
        // Methods
        //

        /**
         * Returns the available configuration ids.
         */
        public Set<String> getAvailableConfigurationIds() {
            return this.configurations.keySet();
        }

        /**
         * Returns the available versions for a specified configuration id.
         *
         * @param configId The configuration id to get available versions for.
         */
        public List<String> getAvailableVersions(String configId) {
            return this.configurations.get(configId);
        }
    }
}
