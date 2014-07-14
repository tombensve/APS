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
 *         2011-07-22: Created!
 *         2012-02-13: Updated to reflect changes in APSConfiguration.
 *
 */
package se.natusoft.osgi.aps.core.config.model.admin;

import se.natusoft.osgi.aps.api.core.config.APSConfig;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigAdmin;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigEditModel;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigEnvironment;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigValueEditModel;
import se.natusoft.osgi.aps.core.config.model.APSConfigInstanceMemoryStoreImpl;

import java.util.*;


/**
 * This represents a specific configuration.
 * <p>
 * This API is intended for editing configuration. Start by getting the APSConfigModel that represents the configuration structure. Then
 * each model value (APSConfigValueModel) can be used to get or set configuration values using this API. Under the surface the whole
 * configuration is stored in a java.util.Properties, which also can be fetched and set directly. It is however the config definition models
 * that generate the keys for the values in the Properties, which is why they are used to set and get configuration values.
 * <p/>
 * Please note that some configuration values are not dependent on configuration environment while others are. Those configuration values
 * whose definition are annotated with @APSConfigItemDescription(environmentSpecific=true) will have different values depending on the configuration
 * environment, which is why that is also passed to all get and set methods. Easiest is to not care which values are env specified and which
 * are and always pass a configuration environment. If you are making a GUI editor for the configuration it is however a good idea to
 * check if a value is configuration environment specific or not so that the user can choose for which environment to set a value. This
 * information is available in APSConfigValueModel.isConfigEnvironmentSpecific(). The APSConfigAdminService also provides all
 * defined configuration environments since it is also used for defining the configuration environments.
 */
public class APSConfigAdminImpl implements APSConfigAdmin {
    //
    // Private Members
    //

    /** The configuration model. */
    private APSConfigEditModel configModel;

    /** The configuration id specified by the user. */
    private String configId;

    /** The configuration version. */
    private String version;

    /** The configuration values. */
    private APSConfigInstanceMemoryStoreImpl configInstanceMemoryStore;

    //
    // Constructors
    //

    /**
     * Creates a new APSConfigAdminImpl instance.
     *
     * @param configModel The config model of this configuration.
     * @param configInstanceMemoryStore The values of this configuration.
     */
    public APSConfigAdminImpl(APSConfigEditModel configModel, APSConfigInstanceMemoryStoreImpl configInstanceMemoryStore) {
        this.configModel = configModel;
        this.configInstanceMemoryStore = configInstanceMemoryStore;
        this.configId = configModel.getConfigId();
        this.version = configModel.getVersion();
    }

    //
    // Methods
    //

    /**
     * Returns the configuration values.
     */
    public synchronized APSConfigInstanceMemoryStoreImpl getConfigInstanceMemoryStore() {
        return this.configInstanceMemoryStore;
    }

    /**
     * Sets or replaced the configuration values.
     *
     * @param configValues The configuration values to set.
     */
    public synchronized void setConfigInstanceMemoryStore(APSConfigInstanceMemoryStoreImpl configValues) {
        // Since there are APSConfigValueImpl instances sitting with the current instance memory store
        // we cannot simply replace it. We have to copy the actual values from the received store into
        // the current store.
        this.configInstanceMemoryStore.replaceWithValuesFrom(configValues);
    }

    /**
     * Returns the version of the configuration.
     */
    @Override
    public String getVersion() {
        return this.version;
    }

    /**
     * Returns the group of the configuration or "" if none.
     */
    @Override
    public String getGroup() {
        return this.configModel.getGroup();
    }

    /**
     * Returns the configuration id.
     */
    @Override
    public String getConfigId() {
        return this.configId;
    }


    // The following API is to support configuration editors.

    /**
     * @return the structural root model of the configuration.
     */
    @Override
    public APSConfigEditModel getConfigModel() {
        return this.configModel;
    }

    /**
     * Returns a configuration value.
     *
     * @param valueEditModel The value model holding the key to the value.
     * @param configEnvironment This argument can always be null. If the config value is not config env specific then this argument has no effect.
     *                          If the config value is config env specific then the value is gotten for the specific config env when this argument
     *                          is non null, and for first that have a value if this is null. It is however strongly recommended to always pass
     *                          a valid config env for this.
     * @return The referenced config value or a default value.
     */
    @Override
    public synchronized String getConfigValue(APSConfigValueEditModel valueEditModel, APSConfigEnvironment configEnvironment) {
        String value = this.configInstanceMemoryStore.getConfigValue(valueEditModel.getKey(configEnvironment));
        if (value == null) {
            value = valueEditModel.getDefaultValue(configEnvironment);
            if (value != null) {
                this.configInstanceMemoryStore.setConfigValue(valueEditModel.getKey(configEnvironment), value);
            }
        }

        return value;
    }

