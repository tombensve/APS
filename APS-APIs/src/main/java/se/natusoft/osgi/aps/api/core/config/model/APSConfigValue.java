/*
 *
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         0.10.0
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
     * Returns the value as a boolean.
     */
    boolean toBoolean();

    /**
     * Returns the value as a Date.
     */
    Date toDate();

    /**
     * Returns the value as a double.
     */
    double toDouble();

    /**
     * Returns the value as a float.
     */
    float toFloat();

    /**
     * Returns the value as an int.
     */
    int toInt();

    /**
     * Returns the value as a long.
     */
    long toLong();

    /**
     * Returns the value is a byte.
     */
    public byte toByte();

    /**
     * Returns the value as a short.
     */
    public short toShort();

    /**
     * Returns the value as a String.
     */
    @Override
    String toString();

}
