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
 *         2018-05-26: Created!
 *
 */
package se.natusoft.osgi.aps.api.core.config;

import se.natusoft.osgi.aps.types.APSHandler;

import java.util.Map;

/**
 * This represents a JSON configuration using Map & List to represent a JSON Object and JSON Array.
 *
 * "Struct path"s are paths that are separated by dots ('.') where the first part is a key in a Map
 * and the part after the dot is a key in the object returned for the first key, and so on. For List
 * objects and index in the form of '.[i].'. Note that the index is a path "part" in itself.
 *
 * Maps are used to represent JSON structure and the "Struct path"s are just a way to provide a
 * reference to values within the structure.
 *
 * There is a "StructMap" class in aps-core-lib that can be used to failure these "struct path"s
 * in implementations. Also if you make a "struct path" reference to a Map and not a end value
 * then this result can be wrapped with a StructMap and from there be accessed just like the
 * lookup path, but relative to this map.
 */
@SuppressWarnings("unused")
public interface APSConfig extends Map<String, Object> {

    /** Cluster event address to use for config events. */
    String CONFIG_EVENT_DESTINATION = "all:aps.config.events";

    /**
     * Calls the provided handler for each value path in the map.
     *
     * This provides paths to all values available in the structure.
     *
     * @param pathHandler The handler to call with value paths.
     */
    void withStructPath(APSHandler<String> pathHandler);

    /**
     * Looks up the value of a specified struct Path. Null or blank will return the whole root config Map.
     *
     * @param structPath The structPath to lookup.
     * @param valueHandler The handler receiving the looked up value.
     */
    void lookupr( String structPath, APSHandler<Object> valueHandler);

    /**
     * Returns the value at the specified struct path.
     *
     * @param structPath The struct path to lookup.
     *
     * @return value or null.
     */
    Object lookup(String structPath);

    /**
     * provides a new value.
     *
     * @param structPath The value path.
     * @param value The value.
     */
    void provide( String structPath, Object value );
}
