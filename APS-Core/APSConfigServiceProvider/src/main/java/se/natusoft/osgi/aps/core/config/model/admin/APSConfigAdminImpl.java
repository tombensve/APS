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
import se.natusoft.osgi.aps.api.core.config.model.admin.*;
import se.natusoft.osgi.aps.api.core.config.service.APSConfigException;
import se.natusoft.osgi.aps.core.config.model.APSConfigInstanceMemoryStoreImpl;

import static se.natusoft.osgi.aps.core.config.model.StaticUtils.*;

import java.util.*;


/**
 * This represents a specific configuration.
 *
 * Configurations are really a tree of configuration objects. How the configuration values are stored however
 * are up to the implementation. In this case we store it in a simple Properties object which we load and save.
 *
 * The configuration objects used by clients are read only! To set and change configuration values you use
 * this class along with models representing the structure of the config objects:
 *
 * __APSConfigValueEditModel__ - This model represents a config value.
 *
 * __APSConfigEditModel__ -This model represents a config object and have a values() method that return value
 * fields. This model extends APSConfigValueEditModel.
 *
 * These models represents the structure of the configuration. They do not represent specific instances of
 * configuration values. They don't know about indexes for list values nor configuration environments. They
 * only supply a navigable structure.
 *
 * To actually reference a configuration value or configuration object you need to build a reference to it.
 * createRef() does just that. Then you add models to reference specific config.
 *
 *     APSConfigAdmin admin = ...
 *
 *     APSConfigEditModel root = admin.getConfigModel();
 *     APSConfigValueEditModel url = root.getValueByName("url");
 *
 *     APSConfigReference urlRef = admin.createRef()._(root)._(url);
 *     System.out.println(admin.getConfigValue(urlRef));
 *     admin.setConfigValue(urlRef, "http://...");
 *
 * Referencing lists:
 *
 *     APSConfigAdmin admin = ...
 *
 *     APSConfigEditModel root = admin.getConfigModel();
 *     APSConfigEditModel dataSources = root.getValeuByName("datasources");
 *     APSConfigValueEditModel dsUrl = dataSources.getValueByName("url");
 *
 *     APSConfigReference dataSource0Url = admin.createRef()
 *             ._(root)._(dataSources, 0)._(dsUrl);
 *     System.out.println(admin.getConfigValue(dataSource0Url));
 *
 * Getting for specific config environment:
 *
 *     APDConfigEnvironment prod = ...
 *
 *     APSConfigReference dataSource0Url = admin.createRef()
 *             ._(root)._(dataSources, 0)._(dsUrl).configEnvironment(prod);
 *
 *
 * Please note that some configuration values are not dependent on configuration environment while others are. Those
 * configuration values whose definition are annotated with @APSConfigItemDescription(environmentSpecific=true) will
 * have different values depending on the configuration environment. Easiest is to not care which values are env
 * specific and which are not, and always provide a configuration environment. It will only be used where it applies.
 *
 * If you are making a GUI editor for the configuration it is however a good idea to check if a value is
 * configuration environment specific or not so that the user can choose for which environment to set a value. This
 * information is available in APSConfigValueModel.isConfigEnvironmentSpecific(). The APSConfigAdminService also
 * provides all defined configuration environments since it is also used for defining the configuration environments.
 */
public class APSConfigAdminImpl implements APSConfigAdmin {
    //
    // Private Members
    //

    /** The configuration model. */
    private APSConfigEditModel configModel;

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
        return this.configModel.getVersion();
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
        return this.configModel.getConfigId();
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
     * Adds a timestamp.
     *
     * @param reference The reference to add timestamp for.
     */
    private void timestamp(APSConfigReference reference) {
        APSConfigReferenceImpl _reference = (APSConfigReferenceImpl)reference;
        setConfigValue(_reference.getValueKey().getTimeKey(), "" + new Date().getTime());
    }

    /**
     * Creates a new empty reference that needs to be populated with APSConfig*EditModels to build a config value reference.
     */
    @Override
    public APSConfigReference createRef() {
        return new APSConfigReferenceImpl();
    }

