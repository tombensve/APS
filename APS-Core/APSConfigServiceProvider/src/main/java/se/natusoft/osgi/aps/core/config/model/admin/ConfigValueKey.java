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
 *         2014-08-13: Created!
 *         
 */
package se.natusoft.osgi.aps.core.config.model.admin;

import se.natusoft.osgi.aps.api.core.config.service.APSConfigException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static se.natusoft.osgi.aps.core.config.model.admin.ConfigPartKey.NO_CONFIG_ENV;
import static se.natusoft.osgi.aps.core.config.model.admin.ConfigPartKey._;

/**
 * This represents one complete key pointing to a config value.
 *
 * This class is also responsible for creating "size" and "time" keys for "many" type size and timestamps
 * used for syncing.
 *
 * The basic key structure is handled by ConfigPartKey.
 *
 * @see se.natusoft.osgi.aps.core.config.model.admin.ConfigPartKey
 */
public class ConfigValueKey {

    //
    // Constants
    //

    /** Suffix for size keys. */
    public static final String SIZE = "SIZE";
    /** Suffix for timestamp keys. */
    public static final String TIME = "TIME";

    //
    // Private Members
    //

    /** The part keys for each node of the config. */
    private LinkedList<ConfigPartKey> parts = new LinkedList<>();

    /** The config environment. */
    private String configEnv;

    //
    // Constructors
    //

    /**
     * Crates a new ConfigValueKey from a set of ConfigPartKey instances.
     *
     * @param configEnv The configuration environment to build this key for.
     * @param keys The keys to combine.
     */
    public ConfigValueKey(String configEnv, ConfigPartKey... keys) {
        this.configEnv = configEnv;
        Collections.addAll(this.parts, keys);
    }

    /**
     * Crates a new ConfigValueKey from a set of ConfigPartKey instances.
     *
     * @param configEnv The configuration environment to build this key for.
     * @param keys The keys to combine.
     */
    public ConfigValueKey(String configEnv, List<ConfigPartKey> keys) {
        this(configEnv, convert(keys));
    }

    /**
     * Does the same as:
     *
     *     ConfigValueKey(null, key1, key2, ...);
     *
     * @param keys The keys to combine to one keyTemplate.
     */
    public ConfigValueKey(ConfigPartKey... keys) {
        this(NO_CONFIG_ENV, keys);
    }

    /**
     * Copy constructor.
     *
     * @param orig The original to copy.
     */
    private ConfigValueKey(ConfigValueKey orig) {
        for (ConfigPartKey cpk : orig.parts) {
            this.parts.add(cpk.copy());
        }

        this.configEnv = orig.configEnv;
    }

    //
    // Methods
    //

    /**
     * Returns a copy of this key.
     */
    public ConfigValueKey copy() {
        return new ConfigValueKey(this);
    }

    /**
     * Updates the index of the value part of this key.
     *
     * @param index The new index to set.
     */
    public ConfigValueKey index(int index) {
        this.parts.getLast().index(index);
        return this;
    }

    /**
     * Converts part key from a List to an Array.
     *
     * @param keys The keys to convert.
     */
    private static ConfigPartKey[] convert(List<ConfigPartKey> keys) {
        ArrayList<ConfigPartKey> kl;
        if (keys instanceof ArrayList) {
            kl = (ArrayList<ConfigPartKey>)keys;
        }
        else {
            kl = new ArrayList<>();
            kl.addAll(keys);
        }

        return kl.toArray(new ConfigPartKey[kl.size()]);
    }

    /**
     * Returns a new ConfigValueKey pointing to the specified index of a many value.
     *
     * @param index The index to get the key for.
     */
    public ConfigValueKey forIndex(int index) {
        if (!isMany()) {
            throw new APSConfigException("Bad call! Can't call forIndex(int) for a non list type value!");
        }

        return copy().index(index);
    }

    /**
     * Returns the index of the value.
     */
    public int getIndex() {
        if (!isMany()) {
            throw new APSConfigException("Bad call! Can't call getIndex() for a non list type value!");
        }
        return this.parts.getLast().getIndex();
    }

    /**
     * Returns the parts that where the sources to this full value key.
     */
    public List<ConfigPartKey> getSources() {
        return this.parts;
    }

    /**
     * Returns true if this key represents a many value.
     */
    public boolean isMany() {
        return this.parts.get(this.parts.size() - 1).isMany();
    }

    /**
     * Returns the key as a String.
     */
    public String getKey() {
        StringBuilder sb = new StringBuilder();
        String prefix = "";
        for (ConfigPartKey partKey : this.parts) {
            sb.append(prefix);
            sb.append(partKey.copy().configEnv(configEnv).getKey());
            prefix = _;
        }
        return sb.toString();
    }

    /**
     * Returns the key without an index.
     *
     * I decided that this was preferable to using a secondary copy of the unindexed key, and it is not possible
     * to determine if the key is indexed or not upon construction either so this needs to be done at some point
     * anyhow. If this is always done in real time you always get the correct value for the current state of the
     * key.
     */
    private String getUnIndexedKey() {
        String unIndexedKey = getKey();
        int ix = unIndexedKey.lastIndexOf('_');
        if (ix > 0 && unIndexedKey.length() > (ix + 1)) {
            // The new default value for index is "", but there might be some "-1" still hanging around!
            if (Character.isDigit(unIndexedKey.charAt(ix + 1)) || unIndexedKey.charAt(ix + 1) == '-') {
                unIndexedKey = unIndexedKey.substring(0, ix + 1);
            }
        }

        return unIndexedKey;
    }

    /**
     * Returns the size key for this key for when isMany()  returns true.
     */
    public String getSizeKey() {
        return getUnIndexedKey() + _ + SIZE;
    }

    /**
     * Returns the timestamp key for this key.
     */
    public String getTimeKey() {
        return getUnIndexedKey() + _ + TIME;
    }

    /**
     * Return this as a String.
     */
    public String toString() {
        return getKey();
    }

}
