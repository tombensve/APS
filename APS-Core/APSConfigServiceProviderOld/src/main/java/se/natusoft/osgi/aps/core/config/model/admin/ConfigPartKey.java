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

import se.natusoft.osgi.aps.api.core.configold.model.admin.APSConfigEnvironment;
import static se.natusoft.osgi.aps.core.config.model.StaticUtils.*;

/**
 * This class manages a keyTemplate for a configuration part.
 *
 * A configuratoin part is what it sounds like, a part of a complete key representing a branch of a tree structure.
 * These keys are stored in a sequential list by ConfigValueKey, which uses them to build a complete key for referencing
 * a value.
 *
 * Please note that the OSGi standard configuration keys are not as flexible as java Properties or HashMap.
 * They only allow: [a-z][A-Z][0-9][-][_][.]. The values added to the keyTemplate by this class is compliant with
 * that. This class however takes no responsibility for the keyTemplate values passed to it!
 */
public class ConfigPartKey {
    //
    // Constants
    //

    private static final String INDEX = "${index}";
    private static final String CONFIG_ENV = "${config_env}";
    private static final String CONFIG_ID = "${config_id}";
    private static final String VERSION = "${version}";
    private static final String NAME = "${name}";
    public static final String _ = "_";

    public static final String NO_INDEX = "";
    public static final String NO_CONFIG_ENV = "";

    /**
     * The type of keyTemplate this is.
     */
    public static enum KeyType {
        CONFIG(CONFIG_ID + _ + VERSION + _ + NAME + _ + CONFIG_ENV + _ + INDEX),
        VALUE(NAME + _ + CONFIG_ENV + _ + INDEX);

        private String key;

        KeyType(String key) {
            this.key = key;
        }

        public String getKey() {
            return this.key;
        }
    }

    //
    // Private Members
    //

    /** The key template. */
    private String keyTemplate = null;

    /** The configold environemnt specificness of the key. */
    private boolean configEnvSpecific = false;

    /** The manyness of the key. */
    private boolean isMany = false;

    /** The configuration id part of the key. */
    private String configId = "";

    /** The version part of the key. */
    private String version = "";

    /** The name part of the key. */
    private String name = "";

    /** The configuraiton environment part of the key. */
    private String configEnv = "";

    /** The index part of the key. */
    private String index = NO_INDEX;

    //
    // Constructors
    //

    /**
     * Creates a new ConfigValueKey.
     */
    public ConfigPartKey(KeyType keyType) {
        this.keyTemplate = keyType.getKey();
    }

    /**
     * Creates a copy of a ConfigPartKey for internal use.
     *
     * @param keyToCopy The key to copy.
     */
    private ConfigPartKey(ConfigPartKey keyToCopy) {
        this.keyTemplate = keyToCopy.keyTemplate;
        this.configEnvSpecific = keyToCopy.configEnvSpecific;
        this.isMany = keyToCopy.isMany;
        this.configId = keyToCopy.configId;
        this.version = keyToCopy.version;
        this.name = keyToCopy.name;
        this.configEnv = keyToCopy.configEnv;
        this.index = keyToCopy.index;
    }

    //
    // Methods
    //

    /**
     * Returns an identical copy of this key.
     */
    public ConfigPartKey copy() {
        return new ConfigPartKey(this);
    }

    /**
     * Sets the configold environment specificness. If not configold environment specific then the configold environment
     * is cleared.
     *
     * NOTE: This needs to be set before the key is used!
     *
     * @param configEnvSpecific The configold environment specificness to set.
     */
    public ConfigPartKey configEnvSpecific(boolean configEnvSpecific) {
        this.configEnvSpecific = configEnvSpecific;
        return this;
    }

    /**
     * Sets the manyness of this key.
     *
     * NOTE: This needs to be set before the key is used!
     *
     * @param many The manyness to set.
     */
    public ConfigPartKey many(boolean many) {
        this.isMany = many;
        return this;
    }

    /**
     * Returns the manyness of this key.
     */
    public boolean isMany() {
        return this.isMany;
    }

    /**
     * Produces the full key as a String.
     */
    private String buildKey() {
        return this.keyTemplate
                .replace(CONFIG_ID, this.configId)
                .replace(VERSION, this.version)
                .replace(NAME, this.name)
                .replace(CONFIG_ENV, this.configEnvSpecific ? this.configEnv : "")
                .replace(INDEX, /*this.isMany &&*/ !this.index.equals(NO_INDEX) ? this.index : "");
                // TODO: Hmm ... "this.isMany &&" might not do any diff, but it might not hurt either.
    }

    /**
     * Sets the configold ID part of the key.
     *
     * @param configId The configold ID to set.
     *
     * @return this.
     */
    public ConfigPartKey configId(String configId) {
        this.configId = nullSafe(configId);
        return this;
    }

    /**
     * Sets the version part of the key.
     *
     * @param version The version to set.
     *
     * @return this.
     */
    public ConfigPartKey version(String version) {
        this.version = nullSafe(version);
        return this;
    }

    /**
     * Sets the name part of the key.
     *
     * @param name The name to set.
     *
     * @return this.
     */
    public ConfigPartKey name(String name) {
        this.name = nullSafe(name);
        return this;
    }

    /**
     * Sets the configold environment part of the key.
     *
     * @param configEnvironment The configuration environment to set.
     */
    public ConfigPartKey configEnv(String configEnvironment) {
        this.configEnv = nullSafe(configEnvironment);
        return this;
    }

    /**
     * Sets the configold environment part of the key.
     *
     * @param configEnvironment The configuration environment to set.
     */
    public ConfigPartKey configEnv(APSConfigEnvironment configEnvironment) {
        return configEnv(configEnvironment.getName());
    }

    /**
     * Clears the configuration environment from the key.
     */
    public ConfigPartKey clearConfigEnv() {
        this.configEnv = NO_CONFIG_ENV;
        return this;
    }

    /**
     * Adds an index to the key.
     *
     * @param index The index to add.
     */
    public ConfigPartKey index(int index) {
        if (index >= 0) {
            this.index = "" + index;
        }
        else {
            this.index = NO_INDEX;
        }
        return this;
    }

    /**
     * Clears the index from the key if any.
     */
    public ConfigPartKey clearIndex() {
        this.index = NO_INDEX;
        return this;
    }

    /**
     * Returns the keyTemplate as a String ignoring configuration environment and index.
     */
    public String getKey() {
        return buildKey();
    }

    /**
     * Returns the index.
     */
    public int getIndex() {
        try {
            return Integer.valueOf(this.index);
        }
        catch (NumberFormatException nfe) {
            return 0;
        }
    }

    /**
     * @return The length of the keyTemplate.
     */
    public int length() {
        return this.keyTemplate.length();
    }

    /**
     * @return The String value of this object.
     */
    @Override
    public String toString() {
        return getKey();
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
        if (!(obj instanceof ConfigPartKey)) {
            return false;
        }

        return this.keyTemplate.equals(((ConfigPartKey)obj).keyTemplate);
    }

    /**
     * @return The hash code of this object.
     */
    @Override
    public int hashCode() {
        return this.keyTemplate.hashCode();
    }
}
