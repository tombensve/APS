package se.natusoft.osgi.aps.core.lib

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.osgi.aps.exceptions.APSValidationException

/**
 * For the lack of a better name :-)
 *
 * This wraps a structured Map that looks like a JSON document, containing Map, List, and other 'Object's as values.
 *
 * A key is a String contining branches separated by '.' characters for each sub structure.
 *
 * It provides a method to collect all value referencing keys in the map structure with full key paths.
 *
 * It provides a lookup method that takes a full value key path and returns a value.
 *
 * Note that since this delegates to the wrapped Map the class is also a Map when compiled!
 *
 * Here is an example from the test that shows how to lookup and how to use the keys:
 *
 *         assert mapo.lookup( "header.type" ).toString() == "service"
 *         assert mapo.lookup( "header.address" ).toString() == "aps.admin.web"
 *         assert mapo.lookup( "header.classifier" ).toString() == "public"
 *         assert mapo.lookup( "body.action" ).toString() == "get-webs"
 *         assert mapo.lookup( "reply.webs.[0].name") == "ConfigAdmin"
 *         assert mapo.lookup( "reply.webs.[1].name") == "RemoteServicesAdmin"
 *         assert mapo.lookup( "reply.webs.[1].url") == "https://localhost:8080/aps/RemoteSvcAdmin"
 *
 * Note that the values are API-wise of type Object! This is because it can be anything, like a String, Map,
 * List, Number (if you stick to JSON formats) or any other type of value you put in there.
 *
 * Also note the indexes in the keys in the example. It is not "webs[0]" but "webs.[0]"! The index is a
 * reference name in itself. The keys returned by getAllKeys() have a number between the '[' and the ']' for
 * List entries. This number is the number of entries in the list. The MapPath class (used by this class)
 * can be used to provide array size of an array value.
 */
@CompileStatic
@TypeChecked
class Mapo {

    //
    // Properties
    //

    @Delegate
    Map<String, Object> map = [ : ]

    //
    // Constructors
    //

    /**
     * Default constructor.
     */
    Mapo() {}

    /**
     * Creates a new Mapo instance from a Map.
     *
     * @param map The map to work with.
     */
    Mapo( Map<String, Object> map ) {
        this.map.putAll( map )
    }

    //
    // Methods
    //

    /**
     * Calls the provided handler for each value key in the map.
     *
     * @param keyHandler The handler to call with value keys.
     */
    void withAllKeys( Closure keyHandler ) {

        findKeys( this.map, new MapPath(), keyHandler )
    }

    /**
     * Returns all keys as a List.
     */
    List<String> getAllKeys() {

        List<String> allKeys = [ ]
        findKeys( this.map, new MapPath() ) { String key ->

            allKeys += key
        }

        allKeys
    }

    /**
     * Does the actual resolving of all value keys.
     *
     * @param map The map to look in.
     * @param path The current path in the map.
     * @param keyHandler The handler to call with value keys.
     */
    private void findKeys( Object searchable, MapPath path, Closure keyHandler ) {

        if ( searchable instanceof Map ) {

            Map<String, Object> map = searchable as Map<String, Object>
            map.each { String key, Object value ->

                MapPath subKey = path.down( key )

                if ( value instanceof Map || value instanceof List ) {

                    findKeys( value, subKey, keyHandler )
                }
                else {

                    keyHandler( subKey.toString() )
                }
            }
        }
        else if ( searchable instanceof List ) {

            MapPath subKey = path.down( "[${( searchable as List ).size()}]" )

            if ( !( searchable as List ).isEmpty() ) {

                Object value = ( searchable as List ).first()

                if ( value instanceof Map || value instanceof List ) {

                    findKeys( value, subKey, keyHandler )
                }
                else {

                    keyHandler( subKey.toString() )
                }
            }
        }
    }

    /**
     * Looks up the value of a specified path.
     *
     * @param path The path to lookup.
     *
     * @return The value or null.
     */
    Object lookup( String path ) {

        lookup( new MapPath( path ) )
    }

    /**
     * Looks up the value of a specified path.
     *
     * @param path The path to lookup.
     *
     * @return The value or null.
     */
    Object lookup( MapPath path ) {

        Object current = this.map

        // I've used Groovy closures over and over and over and over, many many times. I've had no problems what
        // so ever. This code here however had me fighting for many, many hours. I did the following:
        //
        //     mpath.toParts().each { String part -> ... and the rest exactly the same.
        //
        // This threw a NullPointerException! Finally giving up and just changing the .each {...} closure to a
        // for loop instead, it worked perfectly as expected directly!! I wondered if it could be the the @Delegate
        // code generation on compile that interfered in some way. But no, that was not it! Yes, that was a bit
        // far fetched. But so is this incomprehensible problem!! There should be no difference between the .each
        // and a for loop! This smells like a Groovy bug.

        for ( String part : path.toParts() ) {

            if ( Map.class.isAssignableFrom( current.class ) ) {

                current = ( current as Map ).get( part )
            }
            else if ( List.class.isAssignableFrom( current.class ) ) {

                if ( !part.startsWith( "[" ) ) {
                    throw new APSValidationException( "Expected a list index here, got: '${part}'" )
                }

                int index = Integer.valueOf( part.replace( "[", "" ).replace( "]", "" ) )
                current = ( current as List ).get( index )
            }

        }

        current
    }
}
