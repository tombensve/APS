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
import se.natusoft.docutations.Nullable
import se.natusoft.osgi.aps.exceptions.APSValidationException

/**
 * This class uses `Map<String, Object>` to represent JSON documents (Vertx:s JsonObject supports mapping to/from
 * this format).
 *
 * This class is used to define the content of such objects (like a schema) and validates real such objects against it.
 *
 * Example (do not expect example to have realistic data):
 *
 *    Map<String, Object> struct = [
 *       header_?: "The nessage header.",
 *       header_1: [
 *          type_1      : "service",
 *          address_1   : "aps.admin.web",
 *          classifier_0: "?public|private"
 *       ],
 *       body_1  : [
 *          action_1: "get-webs"
 *       ],
 *       reply_0: [
 *          webs_1: [
 *             [
 *                name_1: "?.*",
 *                url_0: "?^https?://.*",
 *                someNumber_0: "#0-100" // Also valid:  ">0" "<100" ">=0" "<=100"
 *             ]
 *          ]
 *       ],
 *       addrmap_1: [
 *          [
 *             "name_1": "?[a-z,A-z,0-9,_]*",
 *             "value_1": "?[0-9,.]*"
 *          ]
 *       ]
 *   ]
 *
 * ### Keys
 *
 *  * __key\_1__ : This entry is required.
 *  * __key\_0__ : This entry is optional.
 *  * __key_?__  : A description of this key.
 *
 * __Note__ that all keys must always be static!
 *
 * ### Values
 *
 * #### "?regexp"
 *
 * The '?' indicates that the rest of the value is a regular expression. This regular expression will be applied to
 * each value.
 *
 * #### "\#range"
 *
 * This indicates that this is a number and defines the number range allowed. The following variants are available:
 *
 * __"#from-to"__ : This specifies a range of allowed values, from lowest to highest.
 *
 * __"#<=num"__ : This specifies that the numeric value must be less than or equal to the specified number.
 *
 * __"#>=num"__ : This specifies that the numeric value must be larger than or equal to the specified number.
 *
 * __"#<num"__ : This specifies that the numeric value must be less than the specified number.
 *
 * __"#>num"__ : This specifies that the numeric value must be larger than the specified number.
 *
 * Note: Both floating point numbers and integers are allowed.
 *
 * #### "!"
 *
 * This means the value is a boolean.
 *
 * #### "bla"
 *
 * This requires values to be exactly "bla".
 *
 * #### [:]
 *
 * An empty object means any object, non validated. Do note that it is not possible to have validated objects
 * under a non validated object. When there is a non validated object, anything under it is also non validated.
 *
 * WARNING: THIS DOES NOT HOLD FOR DYNAMIC BUT SPECIFIC SUB CONTENT!!!
 */
@CompileStatic
@TypeChecked
class MapJsonSchemaValidator implements MapJsonSchemaConst {

    /**
     * Converts the original map into 2 maps that is easier to work against.
     */
    private static class MapObject {

        private Map<String, Object> schemaMap = [ : ]
        private Map<String, Boolean> required = [ : ]
        private boolean empty = false

        /**
         * Creates a new MapObject.
         *
         * @param schemaMap The Map to wrap.
         */
        MapObject( Map<String, Object> schemaMap ) {

            if ( schemaMap.isEmpty() ) {
                this.empty = true
            }
            else {
                schemaMap.each { String key, Object value ->

                    if ( verifyKey( key, this.schemaMap ) ) {
                        String realKey = _key( key )

                        this.schemaMap.put( realKey, value )
                        this.required.put( realKey, _required( key ) ? Boolean.TRUE : Boolean.FALSE )
                    }
                }
            }
        }

        /**
         * @return The internal key set for looking up in.
         */
        Set<String> keySet() {

            this.schemaMap.keySet()
        }

        /**
         * Returns an object for a specific key.
         * @param key The key to get object for.
         */
        Object get( String key ) {

            this.schemaMap[ key ]
        }

        /**
         * Returns true if the specified key points to a required object.
         *
         * @param key The key to check for required.
         */
        boolean isRequired( String key ) {

            this.required[ key ]
        }

