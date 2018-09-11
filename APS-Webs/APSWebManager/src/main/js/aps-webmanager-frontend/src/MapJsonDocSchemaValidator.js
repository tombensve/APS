import { SANE, saneTypeId } from "./SaneTypeId"

//
// PORTED FROM GROOVY (APSCoreLib)
//

// schema type identifiers.
export const NUMBER = "#";
export const BOOLEAN = "!";
//export const STRING = " ";
export const ENUMERATION = "|";

export const REGEXP = "?";

const safeStringify = function(obj) {
    try {
        return JSON.stringify(obj);
    }
    catch ( e ) {
        return obj;
    }
};

/**
 * This class uses an JS object to represent JSON documents. This is ported from Groovy code.
 *
 * This class is used to define the content of such objects (like a schema) and validates real such objects against it.
 *
 * Example (do not expect example to have realistic data):
 *
 *   let schema = {
 *       header_?: "The nessage header.",
 *       header_1: {
 *          type_1      : "service",
 *          address_1   : "aps.admin.web",
 *          classifier_0: "?public|private"
 *       },
 *       body_1  : {
 *          action_1: "get-webs"
 *       },
 *       reply_0: {
 *          webs_1: [
 *             {
 *                name_1: "?.*",
 *                url_0: "?^https?://.*",
 *                someNumber_0: "#0-100" // Also valid:  ">0" "<100" ">=0" "<=100"
 *             }
 *          ]
 *       },
 *       addrmap_1: {
 *          {
 *             "name_1": "?[a-z,A-z,0-9,_]*",
 *             "value_1": "?[0-9,.]*"
 *          }
 *       }
 *   }
 *
 *  ### Keys
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
 * The '?' indicates that the rest of the value is a regular expression. This regular expression will be applied to each value.
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
 */
export default class MapJsonDocSchemaValidator {


    //
    // Constructors
    //

    constructor( validStructure: {} ) {

        /** A valid structure to validate against. */
        this.validStructure = validStructure;
    }

    //
    // Methods
    //

    /**
     * Validates a Map structure against the structural definition of this object.
     *
     * @param toValidate The map to validate.
     *
     * @throws APSValidationException on validation failure.
     */
    validate( toValidate: {} ) {

        if ( toValidate == null ) {

            throw new Error( "Input to validate is null!" );
        }

        MapJsonDocSchemaValidator.validateMap( this.validStructure, toValidate );
    }

    /**
     * Validates that a field is a boolean.
     *
     * @param sourceValue The value to validate.
     * @param errorSource For error message.
     */
    static validateBoolean( sourceValue: any, errorSource: any ) {
        if ( !( saneTypeId(sourceValue) === SANE.BOOLEAN ) ) {
            throw new Error( `Value '${ sourceValue }' must be a boolean! ${ safeStringify(errorSource) }` );
        }
    }

