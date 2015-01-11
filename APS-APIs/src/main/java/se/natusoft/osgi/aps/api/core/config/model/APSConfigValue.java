/*
 *
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         Provides the APIs for the application platform services.
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
 *         2011-08-13: Created!
 *
 */
package se.natusoft.osgi.aps.api.core.config.model;

import java.util.Date;

/**
 * This represents a configuration value.
 */
public interface APSConfigValue {

    /**
     * Returns the value as a String.
     */
    String getString();

    /**
     * Returns the value as a boolean.
     */
    boolean getBoolean();

    /**
     * Returns the value as a double.
     */
    double getDouble();

    /**
     * Returns the value as a float.
     */
    float getFloat();

    /**
     * Returns the value as an int.
     */
    int getInt();

    /**
     * Returns the value as a long.
     */
    long getLong();

    /**
     * Returns the value is a byte.
     */
    public byte getByte();

    /**
     * Returns the value as a short.
     */
    public short getShort();

    /**
     * Returns the value as a Date.
     */
    Date getDate();

    /**
     * Returns the value as a String.
     *
     * @param configEnvironment The config environment to get config value for.
     */
    String getString(String configEnvironment);

    /**
     * Returns the value as a boolean.
     *
     * @param configEnvironment The config environment to get config value for.
     */
    boolean getBoolean(String configEnvironment);

    /**
     * Returns the value as a double.
     *
     * @param configEnvironment The config environment to get config value for.
     */
    double getDouble(String configEnvironment);

    /**
     * Returns the value as a float.
     *
     * @param configEnvironment The config environment to get config value for.
     */
    float getFloat(String configEnvironment);

    /**
     * Returns the value as an int.
     *
     * @param configEnvironment The config environment to get config value for.
     */
    int getInt(String configEnvironment);

    /**
     * Returns the value as a long.
     *
     * @param configEnvironment The config environment to get config value for.
     */
    long getLong(String configEnvironment);

    /**
     * Returns the value is a byte.
     *
     * @param configEnvironment The config environment to get config value for.
     */
    public byte getByte(String configEnvironment);

    /**
     * Returns the value as a short.
     *
     * @param configEnvironment The config environment to get config value for.
     */
    public short getShort(String configEnvironment);

    /**
     * Returns the value as a Date.
     *
     * @param configEnvironment The config environment to get config value for.
     */
    Date getDate(String configEnvironment);


    /**
     * Returns true if value is empty.
     */
    boolean isEmpty();

    /**
     * Returns true if value is empty.
     */
    public boolean isEmpty(String configEnvironment);

    //
    // Deprecated
    //

    /**
     * Returns the value as a String.
     */
    @Override
    @Deprecated
    String toString();

    /**
     * Returns the value as a boolean.
     */
    @Deprecated
    boolean toBoolean();

    /**
     * Returns the value as a double.
     */
    @Deprecated
    double toDouble();

    /**
     * Returns the value as a float.
     */
    @Deprecated
    float toFloat();

    /**
     * Returns the value as an int.
     */
    @Deprecated
    int toInt();

    /**
     * Returns the value as a long.
     */
    @Deprecated
    long toLong();

    /**
     * Returns the value is a byte.
     */
    @Deprecated
    public byte toByte();

    /**
     * Returns the value as a short.
     */
    @Deprecated
    public short toShort();

    /**
     * Returns the value as a Date.
     */
    @Deprecated
    Date toDate();

    /**
     * Returns the value as a String.
     *
     * @param configEnvironment The config environment to get config value for.
     */
    @Deprecated
    String toString(String configEnvironment);

    /**
     * Returns the value as a boolean.
     *
     * @param configEnvironment The config environment to get config value for.
     */
    @Deprecated
    boolean toBoolean(String configEnvironment);

    /**
     * Returns the value as a double.
     *
     * @param configEnvironment The config environment to get config value for.
     */
    @Deprecated
    double toDouble(String configEnvironment);

    /**
     * Returns the value as a float.
     *
     * @param configEnvironment The config environment to get config value for.
     */
    @Deprecated
    float toFloat(String configEnvironment);

    /**
     * Returns the value as an int.
     *
     * @param configEnvironment The config environment to get config value for.
     */
    @Deprecated
    int toInt(String configEnvironment);

    /**
     * Returns the value as a long.
     *
     * @param configEnvironment The config environment to get config value for.
     */
    @Deprecated
    long toLong(String configEnvironment);

    /**
     * Returns the value is a byte.
     *
     * @param configEnvironment The config environment to get config value for.
     */
    @Deprecated
    public byte toByte(String configEnvironment);

    /**
     * Returns the value as a short.
     *
     * @param configEnvironment The config environment to get config value for.
     */
    @Deprecated
    public short toShort(String configEnvironment);

    /**
     * Returns the value as a Date.
     *
     * @param configEnvironment The config environment to get config value for.
     */
    @Deprecated
    Date toDate(String configEnvironment);
}
