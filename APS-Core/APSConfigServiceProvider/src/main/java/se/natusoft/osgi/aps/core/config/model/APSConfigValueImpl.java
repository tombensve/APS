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
 *
 */
package se.natusoft.osgi.aps.core.config.model;

import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigEnvironment;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigValueEditModel;
import se.natusoft.osgi.aps.exceptions.APSRuntimeException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This represents a configuration value.
 */
public class APSConfigValueImpl implements APSConfigValue {
    //
    // Private Members
    //

    /** The configuration definition model representing this value. */
    private APSConfigValueEditModel configValueEditModel = null;

    /** The configuration values to get our configuration value from. */
    private ConfigValueStoreProvider configValuesProvider = null;

    /** Provides the active config environment. */
    private ConfigEnvironmentProvider configEnvProvider = null;

    //
    // Constructors
    //

    /**
     * Creates a new APSConfigValueImpl instance.
     *
     * @param configValueEditModel The configuration definition model representing this value.
     * @param configValuesProvider Provides configuration value store.
     * @param configEnvProvider Provides the currently active configuration environment.
     */
    public APSConfigValueImpl(APSConfigValueEditModel configValueEditModel,
                              ConfigValueStoreProvider configValuesProvider,
                              ConfigEnvironmentProvider configEnvProvider) {
        this.configValueEditModel = configValueEditModel;
        this.configValuesProvider = configValuesProvider;
        this.configEnvProvider = configEnvProvider;
    }


    //
    // Methods
    //

    /**
     * Returns the resolved value.
     */
    protected String getValue() {
        return getNormalValue(this.configEnvProvider.getActiveConfigEnvironment());
    }

    /**
     * Returns the resolved value.
     *
     * @param configEnvironment The config environment to get the value for.
     */
    protected String getValue(String configEnvironment) {
        return getNormalValue(configEnvironment);
    }

    /**
     * The specified value is actually a list of values so an index of the actual value needs to be fetched.
     *
     * @param ix The index of the value to get.
     */
    protected String getIndexedValue(int ix, APSConfigEnvironment configEnvironment) {
        return this.configValuesProvider.getConfigValueStore().
                getConfigValue(this.configValueEditModel.getKey(configEnvironment, ix));
    }

    /**
     * The specified value is actually a list of values so an index of the actual value needs to be fetched.
     *
     * @param ix The index of the value to get.
     */
    protected String getIndexedValue(int ix, String configEnvironemnt) {
        return getIndexedValue(ix, this.configEnvProvider.getConfigEnvironmentByName(configEnvironemnt));
    }

    /**
     * The specified value is actually a list of values so an index of the actual value needs to be fetched.
     *
     * @param ix The index of the value to get.
     */
    protected String getIndexedValue(int ix) {
        return getIndexedValue(ix, this.configEnvProvider.getActiveConfigEnvironment());
    }

    /**
     * Returns a normal non indexed value.
     *
     * @oaram configEnvironment The configuration environment to get value for.
     */
    private String getNormalValue(APSConfigEnvironment configEnvironment) {
        return this.configValuesProvider.getConfigValueStore().
                getConfigValue(this.configValueEditModel.getKey(configEnvironment));
    }

    /**
     * Returns a normal non indexed value.
     *
     * @oaram configEnvironment The configuration environment to get value for.
     */
    private String getNormalValue(String configEnvironment) {
        APSConfigEnvironment confEnv = this.configEnvProvider.getConfigEnvironmentByName(configEnvironment);
        // If asked for environment does not exist, fall back on default.
        if (confEnv == null) {
            confEnv = this.configEnvProvider.getActiveConfigEnvironment();
        }
        return getNormalValue(confEnv);
    }

    /**
     * Gets the value null safe.
     *
     * @param defaultValue The default value to use if no other default value is available.
     * @param value an ingoing value to start with.
     *
     * @return A value guaranteed.
     */
    private String _safeGetValue(String defaultValue, String value) {
        if (value == null) {
            value = this.configValueEditModel.getDefaultValue(this.configEnvProvider.getActiveConfigEnvironment());
        }
        if (value == null) {
            value = defaultValue;
        }

        return value;
    }

    /**
     * Gets the value null safe.
     *
     * @param defaultValue The default value to use if no other default value is available.
     * @param configEnvironment The config environment to get value for.
     *
     * @return A value guaranteed.
     */
    private String safeGetValue(String defaultValue, String configEnvironment) {
        return _safeGetValue(defaultValue, getValue(configEnvironment));
    }

    /**
     * Gets the value null safe.
     *
     * @param defaultValue The default value to use if no other default value is available.
     *
     * @return A value guaranteed.
     */
    private String safeGetValue(String defaultValue) {
        return _safeGetValue(defaultValue, getValue());
    }


    /**
     * Returns the value as a String.
     */
    @Override
    public String toString() {
        return safeGetValue("");
    }

    /**
     * Returns the value is a byte.
     */
    @Override
    public byte toByte() {
        return Byte.valueOf(safeGetValue("0"));
    }

