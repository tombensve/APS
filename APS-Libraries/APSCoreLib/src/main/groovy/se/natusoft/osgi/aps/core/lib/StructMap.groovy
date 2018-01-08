package se.natusoft.osgi.aps.core.lib

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.osgi.aps.exceptions.APSValidationException

import java.util.function.Consumer

/**
 * ## Structured Map
 *
 * This refers to a Map containing both single value values and Map and List values. Basically it refers to
 * a JSON structure. Data read from JSON sources will be put into this. So why do we handle JSON as a Map ?
 * Well, because JSON basically is a Map, and because this is Groovy and Groovy supports JSON structures
 * using maps. The difference is that Groovy has '[' where JSON has '{' for objects.
 *
 * I started out calling it a JSONMap, but finally decided agains that.
 *
 * A key is a String contining branches separated by '.' characters for each sub structure, and I've
 * decided to call these paths.
 *
 * Note that since this delegates to the wrapped Map the class is also a Map when compiled!
 *
 * Here is an example from the test that shows how to lookup and how to use the paths:
 *
 *         assert structMap.lookup( "header.type" ).toString() == "service"
 *         assert structMap.lookup( "header.address" ).toString() == "aps.admin.web"
 *         assert structMap.lookup( "header.classifier" ).toString() == "public"
 *         assert structMap.lookup( "body.action" ).toString() == "get-webs"
 *         assert structMap.lookup( "reply.webs.[0].name") == "ConfigAdmin"
 *         assert structMap.lookup( "reply.webs.[1].name") == "RemoteServicesAdmin"
 *         assert structMap.lookup( "reply.webs.[1].url") == "https://localhost:8080/aps/RemoteSvcAdmin"
 *
 * Note that the values are API-wise of type Object! This is because it can be anything, like a String, Map,
 * List, Number (if you stick to JSON formats) or any other type of value you put in there.
 *
 * Also note the indexes in the paths in the example. It is not "webs[0]" but "webs.[0]"! The index is a
 * reference name in itself. The paths returned by getStructPaths() have a number between the '[' and the ']' for
 * List entries. This number is the number of entries in the list. The StructPath class (used by this class)
 * can be used to provide array size of an array value.
 */
@SuppressWarnings("SpellCheckingInspection")
@CompileStatic
@TypeChecked
class StructMap implements Map<String, Object> {

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
    @SuppressWarnings("GroovyUnusedDeclaration")
    StructMap() {}

    /**
     * Creates a new Mapo instance from a Map.
     *
     * @param map The map to work with.
     */
    StructMap( Map<String, Object> map ) {
        this.map.putAll( map )
    }

    //
    // Methods
    //

    /**
     * Calls the provided handler for each value key in the map.
     *
     * Yes, it is possible to pass a Groovy closure here. I'm using the java8 Consumer as type
     * to be compatible with java. APSConfig interface in APS-APIs which are java do provide method apis
     * that not coincidentally happen to be exactly what this method provides implementation of ...
     *
     * @param keyHandler The handler to call with value keys.
     */
    void withStructPath( Consumer<String> keyHandler ) {

        findPaths( this.map, new StructPath(), keyHandler )
    }

    /**
     * Returns all deep keys as a List.
     */
    @SuppressWarnings("GroovyUnusedDeclaration")
    List<String> getStructPaths() {

        List<String> allKeys = [ ]
        findPaths( this.map, new StructPath() ) { String key ->

            allKeys += key
        }

        allKeys
    }

    /**
     * Does the actual resolving of all value keys.
     *
     * @param map The map to look in.
     * @param path The current path in the map.
     * @param pathHandler The handler to call with paths.
     */
    private void findPaths( Object searchable, StructPath path, Consumer<String> pathHandler ) {

        if ( searchable instanceof Map ) {

            Map<String, Object> map = searchable as Map<String, Object>
            map.each { String key, Object value ->

                StructPath subKey = path.down( key )

                if ( value instanceof Map || value instanceof List ) {

                    findPaths( value, subKey, pathHandler )
                }
                else {

                    pathHandler.accept( subKey.toString() )
                }
            }
        }
        else if ( searchable instanceof List ) {

            StructPath subKey = path.down( "[${( searchable as List ).size()}]" )

            if ( !( searchable as List ).isEmpty() ) {

                Object value = ( searchable as List ).first()

                if ( value instanceof Map || value instanceof List ) {

                    findPaths( value, subKey, pathHandler )
                }
                else {

                    pathHandler.accept( subKey.toString() )
                }
            }
        }
    }

    /**
     * Looks up the value of a specified structPath.
     *
     * @param structPath The structPath to lookup.
     *
     * @return The value or null.
     */
    Object lookup( String structPath ) {

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

        for ( String part : structPath.split( "\\." ) ) {

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

    /**
     * provides a value.
     *
     * @param structPath The value path.
     * @param value The value.
     */
    void provide( String structPath, Object value ) {

        // Yeah, I know. This is not a wonder of clarity!!

        Object current = this.map

        String[] parts = structPath.split( "\\." )

        if ( parts.length > 1 ) {
            //Object oldCurrent = null

            String last = parts[ parts.length - 1 ]

            for ( int i = 0; i < parts.length; i++ ) {
                String part = parts[ i ]

                Object oldCurrent = current

                if ( i == ( parts.length - 1 ) ) {

                    if ( Map.class.isAssignableFrom( current.class ) ) {

                        ( current as Map<String, Object> ).put( last, value )
                    }
                    else { // Must be list

                        int index = Integer.valueOf( part.replace( "[", "" ).replace( "]", "" ) )
                        while ( index > ( ( current as List<Object> ).size() - 1 ) ) {
                            ( current as List<Object> ).add( [ : ] )
                        }
                        ( current as List<Object> ).set( index, value )
                    }
                }
                else if ( Map.class.isAssignableFrom( current.class ) ) {

                    current = ( current as Map ).get( part )
                }
                else if ( List.class.isAssignableFrom( current.class ) ) {

                    if ( !part.startsWith( "[" ) ) {
                        throw new APSValidationException( "Expected a list index here, got: '${part}'" )
                    }

                    int index = Integer.valueOf( part.replace( "[", "" ).replace( "]", "" ) )
                    while ( (current as List).size(  ) < (index + 1)) {
                        (current as List).add([ : ])
                    }
                    current = ( current as List ).get( index )
                }

                // We need to create a new object.
                if ( current == null ) {
                    Object newValue = ""

                    if ( i < ( parts.length - 1 ) ) {
                        // IDEA seem to have gotten a permanent memory of a no longer existing situation here!
                        //noinspection GroovyIfStatementWithIdenticalBranches
                        if ( parts[ i + 1 ].startsWith( "[" ) ) {
                            newValue = [ ]
                        }
                        else {
                            newValue = [ : ]
                        }
                    }

                    // The first should always be a Map!
                    if ( oldCurrent == null || Map.isAssignableFrom( oldCurrent.class ) ) {
                        ( oldCurrent as Map ).put( part, newValue )
                    }
                    else {
                        ( oldCurrent as List<Object> ).add( newValue )
                    }

                    current = newValue
                }

            }
        }
        else {
            this.map.put( structPath, value )
        }
    }
}
