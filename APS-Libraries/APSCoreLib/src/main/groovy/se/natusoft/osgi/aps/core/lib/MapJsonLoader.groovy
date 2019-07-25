package se.natusoft.osgi.aps.core.lib

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
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
@TypeChecked
class MapJsonLoader {

    /**
     * Loads a full schema including !@INCUDE@: referenced files.
     *
     * @param resourcePath The full resource path of the schema to loadMapJson.
     * @param classLoader The ClassLoader to use.
     *
     * @return Fully loaded schema.
     */
    static Map<String, Object> loadMapJson( String resourcePath, ClassLoader classLoader) {

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
    private static void scanMapForIncludes(Map<String, Object> map, ClassLoader classLoader) {

        map.each { String key , Object value ->

            if (value instanceof String && (value as String).startsWith( "!@INCLUDE@:" )) {

                String path = (value as String).substring( 11 )
                Map<String, Object> include = loadMapJson( path, classLoader )
                map[key] = include
            }
            else if (value instanceof Map) {

                scanMapForIncludes( map[key] as Map<String, Object>, classLoader )
            }
        }
    }

}
