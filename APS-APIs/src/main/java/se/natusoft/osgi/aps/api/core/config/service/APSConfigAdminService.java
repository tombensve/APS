/*
 *
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         Provides the APIs for the application platform services.
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
 *         2011-05-18: Created!
 *         2012-02-13: Cleaned up.
 *
 */
package se.natusoft.osgi.aps.api.core.config.service;

import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigAdmin;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigEnvironment;

import java.util.List;
import java.util.Set;

/**
 * This defines an administration service for modifying configuration values.
 * <p>
 * All handled information in this service should be persisted! It is entirely
 * up to the implementation on how it is persisted even though implementations
 * is forced to provide java.util.Properties backing.
 * <p>
 * The API for managing the configuration is designed to be compatible with
 * storing in the standard OSGi R4 configuration service though that is not
 * a requirement. However if the standard OSGi R4 configuration service is
 * used as in memory storage of configuration data then configuration definitions
 * for other non APS services can be made to persist and edit configuration
 * for those.
 * <p/>
 * Configurations are only considered actively valid if they are currently registered
 * by a deployed and active bundle. In other words, the configurations editable by
 * this service is only those that have been provided with APSConfigService.registerConfiguration()
 * since server start and have not had a APSConfigService.unregisterConfiguration() done
 * on them yet.
 */
public interface APSConfigAdminService {

    /**
     * This is a sub API for administering configuration environments.
     */
    interface APSConfigEnvAdmin {

        /**
         * Adds a new configuration environment, like "test" or "prod" for example. If this is
         * the first environment added it will also be selected as the active environment.
         *
         * @param name The name of the environment to add.
         * @param description The description of the environment to add.
         *
         * @throws APSConfigException on failure to add environment.
         */
        void addConfigEnvironment(String name, String description) throws APSConfigException;

        /**
         * Removes the specified environment.
         *
         * @param environment The environment to remove.
         *
         * @throws APSConfigException on failure to remove environment.
         */
        void removeConfigEnvironment(APSConfigEnvironment environment) throws APSConfigException;

        /**
         * Returns a config environment by its name.
         *
         * @param name The name of the environment to get.
         *
         * @return The named environment or null if it does not exist.
         */
        APSConfigEnvironment getConfigEnvironmentByName(String name);

        /**
         * Returns the available environments.
         */
        List<APSConfigEnvironment> getAvailableConfigEnvironments();

        /**
         * Selects a specified environment as the active environment to use for APSConfigService
         * when getting configurations.
         *
         * @param environment The environment to make active.
         *
         * @throws APSConfigException if the specified environment is not among those returned by
         *                                   getAvailableEnvironments().
         */
        void selectActiveConfigEnvironment(APSConfigEnvironment environment) throws APSConfigException;

        /**
         * Returns the currently active environment.
         */
        APSConfigEnvironment getActiveConfigEnvironment();
    }

    /**
     * Returns the API for administering configuration environments.
     */
    APSConfigEnvAdmin getConfigEnvAdmin();

    /**
     * Returns all published configuration instances.
     */
    List<APSConfigAdmin> getAllConfigurations();

    /**
     * Returns all available (registered but not necessary active) configuration ids for
     * the active configuration environment.
     */
    Set<String> getAllConfigurationIds();

    /**
     * Returns all known versions of the specified configuration id for the active configuration environment.
     *
     * @param configId The identifier of the configuration to get the versions for.
     */
    List<String> getVersions(String configId);

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
    APSConfigAdmin getConfiguration(String configId, String version);

    /**
     * Updates an existing configuration with the specified one. Please note that configurations are created
     * the first time a bundle registers the configuration with the APSConfigService.
     * <p/>
     * To edit a configuration, first call getConfiguration(...) then clone it, make changes to it, and then
     * call this method to update the original with the updated version.
     *
     * @param configuration The configuration to update with.
     *
     * @throws APSConfigException on any failure to persist the changes.
     */
    void updateConfiguration(APSConfigAdmin configuration) throws APSConfigException;

    /**
     * Removes the specified configuration from persistent store. If a client re-registers this configuration
     * with the APSConfigService after this call then all previous configuration for it will be gone and only
     * blank, null or default values will be returned.
     *
     * @param configuration The configuration to remove.
     *
     * @throws APSConfigException on failure to remove.
     */
    void removeConfiguration(APSConfigAdmin configuration) throws APSConfigException;

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
    void removeConfiguration(String configId, String version) throws APSConfigException;
}
