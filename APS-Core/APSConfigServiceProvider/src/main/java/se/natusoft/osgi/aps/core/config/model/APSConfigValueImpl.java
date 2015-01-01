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
 *         2011-07-22: Created!
 *
 */
package se.natusoft.osgi.aps.core.config.model;

import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigEnvironment;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigReference;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigValueEditModel;
import se.natusoft.osgi.aps.core.config.model.admin.APSConfigValueEditModelImpl;
import se.natusoft.osgi.aps.exceptions.APSRuntimeException;
import static se.natusoft.osgi.aps.core.config.model.StaticUtils.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This represents a configuration value.
 */
public class APSConfigValueImpl implements APSConfigValue, APSConfigRefConsumer {
    //
    // Private Members
    //

    /** The configuration definition model representing this value. */
    private APSConfigValueEditModelImpl configValueEditModel = null;

    /** The configuration values to get our configuration value from. */
    private ConfigValueStoreProvider configValuesProvider = null;

    /** Provides the active config environment. */
    private ConfigEnvironmentProvider configEnvProvider = null;

    /** The reference to the config value represented by this instance. */
    private APSConfigReference ref;


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
        this.configValueEditModel = (APSConfigValueEditModelImpl)configValueEditModel;
        this.configValuesProvider = configValuesProvider;
        this.configEnvProvider = configEnvProvider;
    }

    /**
     * Creates a new APSConfigValueImpl instance.
     *
     * @param configValueEditModel The configuration definition model representing this value.
     * @param configValuesProvider Provides configuration value store.
     * @param configEnvProvider Provides the currently active configuration environment.
     * @param ref The config reference to use for this instance.
     */
    /*package*/ APSConfigValueImpl(APSConfigValueEditModel configValueEditModel,
                              ConfigValueStoreProvider configValuesProvider,
                              ConfigEnvironmentProvider configEnvProvider,
                              APSConfigReference ref) {
        this(configValueEditModel, configValuesProvider, configEnvProvider);
        this.ref = ref;
    }

    //
    // Methods
    //

    /**
     * Ensures and returns a reference to the config value represented by this instance.
     */
    private APSConfigReference getRef() {
        this.ref = ensureRef(this.ref, this.configValueEditModel);
        return this.ref;
    }

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
        return this.configValuesProvider.
            getConfigValueStore().
            getConfigValue(
                    getRef().index(ix)._(configEnvironment).toString()
            );
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
     * @param configEnvironment The configuration environment to get value for.
     */
    private String getNormalValue(APSConfigEnvironment configEnvironment) {
        return this.configValuesProvider.getConfigValueStore().
                getConfigValue(getRef()._(configEnvironment).toString());
    }

    /**
     * Returns a normal non indexed value.
     *
     * @param configEnvironment The configuration environment to get value for.
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
    public String getString() {
        return safeGetValue("");
    }

    /**
     * Returns the value is a byte.
     */
    @Override
    public byte getByte() {
        return Byte.valueOf(safeGetValue("0"));
    }

    /**
     * Returns the value as a short.
     */
    @Override
    public short getShort() {
        return Short.valueOf(safeGetValue("0"));
    }

    /**
     * Returns the value as an int.
     */
    @Override
    public int getInt() {
        return Integer.valueOf(safeGetValue("0"));
    }

    /**
     * Returns the value as a long.
     */
    @Override
    public long getLong() {
        return Long.valueOf(safeGetValue("0"));
    }

    /**
     * Returns the value as a float.
     */
    @Override
    public float getFloat() {
        return Float.valueOf(safeGetValue("0.0"));
    }

    /**
     * Returns the value as a double.
     */
    @Override
    public double getDouble() {
        return Double.valueOf(safeGetValue("0.0"));
    }

    /**
     * Returns the value as a boolean.
     */
    @Override
    public boolean getBoolean() {
        return Boolean.valueOf(safeGetValue("false"));
    }

    /**
     * Returns the value as a Date.
     *
     * @throws APSRuntimeException on no specified date pattern or no valid date in config.
     */
    @Override
    public Date getDate() {
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
    public String getString(String configEnvironment) {
        return safeGetValue("", configEnvironment);
    }

    /**
     * Returns the value is a byte.
     */
    @Override
    public byte getByte(String configEnvironment) {
        return Byte.valueOf(safeGetValue("0", configEnvironment));
    }

    /**
     * Returns the value as a short.
     */
    @Override
    public short getShort(String configEnvironment) {
        return Short.valueOf(safeGetValue("0", configEnvironment));
    }

    /**
     * Returns the value as an int.
     */
    @Override
    public int getInt(String configEnvironment) {
        return Integer.valueOf(safeGetValue("0", configEnvironment));
    }

    /**
     * Returns the value as a long.
     */
    @Override
    public long getLong(String configEnvironment) {
        return Long.valueOf(safeGetValue("0", configEnvironment));
    }

    /**
     * Returns the value as a float.
     */
    @Override
    public float getFloat(String configEnvironment) {
        return Float.valueOf(safeGetValue("0.0", configEnvironment));
    }

    /**
     * Returns the value as a double.
     */
    @Override
    public double getDouble(String configEnvironment) {
        return Double.valueOf(safeGetValue("0.0", configEnvironment));
    }

    /**
     * Returns the value as a boolean.
     */
    @Override
    public boolean getBoolean(String configEnvironment) {
        return Boolean.valueOf(safeGetValue("false", configEnvironment));
    }

    /**
     * Returns the value as a Date.
     *
     * @throws APSRuntimeException on no specified date pattern or no valid date in config.
     */
    @Override
    public Date getDate(String configEnvironment) {
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

    /**
     * Returns true if value is empty.
     */
    @Override
    public boolean isEmpty() {
        return safeGetValue("").trim().length() == 0;
    }

    /**
     * Returns true if value is empty.
     */
    @Override
    public boolean isEmpty(String configEnvironment) {
        return safeGetValue("", configEnvironment).trim().length() == 0;
    }

    /**
     * Receives a config value reference. This implements APSConfigRefConsumer.
     *
     * __NOTE__: This is only for providing config class fields with a reference. It should not be used to set
     * the reference in any other case since it will add its edit model to the received reference. This class also
     * has a constructor variant that takes a ref which is used to create an indexed instance. This ref is just
     * set as is and can be used for other cases when a ref needs to be provided as is.
     *
     * @param ref The reference received.
     */
    @Override
    public void setConfigReference(APSConfigReference ref) {
        this.ref = ref._(this.configValueEditModel);
    }

    //
    // Deprecated APIs
    //

    /**
     * Returns the value as a String.
     */
    @Override
    @Deprecated
    public String toString() {
        return getString();
    }

    /**
     * Returns the value is a byte.
     */
    @Override
    @Deprecated
    public byte toByte() {
        return getByte();
    }

    /**
     * Returns the value as a short.
     */
    @Override
    @Deprecated
    public short toShort() {
        return getShort();
    }

    /**
     * Returns the value as an int.
     */
    @Override
    @Deprecated
    public int toInt() {
        return getInt();
    }

    /**
     * Returns the value as a long.
     */
    @Override
    @Deprecated
    public long toLong() {
        return getLong();
    }

    /**
     * Returns the value as a float.
     */
    @Override
    @Deprecated
    public float toFloat() {
        return getFloat();
    }

    /**
     * Returns the value as a double.
     */
    @Override
    @Deprecated
    public double toDouble() {
        return getDouble();
    }

    /**
     * Returns the value as a boolean.
     */
    @Override
    @Deprecated
    public boolean toBoolean() {
        return getBoolean();
    }

    /**
     * Returns the value as a Date.
     *
     * @throws APSRuntimeException on no specified date pattern or no valid date in config.
     */
    @Override
    @Deprecated
    public Date toDate() {
        return getDate();
    }

    /**
     * Returns the value as a String.
     */
    @Override
    @Deprecated
    public String toString(String configEnvironment) {
        return getString(configEnvironment);
    }

    /**
     * Returns the value is a byte.
     */
    @Override
    @Deprecated
    public byte toByte(String configEnvironment) {
        return getByte(configEnvironment);
    }

    /**
     * Returns the value as a short.
     */
    @Override
    @Deprecated
    public short toShort(String configEnvironment) {
        return getShort(configEnvironment);
    }

    /**
     * Returns the value as an int.
     */
    @Override
    @Deprecated
    public int toInt(String configEnvironment) {
        return getInt(configEnvironment);
    }

    /**
     * Returns the value as a long.
     */
    @Override
    @Deprecated
    public long toLong(String configEnvironment) {
        return getLong(configEnvironment);
    }

    /**
     * Returns the value as a float.
     */
    @Override
    @Deprecated
    public float toFloat(String configEnvironment) {
        return getFloat(configEnvironment);
    }

    /**
     * Returns the value as a double.
     */
    @Override
    @Deprecated
    public double toDouble(String configEnvironment) {
        return getDouble(configEnvironment);
    }

    /**
     * Returns the value as a boolean.
     */
    @Override
    @Deprecated
    public boolean toBoolean(String configEnvironment) {
        return getBoolean(configEnvironment);
    }

    /**
     * Returns the value as a Date.
     *
     * @throws APSRuntimeException on no specified date pattern or no valid date in config.
     */
    @Override
    @Deprecated
    public Date toDate(String configEnvironment) {
        return getDate(configEnvironment);
    }

}
