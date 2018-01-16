package se.natusoft.osgi.aps.core.lib

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

/**
 * This class scans a MapJson schema as defined by MapJsonDocValidator and extracts a list
 * of MapJsonEntryMeta instances for each value in a MapJson structure living up to the
 * schema.
 *
 * From these the following can be resolved:
 *
 * - The name of a value.
 * - The type of a value.
 * - Is the value required ?
 * - The constraints of the value. If this starts with '?' then the rest is a regular expression.
 *   If not the value is a constant, that is, the value has to be exactly as the constraint string.
 *
 * This is not used by the MapJsonDocValidator when validating! This is intended for GUI configuration
 * editors to use to build a configuration GUI producing valid configurations.
 */
@CompileStatic
@TypeChecked
class MapJsonSchemaMeta {
    //
    // Constants
    //

    private static final int NAME = 0
    private static final int REQUIRED = 1

    //
    // Properties
    //

    /** The parsed schema result. */
    List<MapJsonEntryMeta> mapJsonEntryMetas = [ ]

    //
    // Private Members
    //

    /** The schema we will parse. */
    private Map<String, Object> schema

    //
    // Constructors
    //

    /**
     * Creates a new MapJsonSchema.
     *
     * @param schema The MapJsonSchema to parse.
     */
    MapJsonSchemaMeta( Map<String, Object> schema ) {

        this.schema = schema
        parseMap( this.schema, "" )
    }

    //
    // Methods
    //

    /**
     * Parses a Map looking at each entry.
     *
     * @param map The map to parse.
     * @param entryKey The current entry key.
     */
    private void parseMap( Map<String, Object> map, String entryKey ) {

        map.each { String k, Object v ->

            parseValue( k, v, entryKey )
        }
    }

    /**
     * Parses a value looking for Map, List, values (String, Boolean, Number).
     *
     * @param key Current Map key.
     * @param value Current Map value
     * @param entryKey Current entry key.
     */
    private void parseValue( String key, Object value, String entryKey ) {
        String[] parts = key.split( "_" )

        if (!entryKey.isEmpty(  )) {

            entryKey += "."
        }

        if ( value instanceof Map ) {

            parseMap( (Map)value, "${entryKey}${parts[ NAME ]}" )
        }
        else if ( value instanceof List ) {

            Object obj = ( (List)value )[ 0 ]
            parseValue( "${parts[NAME]}.[]", obj, "${entryKey}" )
        }
        else if ( value instanceof String || value instanceof Number || value instanceof boolean || value instanceof Boolean) {

            if ( parts.length >= 2 ) {

                this.mapJsonEntryMetas += new MapJsonEntryMeta( name: "${entryKey}${parts[ NAME ]}", required: parts[ REQUIRED ] == "1",
                        constraints: (String)value, type: typeConv( value.toString() ) )
            }
        }
    }

    /**
     * Converts from string spec to enum.
     *
     * @param typeIdentifier The type to convert.
     */
    private static MapJsonEntryMeta.Type typeConv( String typeIdentifier ) {

        typeIdentifier = typeIdentifier.trim()

        if (typeIdentifier.startsWith("#")) {

            MapJsonEntryMeta.Type.NUMBER
        }
        else if (typeIdentifier.startsWith( "/" )) {

            MapJsonEntryMeta.Type.BOOLEAN
        }
        else {

            MapJsonEntryMeta.Type.STRING
        }

    }

}
