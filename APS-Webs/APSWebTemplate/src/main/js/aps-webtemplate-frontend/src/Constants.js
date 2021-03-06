//
// The following are routing values that are checked for in the guiProps.routing field.
//

// If this is a copy of the APSWebManager project then this name should be changed.
export const APP_NAME = "web-demo";

//
// ROUTING
//

export const EVENT_ROUTING = "routing";

export const ROUTE_OUTGOING = "outgoing";
export const ROUTE_INCOMING = "incoming";

export const EVENT_ROUTES = Object.freeze( {
    // Locally within the client between components.
    CLIENT: "client",

    // Backend for the app using round robin strategy on send.
    BACKEND: "backend",

    // Delivers message to all listeners of the address in the whole cluster.
    ALL: "all",

    //
    ALL_CLIENTS: "all:client",

    ALL_BACKENDS: "all:backend",

    LOCAL: "local",

    NONE: "none"
} );

//
// Events
//

export const EVENT = Object.freeze( {
    TYPE: "type",
    TYPES: Object.freeze( {
        CHANGE: "change",
        UPDATE: "update",
        DELETE: "delete",
        WANT: "want"
    } )
} );
