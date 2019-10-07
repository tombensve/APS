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
 *         2018-05-23: Created!
 *
 */
package se.natusoft.osgi.aps.core.lib

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

/**
 * This class scans a MapJson schema as defined by MapJsonDocSchemaValidator and extracts a list
 * of MapJsonMetaEntry instances for each value in a MapJson structure living up to the
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
 * This is not used by the MapJsonDocSchemaValidator when validating! This is intended for GUI configuration
 * editors to use to build a configuration GUI producing valid configurations.
 */
@CompileStatic
@TypeChecked
class MapJsonSchemaMeta implements MapJsonSchemaConst {
    //
    // Constants
    //

    private static final int NAME = 0
    private static final int REQUIRED = 1

    //
    // Properties
    //

    /** The parsed schema result. */
    List<MapJsonSchemaEntry> mapJsonSchemaEntries = [ ]

    //
    // Private Members
    //

    /** The schema we will parse. */
    private Map<String, Object> schema = null

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
     * Utility to create an instance of this class from a schema.
     *
     * @param schema The schema to provide meta data for.
     */
    static MapJsonSchemaMeta from( Map<String, Object> schema ) {
        new MapJsonSchemaMeta( schema )
    }

    /**
     * Parses a Map looking at each entry.
     *
     * @param map The map to parse.
     * @param entryKey The current entry key.
     */
    private void parseMap( Map<String, Object> map, String entryKey ) {

        map.each { String k, Object v ->

            parseValue( k, v, entryKey, map )
        }
    }

    /**
     * Parses a value looking for Map, List, values (String, Boolean, Number).
     *
     * @param key Current Map key.
     * @param value Current Map value
     * @param entryKey Current entry key.
     */
    private void parseValue( String key, Object value, String entryKey, Map<String, Object> map ) {

        String[] parts = key.split( "_" )

        if ( !entryKey.isEmpty() ) {

            entryKey += "."
        }

        if ( value instanceof Map ) {

            parseMap( (Map)value, "${entryKey}${parts[ NAME ]}" )
        }
        else if ( value instanceof List ) {

            Object obj = ( (List)value )[ 0 ]
            parseValue( "${parts[NAME]}.[]", obj, "${entryKey}", map )
        }
        else if ( value instanceof String || value instanceof Number || value instanceof Boolean ) {

            if ( parts.length >= 2 ) {
                String descKey = "${key.split( "_" )[0]}_?"
                //println("############ descKey: ${descKey} : ${map.get( descKey )}")
                String desc = map[ descKey as String ]
                this.mapJsonSchemaEntries << new MapJsonSchemaEntry(
                        name: "${entryKey}${parts[ NAME ]}",
                        required: parts[ REQUIRED ] == "1",
                        constraints: value.toString(),
                        //type: typeConv( value.toString() ),
                        description: desc
                )
            }

        }
    }

    /**
     * Converts from string spec to enum.
     *
     * @param typeIdentifier The type to convert.
     */
    private static Type typeConv( String typeIdentifier ) {

        typeIdentifier = typeIdentifier.trim()

        if (typeIdentifier.startsWith(NUMBER)) {

            Type.NUMBER
        }
        else if (typeIdentifier.startsWith( BOOLEAN )) {

            Type.BOOLEAN
        }
        else if (typeIdentifier.startsWith(ENUMERATION)) {

            Type.ENUMERATION
        }
        else {

            Type.STRING
        }

    }

    /**
     * @return A MapJson representation of this object.
     */
    @SuppressWarnings( "GroovyUnusedDeclaration" )
    List<Map<String,Object>> toMapJson() {
        List<Map<String, Object>> entries = []

        this.mapJsonSchemaEntries.each { MapJsonSchemaEntry entry ->
            entries << entry.toMapJson()
        }

        entries
    }

}

/**
 * The valid types of a schema entry.
 */
@CompileStatic
@TypeChecked
enum Type {
    STRING, BOOLEAN, NUMBER, ENUMERATION
}

/**
 * Holds data about an individual value in a MapJson structure according to a MapJsonDocSchemaValidator defined schema.
 *
 * MapJsonSchemaMeta collects a set of these by parsing a MapJson schema.
 */
@CompileStatic
@TypeChecked
class MapJsonSchemaEntry implements MapJsonSchemaConst {

    //
    // Properties
    //

    /** The name of the entry. */
    String name

    /** Is this entry required ? */
    boolean required

    /** The type of the entry. */
    Type type

    /** The entry constraints. */
    String constraints

    /** A desription of the entry. */
    String description

    //
    // Methods
    //

    /**
     * Sets constraint filtering any starting '|' character.
     *
     * @param constraints The constraints to set.
     */
    void setConstraints( String constraints ) {
        if ( constraints.startsWith( ENUMERATION ) ) {
            constraints = constraints.substring( 1 )
        }
        this.constraints = constraints
    }

    /**
     * @return a List of valid values if type is ENUMERATION, null otherwise.
     */
    @SuppressWarnings( "GroovyUnusedDeclaration" )
    List<String> getEnumValues() {
        List<String> enumValues = null
        if ( this.constraints.startsWith( "?" ) ) {
            enumValues = Arrays.asList( this.constraints.substring( 1 ).split( "\\|" ) )
        }

        enumValues
    }

    /**
     * @return A string representation of this.
     */
    String toString() {
        "{ name: ${name}, required: ${required}, type: ${type}, constraints: ${constraints}, description: " +
                "${description} }"
    }

    /**
     * @return as MapJson object.
     */
    Map<String, Object> toMapJson() {
        Map<String, Object> entry = [ : ]
        entry.name = this.name
        entry.reqired = this.required
        entry.type = type.name()
        entry.constraints = this.constraints
        entry.description = this.description

        entry
    }
}

