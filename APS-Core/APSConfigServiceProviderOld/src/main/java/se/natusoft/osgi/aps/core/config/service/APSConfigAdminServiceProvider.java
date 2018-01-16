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
 *         2011-08-05: Created!
 *
 */
package se.natusoft.osgi.aps.core.config.service;

import se.natusoft.osgi.aps.api.core.configold.model.admin.APSConfigAdmin;
import se.natusoft.osgi.aps.api.core.configold.model.admin.APSConfigEnvironment;
import se.natusoft.osgi.aps.api.core.configold.service.APSConfigAdminService;
import se.natusoft.osgi.aps.api.core.configold.service.APSConfigAdminService.APSConfigEnvAdmin;
import se.natusoft.osgi.aps.api.core.configold.service.APSConfigException;
import se.natusoft.osgi.aps.core.config.model.ConfigEnvironmentProvider;
import se.natusoft.osgi.aps.core.config.model.admin.APSConfigAdminImpl;
import se.natusoft.osgi.aps.core.config.model.admin.APSConfigEnvironmentImpl;
import se.natusoft.osgi.aps.core.config.store.APSConfigEnvStore;
import se.natusoft.osgi.aps.core.config.store.APSConfigMemoryStore;
import se.natusoft.osgi.aps.core.config.store.APSConfigPersistentStore;
import se.natusoft.osgi.aps.core.config.store.ConfigStoreInfo;
import se.natusoft.osgi.aps.tools.APSLogger;

import java.util.List;
import java.util.Set;

/**
 * Implements the APSConfigAdminService.
 */
public class APSConfigAdminServiceProvider implements APSConfigAdminService, APSConfigEnvAdmin, ConfigEnvironmentProvider {
    //
    // Private Members
    //

    /** The available configuration environments. */
    private APSConfigEnvStore envStore = null;

    /** Holds the active configurations in memory. */
    private APSConfigMemoryStore memoryStore;

    /** The configuration data store. */
    private APSConfigPersistentStore configStore = null;

    /** Provides information about stored configold. */
    private ConfigStoreInfo configStoreInfo = null;

    /** Our logger. */
    private APSLogger logger = null;

    //
    // Constructors
    //

    /**
     * Creates a new APSConfigAdminServiceProvider instance.
     *
     * @param logger The logger to log to.
     * @param memoryStore The store for storing in memory configurations.
     * @param envStore The store for persistent configuration enviornment definitions.
     * @param configStore The store for persisten configuration data.
     *
     * @throws se.natusoft.osgi.aps.api.core.configold.service.APSConfigException on any failure.
     */
    public APSConfigAdminServiceProvider(
            APSLogger logger,
            APSConfigMemoryStore memoryStore,
            APSConfigEnvStore envStore,
            APSConfigPersistentStore configStore
        ) throws APSConfigException {

        this.logger = logger;
        this.memoryStore = memoryStore;
        this.envStore = envStore;
        this.configStore = configStore;
        this.configStoreInfo = this.memoryStore; // Only supply those that are active.

    }

    //
    // Methods
    //

    /**
     * Adds a new configuration environment, like "test" or "prod" for example. If this is
     * the first environment added it will also be selected as the active environment.
     *
     * @param name The name of the environment to add.
     * @param description The description of the environment to add.
     *
     * @throws APSConfigException on failure to add environment.
     */
    @Override
    public void addConfigEnvironment(String name, String description) throws APSConfigException {
        this.envStore.addEnvironment(new APSConfigEnvironmentImpl(name, description, 0));
    }

    /**
     * Removes the specified environment.
     *
     * @param environment The environment to remove.
     */
    @Override
    public void removeConfigEnvironment(APSConfigEnvironment environment) throws APSConfigException {
        this.envStore.removeEnvironment(environment);
    }

    /**
     * Returns a configold environment by its name.
     *
     * @param name The name of the environment to get.
     *
     * @return The named environment or null if it does not exist.
     */
    public APSConfigEnvironment getConfigEnvironmentByName(String name) {
        return this.envStore.getConfigEnvironmentByName(name);
    }

    /**
     * Returns the avalable environments.
     */
    @Override
    public List<APSConfigEnvironment> getAvailableConfigEnvironments() {
        return this.envStore.getConfigEnvironments();
    }

