import { ROUTE_LOCAL } from "./Consts"
import { containsAnyOrBlank } from "./Utils"
/**
 * This represents a router and is responsible for sending and subscribing to messages.
 */
export default class LocalBusRouter {


    /**
     * Creates a new LocalBusRouter.
     */
    constructor() {
        // noinspection JSValidateTypes
        /** @type {object.<string, function(string, string)>} subscribers */
        this.subscribers = {};
    }

    /**
     * Sends a message.
     *
     * @param {string} address - Address to send to.
     * @param {string} message - The message to send.
     * @param {string} routing - Routing hints.
     */
    send( address, message, routing ) {
        console.log("@@@@@@@@@@@@@@@@@@@ routing: " + routing);
        if (containsAnyOrBlank(routing, [ ROUTE_LOCAL  ]) ) {
            let addressSubscribers = this.subscribers[address];

            if ( addressSubscribers != null ) {

                for ( let callback of addressSubscribers ) {
                    callback( address, message );
                }
            }
        }
    }

    /**
     * Subscribes to messages.
     *
     * @param {string} address                    - Address to subscribe to.
     * @param {function(string, string)} callback - Callback to call with messages.
     * @param {string} routing                    - Routing hints.
     */
    subscribe( address, callback, routing ) {
        console.log("@@@@@@@@@@@@@@@@@@@ routing: " + routing);
        if (containsAnyOrBlank(routing, [ ROUTE_LOCAL ]) ) {
            let addressSubscribers = this.subscribers[address];

            if ( addressSubscribers == null ) {
                addressSubscribers = [];
                this.subscribers[address] = addressSubscribers;
            }

            addressSubscribers.push( callback );
        }
    }

    /**
     * Unsubscribe from receiving messages.
     *
     * @param {string} address - The address to unsubscribe for.
     * @param {function(string, string)} callback - The callback to unsubscribe.
     * @param {string} routing - Routing hints.
     */
    unsubscribe( address, callback, routing ) {
        console.log("@@@@@@@@@@@@@@@@@@@ routing: " + routing);
        if (containsAnyOrBlank(routing, [ ROUTE_LOCAL ]) ) {
            let addressSubscribers = this.subscribers[address];

            if ( addressSubscribers != null ) {
                let remix = addressSubscribers.indexOf( callback );
                addressSubscribers.splice( remix, 1 );
            }
        }
    }
}
