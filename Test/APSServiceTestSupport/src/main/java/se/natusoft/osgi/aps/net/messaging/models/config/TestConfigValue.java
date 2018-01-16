/*
 *
 * PROJECT
 *     Name
 *         APS Service Test Support
 *
 *     Code Version
 *         1.0.0
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
 *         2015-01-18: Created!
 *
 */
package se.natusoft.osgi.aps.net.messaging.models.config;

import se.natusoft.osgi.aps.api.core.configold.model.APSConfigValue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * An implementation of APSConfigValue to use for faking configold in tests.
 */
@SuppressWarnings("UnusedDeclaration")
public class TestConfigValue implements APSConfigValue {
    //
    // Properties
    //

    private String value;

    private String dateFormat = "yyyy/MM/dd HH:mm";

    //
    // Methods
    //

    public void setValue(String value) { this.value = value; }
    public String getValue() { return this.value; }

    public void setDateFormat(String dateFormat) { this.dateFormat = dateFormat; }
    public String getDateFormat() { return this.dateFormat; }

    /**
     * Returns the value as a String.
     */
    @Override
    public String getString() {
        return this.value;
    }

    /**
     * Returns the value as a boolean.
     */
    @Override
    public boolean getBoolean() {
        return Boolean.valueOf(this.value);
    }

    /**
     * Returns the value as a double.
     */
    @Override
    public double getDouble() {
        return Double.valueOf(this.value);
    }

    /**
     * Returns the value as a float.
     */
    @Override
    public float getFloat() {
        return Float.valueOf(this.value);
    }

    /**
     * Returns the value as an int.
     */
    @Override
    public int getInt() {
        return Integer.valueOf(this.value);
    }

    /**
     * Returns the value as a long.
     */
    @Override
    public long getLong() {
        return Long.valueOf(this.value);
    }

    /**
     * Returns the value is a byte.
     */
    @Override
    public byte getByte() {
        return Byte.valueOf(this.value);
    }

    /**
     * Returns the value as a short.
     */
    @Override
    public short getShort() {
        return Short.valueOf(this.value);
    }

    /**
     * Returns the value as a Date.
     */
    @Override
    public Date getDate() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(this.dateFormat);
            return sdf.parse(this.value);
        }
        catch (ParseException pe) {
            throw new RuntimeException("Failed to parse date value.", pe);
        }
    }

    /**
     * Returns the value as a String.
     *
     * @param configEnvironment The configold environment to get configold value for.
     */
    @Override
    public String getString(String configEnvironment) {
        return getString();
    }

    /**
     * Returns the value as a boolean.
     *
     * @param configEnvironment The configold environment to get configold value for.
     */
    @Override
    public boolean getBoolean(String configEnvironment) {
        return getBoolean();
    }

    /**
     * Returns the value as a double.
     *
     * @param configEnvironment The configold environment to get configold value for.
     */
    @Override
    public double getDouble(String configEnvironment) {
        return getDouble();
    }

    /**
     * Returns the value as a float.
     *
     * @param configEnvironment The configold environment to get configold value for.
     */
    @Override
    public float getFloat(String configEnvironment) {
        return getFloat();
    }

    /**
     * Returns the value as an int.
     *
     * @param configEnvironment The configold environment to get configold value for.
     */
    @Override
    public int getInt(String configEnvironment) {
        return getInt();
    }

    /**
     * Returns the value as a long.
     *
     * @param configEnvironment The configold environment to get configold value for.
     */
    @Override
    public long getLong(String configEnvironment) {
        return getLong();
    }

    /**
     * Returns the value is a byte.
     *
     * @param configEnvironment The configold environment to get configold value for.
     */
    @Override
    public byte getByte(String configEnvironment) {
        return getByte();
    }

    /**
     * Returns the value as a short.
     *
     * @param configEnvironment The configold environment to get configold value for.
     */
    @Override
    public short getShort(String configEnvironment) {
        return getShort();
    }

    /**
     * Returns the value as a Date.
     *
     * @param configEnvironment The configold environment to get configold value for.
     */
    @Override
    public Date getDate(String configEnvironment) {
        return getDate();
    }

    /**
     * Returns true if value is empty.
     */
    @Override
    public boolean isEmpty() {
        return this.value == null || this.value.isEmpty();
    }

    /**
     * Returns true if value is empty.
     */
    @Override
    public boolean isEmpty(String configEnvironment) {
        return isEmpty();
    }

    @Override
    /**
     * Returns the value as a String.
     */
    public String toString() {
        return this.value;
    }

    /**
     * Returns the value as a boolean.
     */
    @Override
    public boolean toBoolean() {
        return getBoolean();
    }

    /**
     * Returns the value as a double.
     */
    @Override
    public double toDouble() {
        return getDouble();
    }

    /**
     * Returns the value as a float.
     */
    @Override
    public float toFloat() {
        return getFloat();
    }

    /**
     * Returns the value as an int.
     */
    @Override
    public int toInt() {
        return getInt();
    }

    /**
     * Returns the value as a long.
     */
    @Override
    public long toLong() {
        return getLong();
    }

    /**
     * Returns the value is a byte.
     */
    @Override
    public byte toByte() {
        return getByte();
    }

    /**
     * Returns the value as a short.
     */
    @Override
    public short toShort() {
        return getShort();
    }

    /**
     * Returns the value as a Date.
     */
    @Override
    public Date toDate() {
        return getDate();
    }

    /**
     * Returns the value as a String.
     *
     * @param configEnvironment The configold environment to get configold value for.
     */
    @Override
    public String toString(String configEnvironment) {
        return toString();
    }

    /**
     * Returns the value as a boolean.
     *
     * @param configEnvironment The configold environment to get configold value for.
     */
    @Override
    public boolean toBoolean(String configEnvironment) {
        return toBoolean();
    }

    /**
     * Returns the value as a double.
     *
     * @param configEnvironment The configold environment to get configold value for.
     */
    @Override
    public double toDouble(String configEnvironment) {
        return toDouble();
    }

    /**
     * Returns the value as a float.
     *
     * @param configEnvironment The configold environment to get configold value for.
     */
    @Override
    public float toFloat(String configEnvironment) {
        return toFloat();
    }

    /**
     * Returns the value as an int.
     *
     * @param configEnvironment The configold environment to get configold value for.
     */
    @Override
    public int toInt(String configEnvironment) {
        return toInt();
    }

    /**
     * Returns the value as a long.
     *
     * @param configEnvironment The configold environment to get configold value for.
     */
    @Override
    public long toLong(String configEnvironment) {
        return toLong();
    }

    /**
     * Returns the value is a byte.
     *
     * @param configEnvironment The configold environment to get configold value for.
     */
    @Override
    public byte toByte(String configEnvironment) {
        return toByte();
    }

    /**
     * Returns the value as a short.
     *
     * @param configEnvironment The configold environment to get configold value for.
     */
    @Override
    public short toShort(String configEnvironment) {
        return toShort();
    }

    /**
     * Returns the value as a Date.
     *
     * @param configEnvironment The configold environment to get configold value for.
     */
    @Override
    public Date toDate(String configEnvironment) {
        return toDate();
    }
}
