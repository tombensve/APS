export const SANE = Object.freeze( {
    OBJECT: 1,
    ARRAY: 2,
    FUNCTION: 3,
    STRING: 4,
    BOOLEAN: 5,
    NUMBER: 6,
    UNDEFINED: 7
} );

export function saneTypeId( toType ) {

    if ( typeof toType === "undefined" ) {
        return SANE.UNDEFINED;
    }

    if ( toType.constructor === Array ) {
        return SANE.ARRAY;
    }

    if ( toType.constructor === Object ) {
        return SANE.OBJECT;
    }

    if ( typeof toType === "function" ) {
        return SANE.FUNCTION;
    }

    if ( typeof toType === "string" ) {
        return SANE.STRING;
    }

    if ( typeof toType === "boolean" ) {
        return SANE.BOOLEAN;
    }

    if ( typeof toType === "number" ) {
        return SANE.NUMBER;
    }

}