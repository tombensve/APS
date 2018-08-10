import { EVENT_ROUTING, EVENT_ROUTES } from "./Constants";

/**
 * This represents a router and is responsible for sending and subscribing to messages.
 */
export default class LocalBusRouter {


    /**
     * Creates a new LocalBusRouter.
     */
    constructor() {

        // noinspection JSValidateTypes
        /** @type {object.<string, function(object)>} subscribers */
        this.subscribers = {};
    }

    /**
     * sends a message to all listeners.
     *
     * @param {string} address
     * @param {object} headers
     * @param {object} message
     */
    message( address, headers, message ) {

        if ( headers[EVENT_ROUTING] === EVENT_ROUTES.CLIENT ) {

            let addressSubscribers = this.subscribers[address];

            if ( addressSubscribers != null ) {

                for ( let callback of addressSubscribers ) {
                    callback( message );
                }
            }
        }
    }

    /**
     * Subscribes to messages.
     *
     * @param {string} address                    - Address to subscribe to.
     * @param {object} headers                    - Relevant headers for subscription.
     * @param {function(string, string)} callback - Callback to call with messages.
     */
    subscribe( address, headers, callback ) {

        let addressSubscribers = this.subscribers[address];

        if ( addressSubscribers == null ) {

            addressSubscribers = [];
            this.subscribers[address] = addressSubscribers;
        }

        addressSubscribers.push( callback );
    }

    /**
     * Unsubscribe from receiving messages.
     *
     * @param {string} address - The address to unsubscribe for.
     * @param {object} headers - The headers used when subscribing.
     * @param {function(string, string)} callback - The callback to unsubscribe.
     */
    unsubscribe( address, headers, callback ) {

        let addressSubscribers = this.subscribers[address];

        if ( addressSubscribers != null ) {

            let remix = addressSubscribers.indexOf( callback );
            addressSubscribers.splice( remix, 1 );
        }

    }
}
