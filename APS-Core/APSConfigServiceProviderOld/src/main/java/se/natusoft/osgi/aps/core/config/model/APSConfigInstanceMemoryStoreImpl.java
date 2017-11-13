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
 *     Tommy Svensson (tommy@natusoft.se)
 *         Changes:
 *         2012-02-19: Created!
 *
 */
package se.natusoft.osgi.aps.core.config.model;

import se.natusoft.osgi.aps.api.core.configold.model.admin.APSConfigValueStore;
import se.natusoft.osgi.aps.exceptions.APSRuntimeException;

import java.util.Properties;
import java.util.Set;

/**
 * Holds the values of a configuration.
 */
public class APSConfigInstanceMemoryStoreImpl implements APSConfigValueStore {
    //
    // Private Members
    //

    /** We store configold values in this Properties object. */
    private Properties props = null;

    //
    // Constructors
    //

    /**
     * Creates a new APSConfigInstanceMemoryStoreImpl instance.
     *
     * @param props The backing properties of this configuration values box.
     */
    public APSConfigInstanceMemoryStoreImpl(Properties props) {
        this.props = props;
    }

    //
    // Methods
    //

    /**
     * @return The backing properties of this configuration values box.
     */
    public Properties getProperties() {
        return this.props;
    }

    /**
     * Replaces the backing properties of this configuration values box.
     *
     * @param props The new properties to use.
     */
    public void setProperties(Properties props) {
        this.props = props;
    }

    /**
     * Returns a configuration value by its key.
     *
     * @param key The key of the configuration value to get.
     *
     * @return The configuration value for the key or null if key has not value.
     */
    @Override
    public String getConfigValue(String key) {
        return this.props.getProperty(key);
    }

    /**
     * Sets a configuration value for a key.
     *
     * @param key   The key to set the configuration value for.
     * @param value The value to set.
     */
    @Override
    public void setConfigValue(String key, String value) {
        this.props.setProperty(key, value);
    }

    /**
     * Removes a configuration value by this key.
     *
     * @param key The key to remove.
     *
     * @return The removed value.
     */
    public String removeConfigValue(String key) {
        return this.props.remove(key).toString();
    }

    /**
     * @return All configuration keys stored.
     */
    @Override
    public Set<String> getKeys() {
        return this.props.stringPropertyNames();
    }

    /**
     * Replaces the values of this configuration store with the values of the specified configuration values store.
     *
     * @param configValues The configuration values.
     */
    public void replaceWithValuesFrom(APSConfigValueStore configValues) {
        if (configValues instanceof APSConfigInstanceMemoryStoreImpl) {
            this.props = ((APSConfigInstanceMemoryStoreImpl)configValues).getProperties();
        }
        else {
            throw new APSRuntimeException("Received an unknown implementation of APSConfigValueStore: " + configValues.getClass().getName());
        }
    }


}