    /**
     * Selects a specified environment as the active environment to use for APSConfigService
     * when getting configurations.
     *
     * @param environment The environment to make active.
     *
     * @throws se.natusoft.osgi.aps.api.core.configold.service.APSConfigException if the specified environment is not among those returned by
     *                                   getAvailableEnvironments().
     */
    @Override
    public void selectActiveConfigEnvironment(APSConfigEnvironment environment) throws APSConfigException {
        this.envStore.setActiveConfigEnvironment(environment);
        this.logger.info("Active configold environment was changed to: '" + environment.getName() + "'!");
    }

    /**
     * Returns the currently active environment.
     */
    @Override
    public APSConfigEnvironment getActiveConfigEnvironment() {
        return this.envStore.getActiveConfigEnvironment();
    }

    /**
     * @return The available environments.
     */
    @Override
    public List<APSConfigEnvironment> getConfigEnvironments() {
        return this.envStore.getConfigEnvironments();
    }

    /**
     * Returns the API for administering configuration environments.
     */
    @Override
    public APSConfigEnvAdmin getConfigEnvAdmin() {
        return this;
    }

    /**
     * Returns all published configuration instances.
     */
    @Override
    public List<APSConfigAdmin> getAllConfigurations() {
        return this.memoryStore.getAllConfigurations();
    }

    /**
     * Returns all available (registered but not necessary active) configuration ids.
     */
    @Override
    public Set<String> getAllConfigurationIds() {
        return this.configStoreInfo.getAvailableConfigurationIds();
    }

    /**
     * Returns all known versions of the specified configuration id.
     *
     * @param configId The identifier of the configuration to get the versions for.
     */
    @Override
    public List<String> getVersions(String configId) {
        return this.configStoreInfo.getAvailableVersions(configId);
    }

    /**
     * Returns the admin object for the specified configuration. This object lets you edit the configuration.
     * <p/>
     * The returned object sits on the active configuration values, thus any change to any value will directly
     * be reflected for clients of the configuration. If you want to do an edit then save, start by calling
     * cloneConfig() on the returned object, and then edit that instead. When you want to save, call
     * updateConfiguration(APSConfigAdmin) in this service.
     *
     * @param configId The id of the configuration.
     * @param version The version to get the configuration for.
     */
    @Override
    public APSConfigAdmin getConfiguration(String configId, String version) {
        return this.memoryStore.getConfiguration(configId, version);
    }

    /**
     * Updates an existing configuration with the specified one.
     *
     * @param configuration The configuration to update with.
     *
     * @throws se.natusoft.osgi.aps.api.core.configold.service.APSConfigException on any failure to persist the changes.
     */
    @Override
    public void updateConfiguration(APSConfigAdmin configuration) throws APSConfigException {
        if (this.memoryStore.hasConfiguration(configuration)) {
            this.memoryStore.updateConfiguration((APSConfigAdminImpl)configuration);
        }
        this.configStore.saveConfiguration((APSConfigAdminImpl)configuration);
        this.logger.info("Configuration '" + configuration.getConfigId() + ":" + configuration.getVersion() + "' was updated!");
    }

    /**
     * Removes the specified configuration.
     *
     * @param configuration The configuration to remove.
     *
     * @throws se.natusoft.osgi.aps.api.core.configold.service.APSConfigException on failure to remove.
     */
    @Override
    public void removeConfiguration(APSConfigAdmin configuration) throws APSConfigException {
        if (this.memoryStore.hasConfiguration(configuration)) {
            this.memoryStore.removeConfiguration(configuration);
        }
        this.configStore.deleteConfiguration(configuration);
        this.logger.info("Configuration '" + configuration.getConfigId() + ":" + configuration.getVersion() + "' was deleted!");
    }

    /**
     * Removes the specified configuration from persistent store. If a client re-registers this configuration
     * with the APSConfigService after this call then all previous configuration for it will be gone and only
     * blank, null or default values will be returned.
     *
     * @param configId The id of the configuration to remove.
     * @param version The version of the configuration to remove.
     *
     * @throws APSConfigException on failure to remove.
     */
    public void removeConfiguration(String configId, String version) throws APSConfigException {
        this.configStore.deleteConfiguration(configId, version);
        this.logger.info("Configuration '" + configId + ":" + version + "' was deleted!");
    }
}