    /**
     * Returns the value as a short.
     */
    @Override
    public short toShort() {
        return Short.valueOf(safeGetValue("0"));
    }

    /**
     * Returns the value as an int.
     */
    @Override
    public int toInt() {
        return Integer.valueOf(safeGetValue("0"));
    }

    /**
     * Returns the value as a long.
     */
    @Override
    public long toLong() {
        return Long.valueOf(safeGetValue("0"));
    }

    /**
     * Returns the value as a float.
     */
    @Override
    public float toFloat() {
        return Float.valueOf(safeGetValue("0.0"));
    }

    /**
     * Returns the value as a double.
     */
    @Override
    public double toDouble() {
        return Double.valueOf(safeGetValue("0.0"));
    }

    /**
     * Returns the value as a boolean.
     */
    @Override
    public boolean toBoolean() {
        return Boolean.valueOf(safeGetValue("false"));
    }

    /**
     * Returns the value as a Date.
     *
     * @throws APSRuntimeException on no specified date pattern or no valid date in config.
     */
    @Override
    public Date toDate() {
        if (this.configValueEditModel.getDatePattern() == null) {
            throw new APSRuntimeException("Trying to convert value to a Date object without any specification of date format!");
        }
        SimpleDateFormat sdf = new SimpleDateFormat(this.configValueEditModel.getDatePattern());
        try {
            return sdf.parse(safeGetValue(""));
        } catch (ParseException ex) {
            throw new APSRuntimeException(ex.getMessage(), ex);
        }
    }

    /**
     * Returns the value as a String.
     */
    @Override
    public String toString(String configEnvironment) {
        return safeGetValue("", configEnvironment);
    }

    /**
     * Returns the value is a byte.
     */
    @Override
    public byte toByte(String configEnvironment) {
        return Byte.valueOf(safeGetValue("0", configEnvironment));
    }

    /**
     * Returns the value as a short.
     */
    @Override
    public short toShort(String configEnvironment) {
        return Short.valueOf(safeGetValue("0", configEnvironment));
    }

    /**
     * Returns the value as an int.
     */
    @Override
    public int toInt(String configEnvironment) {
        return Integer.valueOf(safeGetValue("0", configEnvironment));
    }

    /**
     * Returns the value as a long.
     */
    @Override
    public long toLong(String configEnvironment) {
        return Long.valueOf(safeGetValue("0", configEnvironment));
    }

    /**
     * Returns the value as a float.
     */
    @Override
    public float toFloat(String configEnvironment) {
        return Float.valueOf(safeGetValue("0.0", configEnvironment));
    }

    /**
     * Returns the value as a double.
     */
    @Override
    public double toDouble(String configEnvironment) {
        return Double.valueOf(safeGetValue("0.0", configEnvironment));
    }

    /**
     * Returns the value as a boolean.
     */
    @Override
    public boolean toBoolean(String configEnvironment) {
        return Boolean.valueOf(safeGetValue("false", configEnvironment));
    }

    /**
     * Returns the value as a Date.
     *
     * @throws APSRuntimeException on no specified date pattern or no valid date in config.
     */
    @Override
    public Date toDate(String configEnvironment) {
        if (this.configValueEditModel.getDatePattern() == null) {
            throw new APSRuntimeException("Trying to convert value to a Date object without any specification of date format!");
        }
        SimpleDateFormat sdf = new SimpleDateFormat(this.configValueEditModel.getDatePattern());
        try {
            return sdf.parse(safeGetValue("", configEnvironment));
        } catch (ParseException ex) {
            throw new APSRuntimeException(ex.getMessage(), ex);
        }
    }

    //
    // Inner Classes
    //

    /**
     * This represents one index of a list of configuration values.
     */
    public static class APSConfigIndexedValueImpl extends APSConfigValueImpl {
        //
        // Private Members
        //

        /** The index of the specified instance. */
        private int index = -1;

        //
        // Constructors
        //

        /**
         * Creates a new APSConfigValueImpl instance.
         *
         * @param configDefinitionEditModel The configuration definition model representing this value.
         * @param configValuesProvider Provides configuration value store.
         * @param configEnvProvider Provides the currently active configuration environment.
         * @param index The index represented by this instance.
         */
        public APSConfigIndexedValueImpl(APSConfigValueEditModel configDefinitionEditModel,
                                  ConfigValueStoreProvider configValuesProvider,
                                  ConfigEnvironmentProvider configEnvProvider,
                                  int index) {
            super(configDefinitionEditModel, configValuesProvider, configEnvProvider);
            this.index = index;
        }

        //
        // Methods
        //

        /**
         * Returns the resolved value.
         */
        @Override
        protected String getValue() {
            return getIndexedValue(this.index);
        }

        /**
         * Returns the resolved value.
         *
         * @param configEnvironment The config enviornment to get value for.
         */
        @Override
        protected String getValue(String configEnvironment) {
            return getIndexedValue(this.index, configEnvironment);
        }
    }
}