        /**
         * @return true if the valid Map object is empty. In this case anything is allowed.
         */
        boolean isEmpty() {
            return this.empty
        }

        /**
         * Verifies that the key is correctly formatted.
         *
         * @param mapKey The key to verify.
         * @param source The source of the key. Used for exception message to make things clearer.
         */
        private static boolean verifyKey( String mapKey, Map<String, Object> source ) {

            String[] parts = mapKey.split( "_" )

            if ( parts.length >= 2 && ( parts[ 1 ] != '0' && parts[ 1 ] != '1' ) && parts[ 1 ] != '?' ) {

                throw new APSValidationException( "Bad key format! [$mapKey] Should be 'name' or 'name_0' or 'name_1'" +
                        ". ${source}" )
            }

            return parts[ 1 ] != '?'
        }

        /**
         * Returns the key without formatting.
         *
         * @param mapKey The key to "plainify".
         */
        private static String _key( String mapKey ) {

            mapKey.split( "_" )[ 0 ]
        }

        /**
         * Returns true if the specified key indicates a required object.
         *
         * @param mapKey The key to check for required object.
         */
        private static boolean _required( String mapKey ) {

            String[] parts = mapKey.split( "_" )

            if ( parts.length >= 2 ) {

                return parts[ 1 ] == "1"
            }

            false
        }
    }

    //
    // Private Members
    //

    private Map<String, Map<String, Object>> namedStructures = [ : ]

    //
    // Properties
    //

    /** A valid structure to validate against. */
    Map<String, Object> validStructure

    void setValidStructure( Map<String, Object> validStructure ) {
        this.validStructure = validStructure

        Map<String, Object> named = this.validStructure.get( "!@@Named" ) as Map<String, Object>
        if ( named != null ) {

            named.keySet().each { String key ->
                this.namedStructures.put( key, named.get( key ) as Map<String, Object> )
            }

            this.validStructure.remove( "!@@Named" )
        }
    }

    //
    // Methods
    //

    /**
     * This is for Java code which can't do a property constructor.
     *
     * Use:
     *
     *       new MapJsonDocSchemaValidator().validStructure(schema).validate(toValidate);
     *
     * @param schema The schema to use.
     *
     * @return this.
     */
    @SuppressWarnings( "GroovyUnusedDeclaration" )
    MapJsonSchemaValidator validStructure( Map<String, Object> schema ) {

        this.setValidStructure( schema )
        return this
    }

    /**
     * Validates a Map structure against the structural definition of this object.
     *
     * @param toValidate The map to validate.
     *
     * @throws APSValidationException on validation failure.
     */
    void validate( Map<String, Object> toValidate ) throws APSValidationException {

        if ( toValidate == null ) {

            throw new APSValidationException( "Input to validate is null!" )
        }

        validateMap( this.validStructure, toValidate )
    }

    /**
     * Validates that a field is a boolean.
     *
     * @param sourceValue The value to validate.
     * @param errorSource For error message.
     */
    private static void validateBoolean( Object sourceValue, Object errorSource ) {
        if ( !Boolean.class.isAssignableFrom( sourceValue.class ) && !boolean.class.isAssignableFrom( sourceValue
                .class ) ) {
            throw new APSValidationException( "Value '${sourceValue}' must be a boolean! ${errorSource}" )
        }
    }

    /**
     * Valides a string.
     *
     * @param validValue The valid value schema spec.
     * @param sourceValue The value to validate.
     * @param errorSource For error message.
     */
    private static void validateString( String validValue, Object sourceValue, Object errorSource ) {

        if ( validValue.startsWith( REGEXP ) || validValue.startsWith( ENUMERATION ) ) {

            String regExp = validValue.substring( 1 )

            if ( !sourceValue.toString().matches( regExp ) ) {

                throw new APSValidationException( "Value '${sourceValue}' does not match regular expression " +
                        "'${regExp}'! ${errorSource}" )
            }
        }
        else {

            if ( sourceValue.toString() != validValue ) {

                throw new APSValidationException( "Found '$sourceValue'. Expected '$validValue'! $errorSource" )
            }
        }
    }

