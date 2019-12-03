/**
 * Support for named parameters in form of an object.
 */
export default class NamedParams {

    /**
     * Creates a new NamedParams.
     *
     * @param params An object containing a key and value for each parameter.
     * @param what The object using this util. For display in error messages.
     */
    constructor( params: {}, what: string = "" ) {
        this.params = params;
        this.what = what === "" ? "" : (what + ": ");
    }

    /**
     * This fetches the value of a required parameter. If it does not exists an Error will be thrown.
     *
     * @param name The name of the parameter to get.
     *
     * @returns {*} whatever the parameter value is.
     */
    requiredParam( name: string ) {
        let param = this.params[name];
        if ( param === null || param === undefined ) {
            throw new Error( `${this.what}Required parameter '${name}' is missing` );
        }

        return param;
    }

    /**
     * This fetches the value of a parameter assuming null or undefined is a legal result.
     *
     * @param name The name of the parameter to get.
     *
     * @returns {*} whatever the parameter value is, or null, or undefined.
     */
    param( name: string ) {
        return this.params[name];
    }

    /**
     * This fetches the value of a parameter assuming null or undefined is a legal result.
     *
     * @param name The name of the parameter to get.
     * @param def The default value if parameter is not available.
     *
     * @returns {*} whatever the parameter value is, or null, or undefined.
     */
    paramWithDefault( name: string, def: *) {
        let param = this.params[name];
        if ( !param ) {
            param = def;
        }

        return param;
    }
}