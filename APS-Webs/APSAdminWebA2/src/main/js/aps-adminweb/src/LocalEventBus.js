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
 * ### Sender
 *
 * A sender is a function that is called by the send(address, global, message) method. There can be more
 * than one sender, but only one default sender is created with the instance. The default sender only
 * sends to subscribes of this local bus.
 *
 * ### Receiver
 *
 * A receiver handles subscribe and unsubscribe for the bus. There can be more than one of these too.
 * A default provider is provided that only subscribes to messages on this local bus.
 *
 * ### General
 *
 * So why add senders and receivers ? Well, this bus is used by all APS components. They can communicate
 * with each other over this bus. But as is they cannot communicate outside of the specific client they
 * are in. Note the global flag used. As is, it has no function. It is intended for other senders and
 * receivers that can communicate over the network. Those will listen to the global flag and forward
 * messages to another networking bus. External receivers will subscribe using networking bus.
 *
 * I didn't want to bake this functionality into this code. Since APS currently uses Vert.x it will
 * also provide a sender and receiver using the Vert.x eventbus. But with this design it is easy to
 * supply whatever implementation.
 *
 */
class LocalEventBus {

    /**
     * Creates a new LocalEventBus.
     *
     * @constructor
     */
    constructor() {

        // noinspection JSValidateTypes
        /** @type {array} busRouters */
        this.busRouters = [];
    }

    /**
     * This adds a subscriber for an address.
     *
     * @param {String} address                      - The address to subscribe to messages from.
     * @param {function(String, String)} subscriber - A function taking an address and a message.
     * @param {Boolean} global                      - Subscribe to global messages.
     */
    subscribe( address, subscriber, global = false ) {

        for ( let busRouter of this.busRouters ) {
            busRouter.subscribe( address, subscriber, global );
        }
    }

    /**
     * Unsubscribes to a previously done subscription.
     *
     * @param {String} address                      - The address of the subscription.
     * @param {function(String, String)} subscriber - The subscriber to unsubscribe.
     * @param {Boolean} global                      - True if the subscription to unsubscribe is global.
     */
    unsubscribe( address, subscriber, global = false ) {

        for ( let busRouter of this.busRouters ) {

            busRouter.unsubscribe( address, subscriber, global );
        }
    }

    /**
     * Sends a message.
     *
     * @param {String}  address - The address to send to.
     * @param {String}  message - The message to send. Note that this must be a JSON string
     * @param {Boolean} global  - If true the message can be routed over the network. Defaults to false.
     */
    send( address, message, global = false ) {
        console.log( "EventBus: sending(address:" + address + ", global:" + global + "): " + message );

        for ( let busRouter of this.busRouters ) {

            busRouter.send( address, message, global );
        }
    }

    addBusRouter( busRouter ) {
        this.busRouters.push( busRouter )
    }
}

export default LocalEventBus;

// /**
//  * This represents the API for event bus routers. Multiple such can be added to the bus instance, and
//  * all will be called on each send, subscribe or unsubscribe. It is upp to each router to determine
//  * from message content what it should do, if anything.
//  */
// class BusRouter {
//
//     /**
//      * Sends a message.
//      *
//      * @param {string} address - Address to send to.
//      * @param {string} message - The message to send.
//      * @param {boolean} global - The global flag.
//      */
//     send( address, message, global = false ) {
//     }
//
//     /**
//      * Subscribes to messages.
//      *
//      * @param {string} address                    - Address to subscribe to.
//      * @param {function(string, string)} callback - Callback to call with messages.
//      * @param {boolean} global                    - The global flag.
//      */
//     subscribe( address, callback, global = false ) {
//     }
//
//     /**
//      * Unsubscribe from receiving messages.
//      *
//      * @param {string} address - The address to unsubscribe for.
//      * @param {function(string, string)} callback - The callback to unsubscribe.
//      * @param {boolean} global - The global flagh.
//      */
//     unsubscribe( address, callback, global = false ) {
//     }
// }