    /**
     * Valides a string.
     *
     * @param validValue The valid value schema spec.
     * @param sourceValue The value to validate.
     * @param errorSource For error message.
     */
    static validateString( validValue: string, sourceValue: any, errorSource: any ) {

        if ( validValue.startsWith( REGEXP ) || validValue.startsWith( ENUMERATION ) ) {

            let regExp: string = validValue.substring( 1 );

            if ( sourceValue.toString().match( regExp ) === null ) {

                throw new Error( `Value '${ sourceValue }' does not match regular expression '${ regExp }'! ${ safeStringify(errorSource) }` );
            }
        }
        else {

            if ( sourceValue.toString() !== validValue ) {

                throw new Error( "Found '$sourceValue'. Expected '$validValue'! $errorSource" );
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
    static validateNumber( validValue: string, sourceValue: {}, errorSource: {} ): boolean {

        let result: boolean = false;

        if ( validValue.startsWith( NUMBER ) ) {

            result = true;

            validValue = validValue.substring( 1 );

            let from: string = null, to: string = null;

            let equals: boolean = false;

            if ( validValue.startsWith( ">=" ) ) {

                from = validValue.substring( 2 );
                equals = true;
            }
            else if ( validValue.startsWith( ">" ) ) {

                from = validValue.substring( 1 ).trim();
            }
            else if ( validValue.startsWith( "<=" ) ) {

                to = validValue.substring( 2 );
                equals = true;
            }
            else if ( validValue.startsWith( "<" ) ) {

                to = validValue.substring( 1 );
            }
            else if ( validValue.indexOf( "-" ) >= 0 ) {

                let parts: [string] = validValue.split( "-" );
                from = parts[0];
                to = parts[1];
            }

            if ( !Number.isNaN( sourceValue ) ) { // hmm, no isNumber()!
                this.validateNumberPart2( sourceValue, equals, from, to, errorSource, ( val ) => {
                    if ( val.indexOf( '.' ) >= 0 ) {
                        return parseFloat( val );
                    }
                    return parseInt( val );
                } );
            }
        }

        return result;
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
     * @param fromString A closure that converts a value from a String to a Number. Different conversions needed for floating point
     *                   numbers and integers.
     */
    static validateNumberPart2( sourceValue: number, equals: boolean,
                                from: string, to: string,
                                errorSource: {}, fromString: () => mixed ) {
        let value: number = sourceValue;

        if ( equals ) {

            if ( from != null && to == null ) {

                let fromD = fromString( from );
                if ( value >= fromD ) {
                    /*ok*/
                }
                else {
                    throw new Error( `Value (${value}) must be >= ${fromD}! ${safeStringify(errorSource)}` );
                }
            }
            else if ( from == null && to != null ) {

                let toD = fromString( to );
                if ( value <= toD ) {
                    /*ok*/
                }
                else {
                    throw new Error( `Value (${value}) must be <= ${toD}! ${safeStringify(errorSource)}` );
                }
            }
            else {

                let fromD: number = fromString( from );
                let toD: number = fromString( to );

                if ( value >= fromD && value <= toD ) {
                    /*ok*/
                }
                else {
                    throw new Error( `Value (${value}) must be >= ${fromD} && <= ${toD} ${safeStringify(errorSource)}` );
                }
            }
        }
        else {

            if ( from != null && to == null ) {

                let fromD: number = fromString( from );

                if ( value > fromD ) {
                    /*ok*/
                }
                else {
                    throw new Error( `Value (${value}) must be > ${fromD}! ${safeStringify(errorSource)}` );
                }
            }
            else if ( from == null && to != null ) {

                let toD: number = fromString( to );

                if ( value < toD ) {
                    /*ok*/
                }
                else {
                    throw new Error( `Value (${value}) must be < ${toD}! ${safeStringify(errorSource)}` );
                }
            }
            else {

                let fromD: number = fromString( from );
                let toD: number = fromString( to );

                if ( value > fromD && value < toD ) {
                    /*ok*/
                }
                else {
                    throw new Error( `Value (${value}) must be >= ${fromD} && <= ${toD} ${safeStringify(errorSource)}` );
                }
            }
        }
    }

    /**
     * Validates the contents of a Map<Object, String> against the valid declaration Map<Object, String>
     *
     * @param validStructure The valid structure to validate against.
     * @param toValidate The structure to validate.
     */
    static validateMap( validStructure: {}, toValidate: {} ) {

        let validMO = new MapObject( validStructure );

        if ( validMO.isEmpty() ) return;

        // validate children
        for ( let key of Object.keys( toValidate ) ) {
            let value = toValidate[key];


            let validStructureEntry = validMO.get( key );

            let doValidate: boolean = true;

            if (
                validStructureEntry != null &&
                saneTypeId( validStructureEntry ) === SANE.OBJECT &&
                Object.keys( validStructureEntry ).length === 0
            ) {
                doValidate = false;
            }

            // An empty map as a value means accept anything there under. Thereby we don't
            // validate in that situation.
            if ( doValidate ) {

                if ( !validMO.containsKey( key ) ) {

                    throw new Error( `Entry '${ key }' is not valid! ${JSON.stringify( toValidate )}` );
                }

                let sourceValue: {} = value;

                if ( validMO.isRequired( key ) && sourceValue === null ) {

                    throw new Error( `'${ key }' is required! ${JSON.stringify(toValidate)}` );
                }

                if ( saneTypeId(validStructureEntry) === SANE.STRING ) {

                    let validValue: string = validMO.get( key );

                    if ( validValue.length > 0 && validValue[0] === BOOLEAN ) {

                        MapJsonDocSchemaValidator.validateBoolean( sourceValue, toValidate );
                    }
                    else {
                        if ( !MapJsonDocSchemaValidator.validateNumber( validValue, sourceValue, toValidate ) ) {

                            MapJsonDocSchemaValidator.validateString( validValue, sourceValue, toValidate );
                        }
                    }

                }
                else if ( saneTypeId( validStructureEntry ) === SANE.OBJECT ) {

                    // noinspection UnnecessaryLocalVariableJS
                    let toValidateMap: {} = sourceValue;
                    MapJsonDocSchemaValidator.validateMap( validStructureEntry, toValidateMap );
                }
                // If the 'array' part in this line is red marked, then you are using IDEA 2018.2!
                else if ( saneTypeId( validStructureEntry ) === SANE.ARRAY ) {

                    // noinspection UnnecessaryLocalVariableJS
                    let toValidateList: [] = sourceValue;
                    MapJsonDocSchemaValidator.validateList( validStructureEntry, toValidateList );
                }
            }
        }

        validMO.keySet().forEach( key => {
            if ( validMO.isRequired( key ) && !Object.keys( toValidate ).includes( key ) ) {

                throw new Error( `Missing entry for required '${key}'! ${safeStringify(toValidate)}` );
            }

        } );

    }

    /**
     * Validates the contents of a List<Object> against the valid declaration List<Object>.
     *
     * @param validList The valid list to validate against.
     * @param toValidate The list to validate.
     */
    static validateList( validList: [], toValidate: [] ) {

        let validObject = validList[0]; // All entries in a list must currently be identical.

        toValidate.forEach( ( sourceValue ) => {

            if ( saneTypeId(validObject) === SANE.STRING ) {

                let validValue: string = validObject;

                if ( validValue.trim() === BOOLEAN ) {
                    MapJsonDocSchemaValidator.validateBoolean( sourceValue, toValidate );
                }
                else {
                    if ( !MapJsonDocSchemaValidator.validateNumber( validValue, sourceValue, toValidate ) ) {

                        MapJsonDocSchemaValidator.validateString( validValue, sourceValue, toValidate );
                    }
                }
            }
            else if ( validObject.constructor === Object ) {

                MapJsonDocSchemaValidator.validateMap( validObject, sourceValue );
            }
            else if ( validObject.constructor === Array ) {

                MapJsonDocSchemaValidator.validateList( validObject, sourceValue );
            }

        } );

    }
}

/**
 * Converts the original map into 2 maps that is easier to work against. This is used internally
 * to wrap the schema object. It is not exported and only used by MapJsonDocSchemaValidator.
 */
class MapObject {

    /**
     * Creates a new MapObject.
     *
     * @param schemaMap The Map to wrap.
     */
    constructor( schemaMap: {} ) {
        this.schemaMap = {};
        this.required = {};
        this.empty = false;

        if ( Object.keys( schemaMap ).length === 0 ) {
            this.empty = true;
        }
        else {

            Object.keys( schemaMap ).forEach( key => {
                let value = schemaMap[key];

                if ( MapObject.verifyKey( key, this.schemaMap ) ) {

                    let realKey: string = MapObject._key( key );

                    this.schemaMap[realKey] = value;
                    this.required[realKey] = MapObject._required( key );
                }
            } );
        }
    }

    /**
     * Validates that key exists among valid keys.
     *
     * @param key The key to check for.
     *
     * @returns {boolean}
     */
    containsKey( key: string ): boolean {
        return this.keySet().includes( key );
    }

    /**
     * @return The internal key set for looking up in.
     */
    keySet(): [] {

        return Object.keys( this.schemaMap );
    }

    /**
     * Returns an object for a specific key.
     * @param key The key to get object for.
     */
    get( key: string ): {} {

        return this.schemaMap[key];
    }

    /**
     * Returns true if the specified key points to a required object.
     *
     * @param key The key to check for required.
     */
    isRequired( key: string ): boolean {

        return this.required[key];
    }

    /**
     * @return true if the valid Map object is empty. In this case anything is allowed.
     */
    isEmpty(): boolean {
        return this.empty;
    }

    /**
     * Returns the key without formatting.
     *
     * @param mapKey The key to "plainify".
     */
    static _key( mapKey ) {

        return mapKey.split( "_" )[0];
    }

    /**
     * Returns true if the specified key indicates a required object.
     *
     * @param mapKey The key to check for required object.
     */
    static _required( mapKey: string ): boolean {

        let parts = mapKey.split( "_" );

        if ( parts.length >= 2 ) {

            return parts[1] === "1";
        }

        return false;
    }

    /**
     * Verifies that the key is correctly formatted.
     *
     * @param mapKey The key to verify.
     * @param source The source of the key. Used for exception message to make things clearer.
     */
    static verifyKey( mapKey: string, source: {} ): boolean {

        let parts: [string] = mapKey.split( "_" );

        if ( parts.length >= 2 && ( parts[1] !== "0" && parts[1] !== "1" ) && parts[1] !== "?" ) {

            throw new Error( `Bad key format! [$mapKey] Should be 'name' or 'name_0' or 'name_1'. ${ safeStringify(source) }` );
        }


        return parts[1] !== '?';
    }

}

