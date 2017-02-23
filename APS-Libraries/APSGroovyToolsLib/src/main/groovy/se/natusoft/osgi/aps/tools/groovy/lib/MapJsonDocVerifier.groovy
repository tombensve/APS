package se.natusoft.osgi.aps.tools.groovy.lib

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

/**
 * This class uses `Map<String, Object>` to represent JSON documents (Vertx:s JsonObject supports mapping to/from this format).
 *
 * This class is used to define the content of such objects (like a schema) and validates real such objects against it.
 *
 * Example:
 *
 *    Map<String, Object> struct = [
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
 *                url_0: "?^https?://.*"
 *             ]
 *          ]
 *       ]
 *    ]
 *
 *  ### Keys
 *
 *  * __key\_1__ : This entry is required.
 *  * __key\_0__ : This entry is optional.
 *
 * ### Values
 *
 * * __"?regexp"__ : The '?' indicates that the rest of the value is a regular expression. This regular expression will
 *   be applied to each value.
 *
 *   __"bla"__ : This requires values to be exactly "bla".
 *
 */
@CompileStatic
@TypeChecked
class MapJsonDocVerifier {

    /**
     * Converts the original map into 2 maps that is easier to work against.
     */
    private class MapObject {
        private Map<String, Object> map = [ : ]
        private Map<String, Boolean> required = [ : ]

        /**
         * Creates a new MapObject.
         *
         * @param map The Map to wrap.
         */
        MapObject(Map<String, Object> map) {
            map.each { String key, Object value ->
                verifyKey( key )
                String realKey = _key ( key )

                this.map.put ( realKey ,  value )
                this.required.put ( realKey , _required ( key ) ? Boolean.TRUE : Boolean.FALSE )
            }
        }

        /**
         * @return The internal key set for looking up in.
         */
        Set<String> keySet() {
            this.map.keySet()
        }

        /**
         * Returns an object for a specific key.
         * @param key The key to get object for.
         */
        Object get ( String key ) {
            this.map [ key ]
        }

        /**
         * Returns true if the specified key points to a required object.
         *
         * @param key The key to check for required.
         */
        boolean isRequired(String key ) {
            this.required [ key ]
        }

        /**
         * Verifies that the key is correctly formatted.
         *
         * @param mapKey The key to verify.
         */
        private static verifyKey( String mapKey ) {
            String[] parts = mapKey.split( "_" )
            if ( parts.length > 2 || ( parts.length == 2 && ( parts[1] != '0' && parts[1] != '1' ) ) )
                throw new IllegalStateException( "Bad key format! Should be 'name' or 'name_0' or 'name_1'" )
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
            if ( parts.length == 2 ) {
                return parts[ 1 ] == "1"
            }

            false
        }
    }

    //
    // Properties
    //

    /** A valid structure to validate agains. */
    Map<String, Object> validStructure

    //
    // Methods
    //

    /**
     * Validates a Map structure against the structural definition of this object.
     *
     * @param toValidate The map to validate.
     *
     * @throws IllegalStateException on validation failure.
     */
    void validate( Map<String, Object> toValidate ) throws IllegalStateException {
        validateMap( this.validStructure , toValidate )
    }

    /**
     * Validates the contents of a Map<Object, String> against the valid declaration Map<Object, String>
     *
     * @param _validStructure The valid structure to validate against.
     * @param _toValidate The structure to validate.
     */
    private void validateMap( Map<String , Object> validStructure , Map<String, Object> _toValidate ) {
        MapObject validMO = new MapObject ( validStructure )

        // validate children
        _toValidate.keySet().each { String key ->

            if ( !validMO.keySet().contains( key ) ) {
                throw new IllegalStateException ( "Entry '${key}' is not valid!" )
            }

            Object sourceValue = _toValidate [ key ]
            if ( validMO.isRequired ( key ) && sourceValue == null )
                throw new IllegalStateException ( "'${key}' is required!" )

            Object validStructureEntry = validMO.get ( key )

            if ( validStructureEntry instanceof String ) {
                String validValue = validMO.get ( key ) as  String
                if ( validValue.startsWith ("?") ) {
                    String regExp = validValue.substring ( 1 )
                    if ( !sourceValue.toString().matches(regExp) ) {
                        throw new IllegalStateException( "Value '${sourceValue}' does not match regular expression '${regExp}'!" )
                    }
                }
                else {
                    if ( sourceValue.toString() != validValue ) {
                        throw new IllegalStateException( "Found '${sourceValue}'. Expected '${validValue}'!" )
                    }
                }
            }
            else if ( validStructureEntry instanceof Map ) {
                Map<String, Object> toValidateMap = sourceValue as Map<String, Object>
                validateMap(validStructureEntry as Map<String, Object>, toValidateMap)
            }
            else if ( validStructureEntry instanceof List ) {
                List<Object> toValidateList = _toValidate [ key ] as List<Object>
                validateList(validStructureEntry as List<Object>, toValidateList)
            }

        }

        validMO.keySet().each { String key ->
            if ( validMO.isRequired ( key ) && !_toValidate.keySet().contains( key ) ) {
                throw new IllegalStateException("Missing entry for required '${key}'!")
            }
        }

    }

    /**
     * Validates the contents of a List<Object> against the valid declaration List<Object>.
     *
     * @param validList The valid list to validate against.
     * @param toValidate The list to validate.
     */
    private void validateList( List<Object> validList , List<Object> toValidate ) {
        Object validObject = validList [ 0 ]

        toValidate.each { Object toValidateEntry ->

            if ( validObject instanceof String ) {
                String validValue = validObject as String
                if ( validValue.startsWith( "?" ) ) {
                    String regExp = validValue.substring(1)
                    if ( !toValidateEntry.toString().matches( regExp ) ) {
                        throw new IllegalStateException( "Value '${toValidateEntry}' does not match regular expression '${regExp}'!" )
                    }
                } else {
                    if ( toValidateEntry.toString() != validValue ) {
                        throw new IllegalStateException( "Found '${toValidateEntry}'. Expected '${validValue}'!" )
                    }
                }
            } else if ( validObject instanceof Map ) {
                validateMap( validObject as Map<String, Object>, toValidateEntry as Map<String, Object> )
            }
            else if ( validObject instanceof List ) {
                validateList(validObject as List<Object>, toValidateEntry as List<Object>)
            }
        }

    }
}
