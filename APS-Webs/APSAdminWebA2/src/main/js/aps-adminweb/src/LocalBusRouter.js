import { HDR_ROUTING, ROUTE_LOCAL } from "./Constants";

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
     * Validates that the operation is valid for this router based on header info.
     *
     * @param headers The headers to check for validity.
     * @returns {boolean} true or false.
     *
     * @private
     */
    static valid( headers ) {

        return headers[HDR_ROUTING] != null && headers[HDR_ROUTING].indexOf( ROUTE_LOCAL ) >= 0;
    }

    /**
     * Sends a message. This just calls publish since there is not difference between send and publish
     * locally. Vert.x on a cluster sends to only one listener at a time in a round robin fashion for
     * load balancing. But locally we are executing within the same JS environment.
     *
     * @param {string} address - Address to send to.
     * @param {object} headers - The headers for the message.
     * @param {object} message - The message to send.
     */
    send( address, headers , message) {

        this.publish( address, message, headers );
    }

    /**
     * Publishes a message to all listeners.
     *
     * @param {string} address
     * @param {object} headers
     * @param {object} message
     */
    publish( address, headers, message ) {

        if ( LocalBusRouter.valid( headers ) ) {

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

        if ( LocalBusRouter.valid( headers ) ) {

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
     * @param {object} headers - The headers used when subscribing.
     * @param {function(string, string)} callback - The callback to unsubscribe.
     */
    unsubscribe( address, headers, callback ) {

        if ( LocalBusRouter.valid( headers ) ) {

            let addressSubscribers = this.subscribers[address];

            if ( addressSubscribers != null ) {

                let remix = addressSubscribers.indexOf( callback );
                addressSubscribers.splice( remix, 1 );
            }
        }
    }
}
