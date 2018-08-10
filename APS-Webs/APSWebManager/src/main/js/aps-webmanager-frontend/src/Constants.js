//
// The following are routing values that are checked for in the guiProps.routing field.
//

//
// ROUTING
//

export const EVENT_ROUTING = "routing";

export const EVENT_ROUTES = Object.freeze({
    // Only sent locally within the client between components.
    CLIENT:  "client",

    // Leaves the client and goes out on the network. In most cases when this is used you also want the "local" value,
    // like "client,backend".
    BACKEND: "backend",

    // Delivers message to all listeners of the address in the whole cluster.
    CLUSTER: "cluster"
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
        comma = ",";
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

export const EVENT = {
    TYPE: "eventType",
    TYPES: {
        CHANGE: "change",
        ACTION: "action",
        ACTIONS: {
            SUBMIT: "submit"
        }
    }
};