    /**
     * Creates a new reference already containing the root edit model.
     */
    @Override
    public APSConfigReference createRootRef() {
        return createRef()._(getConfigModel());
    }

    /**
     * Returns a configuration value.
     *
     * @param reference The reference to the value.
     *
     * @return The referenced config value or a default value.
     */
    @Override
    public synchronized String getConfigValue(APSConfigReference reference) {
        String value = getConfigValue(reference.toString());

        if (value == null) {
            value = reference.getDefaultValue();
            if (value != null) {
                setConfigValue(reference, value);
            }
        }

        return value;
    }

    /**
     * Sets a configuration value.
     *
     * @param reference The reference to the config value to set.
     * @param value The value to set.
     */
    @Override
    public synchronized void setConfigValue(APSConfigReference reference, String value) {
        setConfigValue(reference.toString(), value);
        timestamp(reference);
    }

    /**
     * Returns the value of the config with the specified key.
     * @param key The key of the config to get.
     */
    private String getConfigValue(String key) {
        return this.configInstanceMemoryStore.getConfigValue(key);
    }

    /**
     * Sets a config value.
     *
     * @param key The key of the config value to set.
     * @param value The value to set.
     */
    private void setConfigValue(String key, String value) {
        this.configInstanceMemoryStore.setConfigValue(key, value);
    }

    /**
     * Removes a config value.
     *
     * @param key The key of the config value to remove.
     * @return The value of the removed config value.
     */
    private String removeConfigValue(String key) {
        return this.configInstanceMemoryStore.removeConfigValue(key);
    }

    /**
     * Return the config keys.
     */
    private Set<String> getConfigKeys() {
        return this.configInstanceMemoryStore.getKeys();
    }

    /**
     * Adds a configuration value.
     *
     * @param reference The reference to the value to add.
     * @param value The actual value to add.
     */
    @Override
    public synchronized void addConfigValue(APSConfigReference reference, String value) {
        ConfigValueKey key = toImpl(reference).getValueKey();

        if (key.isMany()) {
            int size = getListSize(reference);
            setConfigValue(key.forIndex(size).toString(), value);
            ++size;
            setListSize(reference, size);
        }
        else {
            throw new APSConfigException("Bad call! Can only add values to list type values!");
        }
    }

    /**
     * Removes a config value.
     *
     * @param reference A reference to the config value to remove.
     */
    @Override
    public synchronized String removeConfigValue(APSConfigReference reference) {

        // Handle multi value case.
        ConfigValueKey key = toImpl(reference).getValueKey();

        String retVal = null;

        if (key.isMany()) {
            List<String> values = new LinkedList<>();

            int size = getListSize(reference);

            for (int i = 0; i < size; i++) {
                ConfigValueKey iKey = key.forIndex(i);
                if (key.getIndex() != i) {
                    values.add(getConfigValue(iKey.toString()));
                }
                else {
                    retVal = getConfigValue(iKey.toString());
                }
                removeConfigValue(iKey.toString());
            }

            --size;

            for (int i = 0; i < size; i++) {
                ConfigValueKey iKey = key.forIndex(i);
                setConfigValue(iKey.toString(), values.get(i));
            }
            values.clear();

            setListSize(reference, size);
        }
        // Single value case.
        else {
            removeConfigValue(reference.toString());
            timestamp(reference);
        }

        return retVal;
    }

    /**
     * Returns the size of the list pointed to by the specified reference.
     *
     * @param reference The reference to get the size for.
     */
    @Override
    public synchronized int getListSize(APSConfigReference reference) {
        APSConfigReferenceImpl ro_ref = toImpl(reference);
        String sizeKey = ro_ref.getValueKey().getSizeKey();
        String sizeStr = getConfigValue(sizeKey);
        return Integer.valueOf(sizeStr != null ? sizeStr : "0");
    }

