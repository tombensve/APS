/*
 *
 * PROJECT
 *     Name
 *         APS Configuration Service Provider
 *     
 *     Code Version
 *         0.10.0
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

import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigAdmin;
import se.natusoft.osgi.aps.core.config.model.admin.APSConfigAdminImpl;

import java.util.*;

/**
 * This holds active configurations in memory.
 */
public class APSConfigMemoryStore implements ConfigStoreInfo {
    //
    // Private Members
    //

    /** The stored configurations. */
    private Map<String, APSConfigAdminImpl> configurations = new HashMap<String, APSConfigAdminImpl>();

    /** Update listeners. */
    private List<ConfigUpdateListener> configUpdateListeners = new ArrayList<ConfigUpdateListener>();

    /** Holds the currently available config ids and all the versions for a specified config id. */
    private Map<String /* configId */, List<String /* version */>> idsAndVersions = new HashMap<String, List<String>>();

    //
    // Constructors
    //

    /**
     * Creates a new APSConfigMemoryStore instance.
     */
    public APSConfigMemoryStore() {}

    //
    // Methods
    //

    /**
     * Creates and returns a key.
     *
     * @param configId The configurations config id.
     * @param version The version of the configuration
     */
    private String getKey(String configId, String version) {
        return "" + configId + version;
    }

    /**
     * Adds a configuration to the memory store.
     *
     * @param configuration The configuration to add.
     */
    public void addConfiguration(APSConfigAdminImpl configuration) {
        this.configurations.put(getKey(configuration.getConfigId(), configuration.getVersion()), configuration);

        List<String> versions = this.idsAndVersions.get(configuration.getConfigId());
        if (versions == null) {
            versions = new LinkedList<String>();
            this.idsAndVersions.put(configuration.getConfigId(), versions);
        }
        if (!versions.contains(configuration.getVersion())) {
            versions.add(configuration.getVersion());
        }
    }

    /**
     * Returns a stored configuration.
     *
     * @param configId The configuration id of the configuration.
     * @param version The version of the configuration.
     */
    public APSConfigAdminImpl getConfiguration(String configId, String version) {
        return this.configurations.get(getKey(configId, version));
    }

    /**
     * Updates the memory store with this configuration.
     *
     * @param configuration The configuration to update with.
     */
    public void updateConfiguration(APSConfigAdminImpl configuration) {
        APSConfigAdminImpl currentConfiguration = this.configurations.get(getKey(configuration.getConfigId(), configuration.getVersion()));
        currentConfiguration.setConfigInstanceMemoryStore(configuration.getConfigInstanceMemoryStore());

        for (ConfigUpdateListener listener : this.configUpdateListeners) {
            listener.updated(currentConfiguration);
        }
    }

    /**
     * Returns true if the memory store holds the specified configuation.
     *
     * @param configuration The configuration to check for.
     */
    public boolean hasConfiguration(APSConfigAdmin configuration) {
        return this.configurations.get(getKey(configuration.getConfigId(), configuration.getVersion())) != null;
    }

    /**
     * Removes the specified configuration from memory.
     *
     * @param configuration The configuration to remove.
     */
    public void removeConfiguration(APSConfigAdmin configuration) {
        if (configuration != null) {
            String configKey = getKey(configuration.getConfigId(), configuration.getVersion());
            if (this.configurations.containsKey(configKey)) {
                this.configurations.remove(configKey);
            }
            if (this.idsAndVersions.containsKey(configuration.getConfigId())) {
                this.idsAndVersions.remove(configuration.getConfigId());
            }
        }
    }

    /**
     * Returns all configurations.
     */
    public List<APSConfigAdmin> getAllConfigurations() {
        List<APSConfigAdmin> configs = new ArrayList<APSConfigAdmin>();
        for (String key : this.configurations.keySet()) {
            configs.add(this.configurations.get(key));
        }

        return configs;
    }

    /**
     * Adds an update listener to the memory store.
     *
     * @param updateListener The update listener to add.
     */
    public void addUpdateListener(ConfigUpdateListener updateListener) {
        this.configUpdateListeners.add(updateListener);
    }

    /**
     * Removes an update listener from the memory store.
     *
     * @param updateListener The update listener to remove.
     */
    public void removeUpdateListener(ConfigUpdateListener updateListener) {
        this.configUpdateListeners.remove(updateListener);
    }

    /**
     * Returns the available configuration ids.
     */
    @Override
    public Set<String> getAvailableConfigurationIds() {
        return this.idsAndVersions.keySet();
    }

    /**
     * Returns the available versions for a specified configuration id.
     *
     * @param configId The configuration id to get available versions for.
     */
    @Override
    public List<String> getAvailableVersions(String configId) {
        return this.idsAndVersions.get(configId);
    }

    //
    // Inner Classes
    //

    /**
     * Classes that want to be notified of a config update to the memory store implements
     * this.
     */
    public interface ConfigUpdateListener {

        /**
         * Receives updated configuration.
         *
         * @param configuration The updated configuration.
         */
        public void updated(APSConfigAdmin configuration);
    }
}