    /**
     * Validates a number.
     *
     * @param validValue The valid value schema spec.
     * @param sourceValue The value to validate.
     * @param errorSource For error messages.
     * @return true if this was a number.
     */
    private static boolean validateNumber( String validValue, Object sourceValue, Object errorSource ) {

        boolean result = false

        if ( validValue.startsWith( NUMBER ) ) {

            result = true

            validValue = validValue.substring( 1 )

            String from = null, to = null
            boolean equals = false

            if ( validValue.startsWith( ">=" ) ) {

                from = validValue.substring( 2 )
                equals = true
            }
            else if ( validValue.startsWith( ">" ) ) {

                from = validValue.substring( 1 ).trim()
            }
            else if ( validValue.startsWith( "<=" ) ) {

                to = validValue.substring( 2 )
                equals = true
            }
            else if ( validValue.startsWith( "<" ) ) {

                to = validValue.substring( 1 )
            }
            else if ( validValue.contains( "-" ) ) {

                String[] parts = validValue.split( "-" )
                from = parts[ 0 ]
                to = parts[ 1 ]
            }

            if ( Double.class.isAssignableFrom( sourceValue.class ) || Float.class.isAssignableFrom( sourceValue.class ) ) {

                validateNumberAsJavaLangNumber( sourceValue as Number, equals, from, to, errorSource ) { String val ->
                    Double.valueOf( val ) as Number
                }
            }
            else if ( Long.class.isAssignableFrom( sourceValue.class ) || Integer.class.isAssignableFrom( sourceValue.class ) ||
                    Short.class.isAssignableFrom( sourceValue.class ) ) {

                validateNumberAsJavaLangNumber( sourceValue as Number, equals, from, to, errorSource ) { String val ->

                    Long.valueOf( val ) as Number
                }
            }
        }

        result
    }

    /**
     * This part does not care about specific numeric type. It works with java.lang.Number. It validates that value is
     * withing specified range.
     *
     * @param sourceValue The value being validated.
     * @param equals true if it is <= or >=, false if < or >.
     * @param from The from in the range.
     * @param to The to in the range.
     * @param errorSource For error messages.
     * @param fromString A closure that converts a value from a String to a Number. Different conversions needed for
     * floating point
     *                   numbers and integers.
     */
    private static void validateNumberAsJavaLangNumber( Number sourceValue, boolean equals,
                                                        @Nullable String from, String to,
                                                        Object errorSource, Closure<Number> fromString ) {
        Number value = sourceValue as Number

        if ( equals ) {

            if ( from != null && to == null ) {

                Number fromD = fromString( from ) //Double.valueOf ( from )
                if ( value >= fromD ) {
                    /*ok*/
                }
                else {
                    throw new APSValidationException( "Value ($value) must be >= $fromD! $errorSource" )
                }
            }
            else if ( from == null && to != null ) {

                Number toD = fromString( to ) //Double.valueOf( to )
                if ( value <= toD ) {
                    /*ok*/
                }
                else {
                    throw new APSValidationException( "Value ($value) must be <= $toD! $errorSource" )
                }
            }
            else {

                Number fromD = fromString( from ) //Double.valueOf ( from )
                Number toD = fromString( to ) //Double.valueOf ( to )

                if ( value >= fromD && value <= toD ) {
                    /*ok*/
                }
                else {
                    throw new APSValidationException( "Value ($value) must be >= $fromD && <= $toD $errorSource" )
                }
            }
        }
        else {

            if ( from != null && to == null ) {

                Number fromD = fromString( from ) //Double.valueOf ( from )

                if ( value > fromD ) {
                    /*ok*/
                }
                else {
                    throw new APSValidationException( "Value ($value) must be > $fromD! $errorSource" )
                }
            }
            else if ( from == null && to != null ) {

                Number toD = fromString( to ) //Double.valueOf( to )

                if ( value < toD ) {
                    /*ok*/
                }
                else {
                    throw new APSValidationException( "Value ($value) must be < $toD! $errorSource" )
                }
            }
            else {

                Number fromD = fromString( from ) //Double.valueOf ( from )
                Number toD = fromString( to ) //Double.valueOf ( to )

                if ( value > fromD && value < toD ) {
                    /*ok*/
                }
                else {
                    throw new APSValidationException( "Value ($value) must be >= $fromD && <= $toD $errorSource" )
                }
            }
        }
    }