    /**
     * Returns the timestamp for the specified config value.
     *
     * @param valueEditModel The value model holding the key to the value.
     * @param configEnvironment This argument can always be null. If the config value is not config env specific then this argument has no effect.
     *                          If the config value is config env specific then the value is gotten for the specific config env when this argument
     *                          is non null, and for first that have a value if this is null. It is however strongly recommended to always pass
     *                          a valid config env for this.
     * @return The timestamp of the config value which will be 0 if not set (January 1, 1970 00:00:00).
     */
    public long getConfigValueTimestamp(APSConfigValueEditModel valueEditModel, APSConfigEnvironment configEnvironment) {
        long timestamp = 0;
        String ts = this.configInstanceMemoryStore.getConfigValue(valueEditModel.getTimestampKey(configEnvironment));
        if (ts != null) {
            timestamp = Long.valueOf(ts);
        }

        return timestamp;
    }

    /**
     * Sets a configuration value.
     *
     * @param valueEditModel The value model holding the key to the value.
     * @param value The value to set.
     * @param configEnvironment This argument can always be null. If the config value is not config env specific then this argument has no effect.
     *                          If the config value is config env specific then the value is set for the specific config env when this argument
     *                          is non null, and for all config envs if this argument is null. It is however strongly recommended to always pass
     *                          a valid config env for this.
     */
    @Override
    public synchronized void setConfigValue(APSConfigValueEditModel valueEditModel, String value, APSConfigEnvironment configEnvironment) {
        this.configInstanceMemoryStore.setConfigValue(valueEditModel.getKey(configEnvironment), value);
        this.configInstanceMemoryStore.setConfigValue(valueEditModel.getTimestampKey(configEnvironment), "" + new Date().getTime());
    }

    /**
     * Removes a configuration value.
     *
     * @param valueEditModel The value model holding the key to the value.
     * @param configEnvironment This argument can always be null. If the config value is not config env specific then this argument has no effect.
     *                          If the config value is config env specific then the value is removed for the specific config env when this argument
     *                          is non null, and for all config envs if this argument is null, making null a more useful value in this case.
     * @return The removed value.
     */
    @Override
    public synchronized String removeConfigValue(APSConfigValueEditModel valueEditModel, APSConfigEnvironment configEnvironment) {
        this.configInstanceMemoryStore.setConfigValue(valueEditModel.getTimestampKey(configEnvironment), "" + new Date().getTime());
        return this.configInstanceMemoryStore.removeConfigValue(valueEditModel.getKey(configEnvironment));
    }

    /**
     * Gets a configuration "many" value.
     *
     * @param valueEditModel The value model holding the key to the value.
     * @param index The index of the "many" value.
     * @param configEnvironment This argument can always be null. If the config value is not config env specific then this argument has no effect.
     *                          If the config value is config env specific then the value is gotten for the specific config env when this argument
     *                          is non null, and for first that have a value if this is null. It is however strongly recommended to always pass
     *                          a valid config env for this.
     * @return The referenced config value or a default value.
     */
    @Override
    public synchronized String getConfigValue(APSConfigValueEditModel valueEditModel, int index, APSConfigEnvironment configEnvironment) {
        String value = this.configInstanceMemoryStore.getConfigValue(valueEditModel.getKey(configEnvironment, index));

        return value != null ? value : valueEditModel.getDefaultValue(configEnvironment);
    }

    /**
     * Sets a configuration "many" value.
     *
     * @param valueEditModel The value model holding the key to the value.
     * @param index The index of the "many" value.
     * @param value the many value to set.
     * @param configEnvironment This argument can always be null. If the config value is not config env specific then this argument has no effect.
     *                          If the config value is config env specific then the value is set for the specific config env when this argument
     *                          is non null, and for all config envs if this argument is null. It is however strongly recommended to always pass
     *                          a valid config env for this.
     */
    @Override
    public synchronized void setConfigValue(APSConfigValueEditModel valueEditModel, int index, String value, APSConfigEnvironment configEnvironment) {
        this.configInstanceMemoryStore.setConfigValue(valueEditModel.getKey(configEnvironment, index), value);
        this.configInstanceMemoryStore.setConfigValue(valueEditModel.getTimestampKey(configEnvironment), "" + new Date().getTime());
    }

