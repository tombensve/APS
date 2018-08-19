//
// The following are routing values that are checked for in the guiProps.routing field.
//

//
// ROUTING
//

export const EVENT_ROUTING = "routing";

export const EVENT_ROUTES = Object.freeze({
    // Locally within the client between components.
    CLIENT:  "client",

    // Backend for the app using round robin strategy on send.
    BACKEND: "backend",

    // Delivers message to all listeners of the address in the whole cluster.
    ALL: "all",

    //
    ALL_CLIENTS: "all:client",

    ALL_BACKENDS: "all:backend",

    LOCAL: "local"
});

/**
 *
 * @param {array<string>} routes
 * @returns {string}
 */
export function multiRoutes(routes) {
    let res = "";
    let comma = "";

    for (let route of routes) {
        res += comma + route;
        comma = " ";
    }

    return res;
}

//
// ADDRESSES
//

export const ADDR_NEW_CLIENT = "aps:new_client";

//
// Events
//

export const EVENT = Object.freeze({
    TYPE: "type",
    TYPES: Object.freeze({
        CHANGE: "change",
        UPDATE: "update",
        DELETE: "delete",
        WANT: "want"
    })
});