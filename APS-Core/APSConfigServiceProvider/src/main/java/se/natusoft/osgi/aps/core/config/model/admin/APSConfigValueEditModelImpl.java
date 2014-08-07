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
 *         2011-08-06: Created!
 *         2012-02-13: Updated to reflect changes in APSConfigDefinitionValueModel.
 *
 */
package se.natusoft.osgi.aps.core.config.model.admin;

import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigEditModel;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigEnvironment;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigValueEditModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This represents an configuration definition value.
 */
public class APSConfigValueEditModelImpl implements APSConfigValueEditModel {
    //
    // Constants
    //

    /** Represents a non index. */
    public static final int NO_INDEX = -1;

    /** The default config environment when none have been given. */
    private static final APSConfigEnvironment DEFAULT_CONFIG_ENV = new APSConfigEnvironmentImpl("default", "default", 0);

    //
    // Private Members
    //

    /** The parent of this or null if top parent. */
    private APSConfigEditModel parent = null;

    /** The localKey for this value. */
    private ConfigPartKey key = null;

    /** The name of the value. This is basically the last part of the localKey. */
    private String name = null;

    /** The defaults for this value as a map. */
    private Map<APSConfigEnvironment, APSConfigDefaultValue> defaultValuesMap = new HashMap<>();

    /** The description of this value. */
    private String description = "";

    /** If true this represents an array of values. */
    private boolean isMany = false;

    /** The date pattern to use to parse date values. */
    private String datePattern = null;

    /** This should be true if the value is configuration environment specific. */
    private boolean configEnvSpecific = false;

    /** If true then this is a boolean value. */
    private boolean isBoolean = false;

    /** If non empty array then this contains the valid values for the value. */
    private String[] validValues = new String[0];

    //
    // Constructors
    //

    /**
     * Creates a new APSConfigValueModelImpl instance.
     *
     * @param name the name of the value.
     * @param parent The parent of this value.
     * @param defaultValues The default values for this value.
     */
    public APSConfigValueEditModelImpl(String name, APSConfigEditModel parent, List<APSConfigDefaultValue> defaultValues) {
        this(new ConfigPartKey(ConfigPartKey.KeyType.VALUE).name(name), name, parent, defaultValues);
    }

    /**
     * Creates a new APSConfigValueModelImpl instance.
     *
     * @param key The key for this value.
     * @param name the name of the value.
     * @param parent The parent of this value.
     * @param defaultValues The default values for this value.
     */
    protected APSConfigValueEditModelImpl(ConfigPartKey key, String name, APSConfigEditModel parent, List<APSConfigDefaultValue> defaultValues) {
        this.key = key;
        this.name = name;
        this.parent = parent;
        for (APSConfigDefaultValue defValue : defaultValues) {
            this.defaultValuesMap.put(defValue.getConfigEnv(), defValue);
        }
    }

    //
    // Methods
    //

    /**
     * Sets the description of this value.
     *
     * @param description The descripton to set.
     */
    protected void setDescription(String description) {
        this.description = description;
    }

    /**
     * The description of this value.
     *
     * @return the description
     */
    @Override
    public String getDescription() {
        return this.description;
    }

    /**
     * If true this represents an array of values.
     *
     * @return the isMany
     */
    @Override
    public boolean isMany() {
        return this.isMany;
    }

    /**
     * If true this represents an array of values.
     *
     * @param isMany the isMany to set
     */
    protected void setMany(boolean isMany) {
        this.isMany = isMany;
        if (this.key != null) {
            this.key.many(this.isMany());
        }
    }

    /**
     * The date pattern to use for parsing date values.
     *
     * @return The date pattern.
     */
    @Override
    public String getDatePattern() {
        return this.datePattern;
    }

    /**
     * Sets the date pattern to use to parse date values.
     *
     * @param datePattern The date pattern to set.
     */
    public void setDatePattern(String datePattern) {
        this.datePattern = datePattern;
    }

    /**
     * true if the value is a boolean type value (based on config class value annotation).
     *
     * @return true or false.
     */
    @Override
    public boolean isBoolean() {
        return isBoolean;
    }

    /**
     * Sets the 'isBoolean' state for this value.
     *
     * @param isBoolean The state to set.
     */
    public void setIsBoolean(boolean isBoolean) {
        this.isBoolean = isBoolean;
    }

    /**
     * If the size of this array > 0 then these are the valid values to set for this value.
     *
     * @return An emtpy array or set of valid values.
     */
    @Override
    public String[] getValidValues() {
        return this.validValues;
    }

    /**
     * Sets the valid values for this value.
     *
     * @param validValues The valid values to set.
     */
    public void setValidValues(String[] validValues) {
        this.validValues = validValues;
    }

    /**
     * The name of the value. This is basically the last part of the localKey.
     *
     * @return the name
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * The name of the value. This is basically the last part of the localKey.
     *
     * @param name the name to set
     */
    protected void setName(String name) {
        this.name = name;
    }

    /**
     * true if the value represented by this model is environment specific, false otherwise.
     *
     * @return true or false.
     */
    @Override
    public boolean isConfigEnvironmentSpecific() {
        return this.configEnvSpecific;
    }

    /**
     * Set to true if the value represented by tthis model is environment specific.
     *
     * @param configEnvSpecific The config env specificness to set.
     */
    public void setConfigEnvSpecific(boolean configEnvSpecific) {
        this.configEnvSpecific = configEnvSpecific;
        this.key.configEnvSpecific(configEnvSpecific);
    }

    /**
     * The parent of this or null if top parent.
     *
     * @return the parent
     */
    @Override
    public APSConfigEditModel getParent() {
        return this.parent;
    }

    /**
     * The localKey for this value.
     *
     * @return the key.
     */
    public ConfigPartKey getKey() {
        return this.key;
    }

    /**
     * The default for this value.
     *
     * @param configEnv The configuration environment to get the default value for.
     *
     * @return the defaultValue
     */
    @Override
    public String getDefaultValue(APSConfigEnvironment configEnv) {
        String value = null;
        APSConfigDefaultValue defaultValue = this.defaultValuesMap.get(configEnv);
        // When a config environment is not specified in the annotation it will be set to "default". This is
        // a fallback for when there is no default value for the current config environment, but one for "default".
        // That is, if only a default value for "default" is provided then it will apply for any config environment.
        if (defaultValue == null) {
            defaultValue = this.defaultValuesMap.get(DEFAULT_CONFIG_ENV);
        }
        if (defaultValue != null) {
            value = defaultValue.getValue();
        }

        return value;
    }

    /**
     * @return The hash code of this object.
     */
    @Override
    public int hashCode() {
        return this.key != null ? this.key.hashCode() : super.hashCode();
    }

    /**
     * Compares this object with another for equality.
     *
     * @param obj The object to compare to.
     *
     * @return true if they are both equal.
     */
    @Override
    public boolean equals(Object obj) {
        if (!APSConfigValueEditModelImpl.class.isAssignableFrom(obj.getClass())) {
            return false;
        }

        if (this.key != null) {
            return this.key.equals(((APSConfigValueEditModelImpl)obj).key);
        }

        return super.equals(obj);
    }
}