    /**
     * Adds a configuration "many" value last.
     *
     * @param valueEditModel The value model holding the key to the value.
     * @param value The value to add.
     * @param configEnvironment This argument can always be null. If the config value is not config env specific then this argument has no effect.
     *                          If the config value is config env specific then the value is removed for the specific config env when this argument
     *                          is non null, and for all config envs if this argument is null, making null a more useful value in this case.
     */
    @Override
    public void addConfigValue(APSConfigValueEditModel valueEditModel, String value, APSConfigEnvironment configEnvironment) {
        int size = getSize(valueEditModel, configEnvironment);
        setConfigValue(valueEditModel, size, value, configEnvironment);
        setSize(size + 1, valueEditModel, configEnvironment);
    }

    /**
     * Removes a configuration "many" value.
     *
     * @param valueEditModel The value model holding the key to the value.
     * @param index The index to remove.
     * @param configEnvironment This argument can always be null. If the config value is not config env specific then this argument has no effect.
     *                          If the config value is config env specific then the value is removed for the specific config env when this argument
     *                          is non null, and for all config envs if this argument is null, making null a more useful value in this case.
     * @return The removed value.
     */
    @Override
    public synchronized String removeConfigValue(APSConfigValueEditModel valueEditModel, int index, APSConfigEnvironment configEnvironment) {
        String value;
        if (index == (getSize(valueEditModel, configEnvironment) -1)) {
            value = this.configInstanceMemoryStore.removeConfigValue(valueEditModel.getKey(configEnvironment, index));
            setSize(getSize(valueEditModel, configEnvironment) - 1, valueEditModel, configEnvironment);
        }
        else {
            // Transfer entries to temp List, remove entry, create entries again.
            List<String> values = new LinkedList<String>();
            int size = getSize(valueEditModel, configEnvironment);
            for (int i = 0; i < size; i++) {
                values.add(this.configInstanceMemoryStore.removeConfigValue(valueEditModel.getKey(configEnvironment, i)));
            }

            value = values.remove(index);

            setSize(0, valueEditModel, configEnvironment);
            for (String val : values) {
                addConfigValue(valueEditModel, val, configEnvironment);
            }
        }
        this.configInstanceMemoryStore.setConfigValue(valueEditModel.getTimestampKey(configEnvironment), "" + new Date().getTime());

        return value;
    }

    /**
     * The APSConfigModel that represents an APSConfigList represents just the list itself. This returns a new APSConfigModel
     * that represents an entry at a specific index in the list and can be used to set and get values for the config entry at that index.
     * <p/>
     * The returned model is a temporary model and should <b>only</b> be used to access and modify values of the specified index.
     * Never ever pass such a model as input to any of the get/add/removeConfig() methods. It should only be used with
     * the *configValue() methods.
     *
     * @param configModel The original config model representing its structural place.
     * @param index The index the get a version for.
     * @param configEnvironment This argument can always be null. If the config value is not config env specific then this argument has no effect.
     *
     * @return A config model representing the specified index.
     */
    public synchronized APSConfigEditModel getConfigListEntry(APSConfigEditModel configModel, int index, APSConfigEnvironment configEnvironment) {
        return ((APSConfigEditModelImpl)configModel).createIndexVersion(index, configModel, configEnvironment);
    }

    /**
     * The APSConfigModel that represents an APSConfigList represents just the list itself. This creates a new list entry and
     * returns a model representing that entry and can be used to set and get values in the entry.
     * <p/>
     * The returned model is a temporary model and should <b>only</b> be used to access and modify values of the specified index.
     * Never ever pass such a model as input to any of the get/add/removeConfig() method. It should only be used with
     * the *configValue() methods.
     *
     * @param configModel The config model representing the APSConfigList.
     * @param configEnvironment This argument can always be null. If the config value is not config env specific then this argument has no effect.
     *                          If the config value is config env specific then the value is removed for the specific config env when this argument
     *                          is non null, and for all config envs if this argument is null, making null a more useful value in this case.
     * @return A new config model representing the created list entry.
     */
    public synchronized APSConfigEditModel createConfigListEntry(APSConfigEditModel configModel, APSConfigEnvironment configEnvironment) {
        int size = getSize(configModel, configEnvironment);
        setSize(size + 1, configModel, configEnvironment);
        return ((APSConfigEditModelImpl)configModel).createIndexVersion(size, configModel, configEnvironment);
    }

