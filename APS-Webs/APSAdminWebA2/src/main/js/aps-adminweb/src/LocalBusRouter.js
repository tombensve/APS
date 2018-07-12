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
     * @param {boolean} global - The global flag.
     */
    send( address, message, global = false ) {
        if ( !global ) {
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
     * @param {boolean} global                    - The global flag.
     */
    subscribe( address, callback, global = false ) {
        if (!global) {
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
     * @param {boolean} global - The global flagh.
     */
    unsubscribe( address, callback, global = false ) {
        let addressSubscribers = this.subscribers[address];

        if ( addressSubscribers != null ) {
            let remix = addressSubscribers.indexOf( callback );
            addressSubscribers.splice( remix, 1 );
        }
    }
}