    /**
     * Validates the contents of a Map<Object, String> against the valid declaration Map<Object, String>
     *
     * @param _validStructure The valid structure to validate against.
     * @param toValidate The structure to validate.
     */
    private void validateMap( Map<String, Object> validStructure, Map<String, Object> toValidate ) {

        MapObject validMO = new MapObject( validStructure )

        if ( validMO.isEmpty() ) {
            return
        }

        // validate children
        toValidate.each { String key, Object value ->

            Object validStructureEntry = validMO.get( key )

            if ( validStructureEntry.toString().startsWith( "!@@" ) ) {

                Object sourceValue = value
                Map<String, Object> toValidateMap = sourceValue as Map<String, Object>
                String name = validStructureEntry.toString().substring( 3 )

                validateMap( this.namedStructures[ name ], toValidateMap )
            }
            else {
                boolean doValidate = true

                if ( validStructureEntry != null && validStructureEntry instanceof Map ) {
                    if ( ( validStructureEntry as Map ).isEmpty() ) {
                        doValidate = false
                    }
                }

                // An empty map as a value means accept anything there under. Thereby we don't
                // validate in that situation.
                if ( doValidate ) {

                    if ( !validMO.keySet().contains( key ) || key.startsWith( "!@@" ) ) {

                        throw new APSValidationException( "Entry '${key}' is not valid! $toValidate" )
                    }

                    Object sourceValue = value

                    if ( validMO.isRequired( key ) && sourceValue == null ) {

                        throw new APSValidationException( "'${key}' is required! $toValidate" )
                    }

                    if ( validStructureEntry instanceof String ) {

                        String validValue = validMO.get( key ) as String

                        if ( validValue.trim() == BOOLEAN ) {

                            validateBoolean( sourceValue, toValidate )
                        }
                        else {
                            if ( !validateNumber( validValue, sourceValue, toValidate ) ) {

                                validateString( validValue, sourceValue, toValidate )
                            }
                        }

                    }

                    else if ( validStructureEntry instanceof Map ) {

                        Map<String, Object> toValidateMap = sourceValue as Map<String, Object>
                        validateMap( validStructureEntry as Map<String, Object>, toValidateMap )
                    }

                    // If the 'array' part in this line is red marked, then you are using IDEA 2018.2!
                    else if ( validStructureEntry instanceof List || validStructureEntry.class.array ) {

                        List<Object> toValidateList = sourceValue as List<Object>
                        validateList( validStructureEntry as List<Object>, toValidateList )
                    }
                }
            }
        }

        validMO.keySet().each { String key ->

            if ( validMO.isRequired( key ) && !toValidate.keySet().contains( key ) ) {

                throw new APSValidationException( "Missing entry for required '$key'! $toValidate" )
            }
        }

    }

    /**
     * Validates the contents of a List<Object> against the valid declaration List<Object>.
     *
     * @param validList The valid list to validate against.
     * @param toValidate The list to validate.
     */
    private void validateList( List<Object> validList, List<Object> toValidate ) {

        Object validObject = validList[ 0 ]
        if (validObject.toString(  ).startsWith( "!@@" )) {
            String name = validObject.toString(  ).substring( 3 )
            validateMap( this.namedStructures[name], toValidate[0] as Map<String, Object> )
        }
        else {
            // All entries in a list must currently be identical.

            toValidate.each { Object sourceValue ->

                if ( validObject instanceof String ) {

                    String validValue = validObject as String

                    if ( validValue.trim() == BOOLEAN ) {
                        validateBoolean( sourceValue, toValidate )
                    }
                    else {
                        if ( !validateNumber( validValue, sourceValue, toValidate ) ) {

                            validateString( validValue, sourceValue, toValidate )
                        }
                    }
                }
                else if ( validObject instanceof Map ) {

                    validateMap( validObject as Map<String, Object>, sourceValue as Map<String, Object> )
                }
                else if ( validObject instanceof List || validObject.class.array ) {

                    validateList( validObject as List<Object>, sourceValue as List<Object> )
                }
            }
        }

    }
}