    /**
     * This removes a config entry in the APSConfigList represented by the specified APSConfigModel.
     *
     * @param configModel The config model representing the config entry to remove.
     * @param index The index of the entry to remove.
     * @param configEnvironment This argument can always be null. If the config value is not config env specific then this argument has no effect.
     *                          If the config value is config env specific then the value is removed for the specific config env when this argument
     *                          is non null, and for all config envs if this argument is null, making null a more useful value in this case.
     */
    public synchronized void removeConfigListEntry(APSConfigEditModel configModel, int index, APSConfigEnvironment configEnvironment) {
        ConfigValueKey key = new ConfigValueKey(configModel.getKey(configEnvironment));
        int size = getSize(configModel, configEnvironment);
        
        List<Map<String, String>> keepListValues = new LinkedList<Map<String, String>>();
        for (int i = 0; i < size; i++) {
            ConfigValueKey indexKey = key.getNodeKey(i);
            int indexKeyLen = indexKey.length();

            Map<String, String> indexValues = new HashMap<String, String>();
            for (String confKey : this.configInstanceMemoryStore.getKeys()) {
                if (confKey.startsWith(indexKey.toString())) {
                    String value = this.configInstanceMemoryStore.removeConfigValue(confKey);
                    // We only save the part after our index key, or in other words, a relative key.
                    String relKey = confKey.substring(indexKeyLen);
                    indexValues.put(relKey, value);
                }
            }

            if (i != index) { // We don't save the one we want to remove.
                keepListValues.add(indexValues);
            }
        }

        int ix = 0;
        for (Map<String, String> indexValue : keepListValues) {
            ConfigValueKey indexKey = key.getNodeKey(ix++);
            
            for (String valueKey : indexValue.keySet()) {
                String value = indexValue.get(valueKey);
                // Since this key is relative we add the first part again with a new index.
                this.configInstanceMemoryStore.setConfigValue(indexKey + valueKey, value);
            }
        }

        setSize(size - 1, configModel, configEnvironment);
    }

    /**
     * Sets a new size of a "many" value.
     *
     * @param size The size to set.
     * @param valueEditModel The value model holding the key to the value.
     * @param configEnvironment The config environment to set the size for.
     */
    private void setSize(int size, APSConfigValueEditModel valueEditModel, APSConfigEnvironment configEnvironment) {
        this.configInstanceMemoryStore.setConfigValue(valueEditModel.getManyValueSizeKey(configEnvironment), "" + size);
        this.configInstanceMemoryStore.setConfigValue(valueEditModel.getManyValueSizeKey(configEnvironment) + "_time",
                "" + new Date().getTime());
    }

    /**
     * Gets the number of values of a "many" value.
     * <p/>
     * Please note that APSConfigModel is a subclass of APSConfigValueModel. This method is also valid for
     * APSConfigModels.
     *
     * @param valueEditModel The value model holding the key to the value.
     * @param configEnvironment The config environment to get the number of values for.
     *
     * @return The size.
     */    @Override
    public synchronized int getSize(APSConfigValueEditModel valueEditModel, APSConfigEnvironment configEnvironment) {
        int size = 0;
        String sizeStr = this.configInstanceMemoryStore.getConfigValue(valueEditModel.getManyValueSizeKey(configEnvironment));
        if (sizeStr != null) {
            try {
                size = Integer.valueOf(sizeStr);
            }
            catch (NumberFormatException nfe) {/* OK */}
        }
        return size;
    }

    /**
     * This object represents a specific configuration. When editing a configuration you might not want changed values to
     * have immediate effect, but rather have the user do a "save" to change the active configuration. In this case use
     * this method to clone the config, edit that, then on save call APSConfigAdminService.updateConfiguration(clone).
     */
    @Override
    public synchronized APSConfigAdmin cloneConfig() {
        Properties newProps = new Properties();
        for (String key : this.configInstanceMemoryStore.getKeys()) {
            newProps.setProperty(key, this.configInstanceMemoryStore.getConfigValue(key));
        }

        return new APSConfigAdminImpl(this.configModel, new APSConfigInstanceMemoryStoreImpl(newProps));
    }

    /**
     * Sends an event to all listeners of modified config.
     * <p/>
     * This needs to be called by any code that updates config values. I do not
     * want to trigger this for every changed value!
     */
    public void sendConfigModifiedEvent() {
        APSConfigEditModel topConfigEditModel = this.configModel;
        while (topConfigEditModel.getParent() != null) {
            topConfigEditModel = topConfigEditModel.getParent();
        }
        APSConfig instance = ((APSConfigEditModelImpl)topConfigEditModel).getInstance();
        instance.triggerConfigChangedEvent(topConfigEditModel.getConfigId());
    }

    /**
     * @return The hash code of the object.
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + (this.configId != null ? this.configId.hashCode() : 0);
        hash = 23 * hash + (this.version != null ? this.version.hashCode() : 0);
        return hash;
    }

    /**
     * Compares for equality.
     *
     * @param obj The object to compare to.
     *
     * @return true if equal.
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof APSConfigAdmin)) {
            return false;
        }
        APSConfigAdmin cobj = (APSConfigAdmin)obj;
        return this.configId.equals(cobj.getConfigId()) &&
               this.version.equals(cobj.getVersion());

    }

}
