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
 *     Tommy Svensson (tommy.svensson@biltmore.se)
 *         Changes:
 *         2012-05-06: Created!
 *         
 */
package se.natusoft.osgi.aps.core.config.model.admin;

import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigEnvironment;

/**
 * This class manages the keys in the config store.
 * <p/>
 * Please note that the OSGi standard configuration keys are not as flexible as java Properties or HashMap.
 * They only allow: [a-z][A-Z][0-9][-][_][.]. The values added to the key by this class is compliant with
 * that. This class however takes no responsibility for the key values pass to it!
 */
public class ConfigValueKey {

    //
    // Private Members
    //

    /** The key. */
    private String key = null;

    //
    // Constructors
    //

    /**
     * Creates a new ConfigValueKey.
     *
     * @param key The initial key value.
     */
    public ConfigValueKey(String key) {
        this.key = key.toLowerCase();
    }

    /**
     * Creates a new ConfigValueKey.
     *
     * @param key The initial value.
     * @param version The version to append to the initial value (in internal key format!).
     */
    public ConfigValueKey(String key, String version) {
        this(key + getVersionKeyAddition(version));
    }

    //
    // Methods
    //

    /**
     * Returns key addition for config environment.
     *
     * @param configEnv The confing environment to add to key or null if none.
     *
     * @return A string to be added to current key when creating a new key.
     */
    private static String getConfigEnvKeyAddition(APSConfigEnvironment configEnv) {
        if (configEnv != null) {
            return "_" + configEnv.getName();
        }

        return "";
    }

    /**
     * Returns key addition for index.
     *
     * @param index The index to produce key addition for.
     *
     * @return A string to be added to the current key when creating a new key.
     */
    private static String getIndexKeyAddition(int index) {
        return "_" + index + "_";
    }

    /**
     * Returns key addition for size.
     *
     * @return A string to be added to the current key when creating a new key.
     */
    private static String getSizeKeyAddition() {
        return "_size";
    }

    /**
     * Returns the key addition for version.
     *
     * @param version The version to be added to the key.
     *
     * @return A string to be added to the key.
     */
    private static String getVersionKeyAddition(String version) {
        return "_" + version + "_";
    }

    /**
     * Gets a node key for a specific index of a 'many' node.
     *
     * @param index The index to get the key for.
     *
     * @return A new ConfigValueKey.
     */
    public ConfigValueKey getNodeKey(int index) {
        return new ConfigValueKey(this.key + getIndexKeyAddition(index));
    }

    /**
     * Gets new key for the specified config environment.
     *
     * @param configEnv The configuration environment to get the key for. For values that are configuration environment specific
     *                  a key including the configuration environment will be produced. For other values the specified config
     *                  environment has no effect. If null is passed the key is treated as a non config environment specific.
     *                  Only pass null if you are absolutely sure or you might end up with a bad key!
     *
     * @return A new ConfigValueKey.
     */
    public ConfigValueKey getValueKey(APSConfigEnvironment configEnv) {
        return new ConfigValueKey(this.key + getConfigEnvKeyAddition(configEnv));
    }

    /**
     * Gets a new key for a specific index for a 'many' type value.
     *
     * @param configEnv The configuration environment to get the key for. For values that are configuration environment specific
     *                  a key including the configuration environment will be produced. For other values the specified config
     *                  environment has no effect. If null is passed the key is treated as a non config environment specific.
     *                  Only pass null if you are absolutely sure or you might end up with a bad key!
     * @param index  The index of a "many" value.
     *
     * @return A new ConfigValueKey.
     */
    public ConfigValueKey getValueKey(APSConfigEnvironment configEnv, int index) {
        return new ConfigValueKey(this.key + getConfigEnvKeyAddition(configEnv) + getIndexKeyAddition(index));
    }

    /**
     * Gets a new key for the number of values of a 'many' value.
     *
     * @param configEnv The configuration environment to get the key for. For values that are configuration environment specific
     *                  a key including the configuration environment will be produced. For other values the specified config
     *                  environment has no effect. If null is passed the key is treated as a non config environment specific.
     *                  Only pass null if you are absolutely sure or you might end up with a bad key!
     *
     * @return The many value size key.
     */
    public ConfigValueKey getManyValueSizeKey(APSConfigEnvironment configEnv) {
        return new ConfigValueKey(this.key + getConfigEnvKeyAddition(configEnv) + getSizeKeyAddition());
    }

    /**
     * Adds key data to key.
     *
     * @param toAdd The key data to add.
     *
     * @return A new ConfigValueKey containing the added data.
     */
    public ConfigValueKey addToKey(String toAdd) {
        return new ConfigValueKey(this.key + toAdd.toLowerCase());
    }

    /**
     * Adds another key to this key.
     *
     * @param keyToAdd The key to add.
     *
     * @return A new ConfigValueKey containing the added key.
     */
    public ConfigValueKey addToKey(ConfigValueKey keyToAdd) {
        return new ConfigValueKey(this.key + keyToAdd);
    }

    /**
     * Adds a key to this node key. This is only valid if this key represents a node key.
     * <p/>
     * If the current key is not empty then a divider between this and the added key is added.
     *
     * @param keyToAdd The key to add to this node key.
     *
     * @return A new ConfigValueKey containing the added key.
     */
    public ConfigValueKey addToNodeKey(ConfigValueKey keyToAdd) {
        return new ConfigValueKey(this.key.length() > 0 ? "_" + keyToAdd : "" + keyToAdd);
    }

    /**
     * Creates a new child key of this key. This is just an alias for addToKey(key)!
     *
     * @param key The child key part.
     */
    public ConfigValueKey createChildKey(String key) {
        return addToKey(key);
    }

    /**
     * @return The length of the key.
     */
    public int length() {
        return this.key.length();
    }

    /**
     * @return The String value of this object.
     */
    @Override
    public String toString() {
        return this.key;
    }

    /**
     * Compare with other object for equality.
     *
     * @param obj The object to compare to.
     *
     * @return true if equal, false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ConfigValueKey)) {
            return false;
        }

        return this.key.equals(((ConfigValueKey)obj).key);
    }

    /**
     * @return The hash code of this object.
     */
    @Override
    public int hashCode() {
        return this.key.hashCode();
    }
}
