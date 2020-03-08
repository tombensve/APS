/*
 *
 * PROJECT
 *     Name
 *         APS Core Lib
 *
 *     Code Version
 *         1.0.0
 *
 *     Description
 *         This library is made in Groovy and thus depends on Groovy, and contains functionality that
 *         makes sense for Groovy, but not as much for Java.
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
 *         2019-08-17: Created!
 *
 */
package se.natusoft.osgi.aps.core.lib

import groovy.transform.CompileStatic
import se.natusoft.osgi.aps.util.APSJson

/**
 * This is a utility that loads MapJson files and returns as Map<String, Object>.
 *
 * This supports values like "!@INCLUDE@:some/dir/something.json" that will be read as resource
 * from classpath and parsed, and inserted as value. In other words, an include.
 *
 * The '!@INCLUDE@:' path prefix is to make it somewhat unique to avoid misinterpreting values.
 */
@CompileStatic
class MapJsonLoader {

    /**
     * Values of "==>key" will be replaced with value of key.
     *
     * These will be loaded from messages/NamedRules.json on first use.
     */
    private static Map<String, String> STATIC_RULES = null

    private static void loadNamedRules(ClassLoader classLoader) {
        try {
            STATIC_RULES =
                    APSJson.readObject( classLoader.getResourceAsStream( "aps/messages/NamedRules.json" ) ) as Map<String, String>
        }
        catch ( Exception e ) {
            e.printStackTrace( System.err )
        }
    }

    /**
     * Loads a full schema including !@INCUDE@: referenced files.
     *
     * @param resourcePath The full resource path of the schema to loadMapJson.
     * @param classLoader The ClassLoader to use.
     *
     * @return Fully loaded schema.
     */
    static Map<String, Object> loadMapJson( String resourcePath, ClassLoader classLoader ) {

        Map<String, Object> json = APSJson.readObject( classLoader.getResourceAsStream( resourcePath ) )

        scanMapForIncludes( json, classLoader )

        json
    }

    /**
     * Handles loading of any includes.
     *
     * @param map The Map to scan for includes.
     * @param classLoader The ClassLoader used for loading included resources.
     */
    private static void scanMapForIncludes( Map<String, Object> map, ClassLoader classLoader ) {

        map.each { String key, Object value ->

            if ( value instanceof String && ( value as String ).startsWith( "!@INCLUDE@:" ) ) {

                String path = ( value as String ).substring( 11 )
                Map<String, Object> include = loadMapJson( path, classLoader )
                map[ key ] = include
            }
            else if ( value instanceof String && ( value as String ).startsWith( "NamedRule:" ) ) {

                if (STATIC_RULES == null) {
                    loadNamedRules( classLoader )
                }

                value = value.substring( 10 )
                value = STATIC_RULES[ value ]
                if (value != null) {
                    map[ key ] = value
                }
            }
            else if ( value instanceof Map ) {

                scanMapForIncludes( map[ key ] as Map<String, Object>, classLoader )
            }
        }
    }

}
