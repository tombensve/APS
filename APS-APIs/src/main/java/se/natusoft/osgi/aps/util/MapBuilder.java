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
package se.natusoft.osgi.aps.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Builds a simple map.
 */
public class MapBuilder {

    private Map<String, Object> map;
    private String key;

    public MapBuilder() {
        this.map = new LinkedHashMap<>(  );
    }

    public MapBuilder(Map<String, Object> map) {
        this.map = map;
    }

    /**
     * Builds a Map by taking key, value, key, value ... from an Object array.
     *
     * @param keyval The key value array.
     */
    private Map<String, Object> keyval( Object... keyval ) {

        int i = 0;
        while ( i < keyval.length ) {

            map.put( keyval[ i ].toString().replace( ":", "" ), keyval[ i + 1 ] );
            i += 2;
        }

        return map;
    }

    public MapBuilder k( String key ) {

        if ( this.key != null ) throw new IllegalStateException( "Expecting a key, not a value!" );

        this.key = key;
        return this;
    }

    public MapBuilder v( Object value ) {

        if ( this.key == null ) throw new IllegalStateException( "Expecting a value, not a key!" );

        this.map.put( this.key, value );
        this.key = null;
        return this;
    }

    public Map<String, Object> toMap() {

        return this.map;
    }

    public static Map<String, Object> map( Object... keyval ) {

        return new MapBuilder().keyval( keyval );
    }

    public static MapBuilder map() {

        return new MapBuilder();
    }
}
