/**
 * This is a simple local event bus that by default sends all messages to all current
 * subscribers.
 *
 * ## Types
 *
 * ### Address
 *
 * This is a target group for messages. A message sent to an address will only be received by
 * those listening to that address.
 *
 * ### Routers
 *
 * This class actually does nothing at all! It just provides an API to one or more "routers".
 * Well you can have zero also, but nothing will happen then. This just calls the corresponding
 * subscribe(...), unsubscribe(...), and send(...) method of each added router.
 *
 * The intention with this is to have at least 2 different "routers". One that only sends locally
 * among the subscribers within the JS instance, that is no networking, just calling another object
 * directly. One that uses the Vert.x eventbus JS client over the eventbus bridge to the Vert.x server
 * side. Most of the messages sent and received are only between the local components and it would be
 * a waste to send them out on the network and back again. Some of the messages we want to go to the
 * backend. This will also allow different clients to indirectly communicate with each other.
 *
 * Also if at some later time I decided to use something else than Vert.x for example, then I only
 * need to change the router handling Vert.x. It will not affect the components which only uses
 * this.
 */
class LocalEventBus {

    /**
     * Creates a new LocalEventBus.
     *
     * @constructor
     */
    constructor() {

        // noinspection JSValidateTypes
        /**
         * @type {array} busRouters. These routers must provide the subscribe, unsubscribe, and send methods
         *               and parameters as this API have. These calls will be forwarded to each added router.
         *               Router is a bad name here since each actually takes full responsibility for any
         *               messaging. But it also makes things clear and very flexible. The components only
         *               uses / knows about this class. Where messages go and where they come from is the
         *               responsibility for other code.
         */
        this.busRouters = [];
    }

    /**
     * This adds a subscriber for an address.
     *
     * @param {string} address                      - The address to subscribe to messages from.
     * @param {function(String, String)} subscriber - A function taking an address and a message.
     * @param {string} routing                      - Routing hints.
     */
    subscribe( address, subscriber, routing = "" ) {

        for ( let busRouter of this.busRouters ) {
            busRouter.subscribe( address, subscriber, routing );
        }
    }

    /**
     * Unsubscribes to a previously done subscription.
     *
     * @param {string} address                      - The address of the subscription.
     * @param {function(String, String)} subscriber - The subscriber to unsubscribe.
     * @param {string} routing                     - Routing hints.
     */
    unsubscribe( address, subscriber, routing = "" ) {

        for ( let busRouter of this.busRouters ) {

            busRouter.unsubscribe( address, subscriber, routing );
        }
    }

    /**
     * Sends a message.
     *
     * @param {String} address  - The address to send to.
     * @param {String} message  - The message to send. Note that this must be a JSON string
     * @param {string} routing  - Routing hints. Note that this could be provided in the actual
     *                            message for this method, but subscribe(...) and unsubscribe(...)
     *                            still needs this, so for consistency I pass it along here too.
     *                            It is of course entirely OK for a "router" provider to ignore this
     *                            and use message information instead if it wants.
     */
    send( address, message, routing = "" ) {
        console.log( "EventBus: sending(address:" + address + ", routing:" + routing + "): " + message );

        for ( let busRouter of this.busRouters ) {

            busRouter.send( address, message, routing );
        }
    }

    addBusRouter( busRouter ) {
        this.busRouters.push( busRouter )
    }
}

export default LocalEventBus;