    /**
     * Sets a new size of a list.
     *
     * @param reference The reference to the list.
     * @param size The new size.
     */
    private void setListSize(APSConfigReference reference, int size) {
        APSConfigReferenceImpl ro_ref = toImpl(reference);
        String sizeKey = ro_ref.getValueKey().getSizeKey();
        setConfigValue(sizeKey, "" + size);
    }

    /**
     * Adds a config **object** list entry returning a reference to the new entry.
     *
     * @param reference A reference to the many list to add an entry to.
     *
     * @return A reference to the new entry.
     */
    @Override
    public synchronized APSConfigReference addConfigList(APSConfigReference reference) {
        int size = getListSize(reference);
        setListSize(reference, size + 1);

        return reference.index(size);
    }

    /**
     * Removes the specified list config object using reference to it. **Note** that the reference
     * must include a specific index!
     *
     * @param reference The reference to the specific config object to delete.
     */
    @Override
    public synchronized void removeConfigList(APSConfigReference reference) {
        APSConfigReferenceImpl ro_ref = toImpl(reference);
        removeConfigListEntry(reference, ro_ref.getIndex());
    }

    /**
     * This removes a list entry by reindexing the list and returns the no longer valid index (previously last index).
     *
     * @param ref The reference pointing to the list to reindex.
     * @param from The index to start re-indexing from.
     * @param to The index to stop re-indexing at.
     * @param startingAt The starting index of the new indexes.
     */
    private int reindexMSList(APSConfigReference ref, int from, int to, int startingAt) {
        APSConfigReference iteratorRef = ref.copy();
        APSConfigReference newIndexRef = ref.copy();

        int current = startingAt;
        for (int i = from; i <= to; i++) {
            APSConfigReference iRef = iteratorRef.index(i);

            for (String key : this.configInstanceMemoryStore.getKeys()) {

                if (key.startsWith(iRef.toString())) {
                    String value = this.configInstanceMemoryStore.removeConfigValue(key);
                    String newKey = key.replace(iRef.toString(), newIndexRef.index(current).toString());
                    this.configInstanceMemoryStore.setConfigValue(newKey, value);
                }
            }
            ++current;
        }

        return current;
    }

    /**
     * This removes a branch or a leaf entry in a list.
     *
     * @param ref The reference to the list.
     * @param index The index in the list to remove.
     */
    private void removeConfigListEntry(APSConfigReference ref, int index) {
        int size = getListSize(ref);
        int last = reindexMSList(ref, index + 1, size - 1, index);
        setListSize(ref, size - 1);

        // If the removed entry happens to be the last entry then the reindex have not removed anything.
        // So to be safe lets do a cleanup of the last index.
        APSConfigReference removeRef = ref.index(last);
        for (String key : this.configInstanceMemoryStore.getKeys()) {
            if (key.startsWith(removeRef.toString())) {
                this.configInstanceMemoryStore.removeConfigValue(key);
            }
        }

    }

    /**
     * This object represents a specific configuration. When editing a configuration you might not want changed values
     * to have immediate effect, but rather have the user do a "save" to change the active configuration. In this case
     * use this method to clone the config, edit that, then on save call APSConfigAdminService.updateConfiguration(clone).
     */
    @Override
    public synchronized APSConfigAdmin cloneConfig() {
        Properties newProps = new Properties();
        for (String key : getConfigKeys()) {
            newProps.setProperty(key, getConfigValue(key));
        }

        return new APSConfigAdminImpl(this.configModel, new APSConfigInstanceMemoryStoreImpl(newProps));
    }

    /**
     * Sends an event to all listeners of modified config.
     * <p/>
     * This needs to be called by any code that updates config values. I do not
     * want to trigger this for every changed value!
     */
    @Override
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
        hash = 23 * hash + (this.configModel.getConfigId() != null ? this.configModel.getConfigId().hashCode() : 0);
        hash = 23 * hash + (this.configModel.getVersion() != null ? this.configModel.getVersion().hashCode() : 0);
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
        return this.configModel.getConfigId().equals(cobj.getConfigId()) &&
               this.configModel.getVersion().equals(cobj.getVersion());

    }

}
