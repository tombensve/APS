import StructPath from "./StructPath"

//
// PORTED FROM GROOVY (APSCoreLib)
//
/**
 * ## Structured Map
 *
 * This refers to a Map containing both single value values and Map and List values. Basically it refers to
 * a JSON structure. Data read from JSON sources will be put into this. So why do we handle JSON as a Map ?
 * Well, because JSON basically is a Map, and because this is Groovy and Groovy supports JSON structures
 * using maps. The difference is that Groovy has '[' where JSON has '{' for objects.
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
 *         assert structMap.lookup( "reply.webs.[*]" ) == 2
 *
 * Note that the values are API-wise of type Object! This is because it can be anything, like a String, Map,
 * List, Number (if you stick to JSON formats) or any other type of value you put in there.
 *
 * Also note the indexes in the paths in the example. It is not "webs[0]" but "webs.[0]"! The index is a
 * reference name in itself. The paths returned by getStructPaths() have a number between the '[' and the ']' for
 * List entries. An index of [*] and nothing more after that will return the number of entries in that list
 * as an int.
 */
class StructMap {

    //
    // Constructors
    //

    /**
     * Creates a new StructMap instance from a Map.
     *
     * @param map The map to work with.
     */
    constructor( map: {} = {} ) {
        Object.assign( this, map );
    }

    //
    // Methods
    //

    /**
     * Does the actual resolving of all value keys.
     *
     * @param searchable The object to look in.
     * @param path The current path in the map.
     * @param pathHandler The handler to call with paths.
     */
    findPaths( searchable: {}, path: StructPath, pathHandler: () => mixed ) {

        if ( typeof searchable === "object" ) {

            let map = searchable;

            for ( let key of Object.keys( map ) ) {
                let value = map[key];
                let subKey: StructPath = path.down( key );

                if ( value.constructor === Object || value.constructor === Array ) {

                    this.findPaths( value, subKey, pathHandler );
                }
                else {

                    pathHandler( subKey.toString() );
                }
            }
        }

        else if ( searchable.constructor === Array ) {

            let subKey = path.down( `[${searchable.length}]` );

            if ( searchable.length !== 0 ) {

                let value = searchable[0];

                if ( value.constructor === Object || value.constructor === Array ) {

                    this.findPaths( value, subKey, pathHandler );
                }

                else {

                    pathHandler( subKey.toString() );
                }
            }
        }
    }

    /**
     * Looks up the value of a specified stuctured path.
     *
     * A path ending in [*] will return the number of entries in the array.
     *
     * @param structPath The structured path to lookup.
     *
     * @return The value found at the path.
     */
    lookup( structPath: string ) {
        let current = this;

        for ( let part of structPath.split( "\\." ) ) {

            if ( current.constructor === Object ) {

                let next = current[part];
                if ( next != null ) {
                    current = next;
                }
                else {
                    throw new Error( "Bad path [${structPath}]! Failed on '${part}'" );
                }
            }
            else if ( current.constructor === Array ) {

                if ( !part.startsWith( "[" ) ) {
                    throw new Error( "Expected a list index here, got: '${part}'" );
                }

                let ixStr: string = part.replace( "[", "" ).replace( "]", "" );

                // Index '*' means number of entries in the list. In this case anything below this is
                // ignored, so we return with the size immediately.
                if ( ixStr === "*" ) {
                    return current.size();
                }
                else {
                    let index: number = parseInt( ixStr );
                    current = current[index];
                }
            }
        }

        return current;
    }

    /**
     * provides a value.
     *
     * @param structPath The value path.
     * @param value The value.
     */
    provide( structPath: string, value: {} ) {

        // Yeah, I know. This is not a wonder of clarity!!

        let current = this;

        let parts: [string] = structPath.split( "\\." );

        if ( parts.length > 1 ) {
            //Object oldCurrent = null

            let last: string = parts[parts.length - 1];

            for ( let i = 0; i < parts.length; i++ ) {

                let part: string = parts[i];

                let oldCurrent: {} = current;

                if ( i === ( parts.length - 1 ) ) {

                    if ( current.constructor === Object ) {

                        current[last] = value;
                    }
                    else if ( current.constructor === Array ) {

                        let index = parseInt( part.replace( "[", "" ).replace( "]", "" ) );

                        while ( index > ( current.length - 1 ) ) {
                            current.push( {} );
                        }

                        current[index] = value;
                    }
                    else {
                        throw new Error( `Unknown type in structure: ${current}` );
                    }
                }
                else if ( current.constructor === Object ) {

                    current = current[part];
                }
                else if ( current.constructor === Array ) {

                    if ( !part.startsWith( "[" ) ) {
                        throw new Error( "Expected a list index here, got: '${part}'" );
                    }

                    let index: number = parseInt( part.replace( "[", "" ).replace( "]", "" ) );
                    while ( current.length() < ( index + 1 ) ) {
                        current.push( {} );
                    }
                    current = current[index];
                }

                // We need to create a new object.
                if ( current == null ) {
                    let newValue = "";

                    if ( i < ( parts.length - 1 ) ) {
                        if ( parts[i + 1].startsWith( "[" ) ) {
                            newValue = [];
                        }
                        else {
                            newValue = {};
                        }
                    }

                    // The first should always be a Map!
                    if ( oldCurrent == null || oldCurrent.constructor === Object ) {
                        oldCurrent[part] = newValue;
                    }
                    else {
                        oldCurrent.push( newValue );
                    }

                    current = newValue
                }

            }
        }
        else {
            this[structPath] = value;
        }
    }
}
