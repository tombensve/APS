/* 
 * 
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         0.9.3
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
 *     Tommy Svensson (tommy@natusoft.se)
 *         Changes:
 *         2012-12-28: Created!
 *         
 */
package se.natusoft.osgi.aps.api.core.config.model.test;

import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue;

import java.util.Date;

/**
 * This implementation is only for faking a config in test.
 */
public class APSConfigValueTestImpl implements APSConfigValue {

    private String value = null;

    public APSConfigValueTestImpl(String value) {
        this.value = value;
    }

    /**
     * Returns the value as a boolean.
     */
    @Override
    public boolean toBoolean() {
        return Boolean.valueOf(this.value).booleanValue();
    }

    /**
     * Returns the value as a Date.
     */
    @Override
    public Date toDate() {
        return new Date();
    }

    /**
     * Returns the value as a double.
     */
    @Override
    public double toDouble() {
        return Double.valueOf(this.value).doubleValue();
    }

    /**
     * Returns the value as a float.
     */
    @Override
    public float toFloat() {
        return Float.valueOf(this.value).floatValue();
    }

    /**
     * Returns the value as an int.
     */
    @Override
    public int toInt() {
        return Integer.valueOf(this.value).intValue();
    }

    /**
     * Returns the value as a long.
     */
    @Override
    public long toLong() {
        return Long.valueOf(this.value).longValue();
    }

    /**
     * Returns the value is a byte.
     */
    @Override
    public byte toByte() {
        return Byte.valueOf(value).byteValue();
    }

    /**
     * Returns the value as a short.
     */
    @Override
    public short toShort() {
        return Short.valueOf(this.value).shortValue();
    }

    public String toString() {
        return this.value;
    }
}
