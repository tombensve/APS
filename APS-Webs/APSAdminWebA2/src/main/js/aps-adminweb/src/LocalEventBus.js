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

    // static instance = new LocalEventBus();
    //
    // static get INSTANCE() {
    //     return LocalEventBus.instance;
    // }

    /**
     * Creates a new LocalEventBus.
     *
     * @constructor
     */
    constructor() {
        this.subscribers = {};

        /** @type {function(String, Boolean, String)[]} Used to send messages. */
        this.senders = [];

        /** @type {Object[]} Used to subscribe to messages. */
        this.receivers = [];

        // The defaultReceiver does not use the 'global' flag, which is why we need to suppress this warning.
        // noinspection JSUnusedLocalSymbols
        /**
         * The default sender, always added internally.
         *
         * Sends all messages to all subscribers. Note that the default sender does not use
         * the 'global' argument. It is however passed when the sender function is called.
         *
         * @param {String}  address - The address to send to.
         * @param {String}  message - The message to send. Will be JSON in a string.
         * @param {Boolean} global  - True if global message.
         */
        this.defaultSender = ( address, message, global ) => {
            let addressSubscribers = this.subscribers[address];

            if ( addressSubscribers != null ) {

                for ( let subscriber of addressSubscribers ) {
                    subscriber( address, message );
                }
            }
        };

        this.addSender( this.defaultSender );

        // noinspection JSUnusedLocalSymbols
        /**
         * Contains a subscriber function and an unsubscriber function. These functions as the name
         * hints handles subscriptions and unsubscribes. These only subscribes and unsubscribes to
         * messages on this local event bus.
         *
         * @type {{subscriber: LocalEventBus.defaultReceiver.subscriber, unsubscriber: LocalEventBus.defaultReceiver.unsubscriber}}
         */
        this.defaultReceiver = {
            /**
             * The default subscriber. Stores subscribers locally mapped on address listening to.
             *
             * @param {String} address      - The address to subscribe to.
             * @param {function} subscriber - The subscriber function to call on messages.
             * @param {Boolean} global      - True if subscriber is global.
             */
            subscriber: ( address, subscriber, global ) => {
                let addressSubscribers = this.subscribers[address];

                if ( addressSubscribers == null ) {
                    addressSubscribers = [];
                    this.subscribers[address] = addressSubscribers;
                }

                addressSubscribers.push( subscriber );
            },

            /**
             * The default unsubscriber. Removes a previously added subscriber.
             *
             * @param {String}   address    - The adress to unsubscribe from.
             * @param {function} subscriber - The subscription callback to remove.
             * @param {Boolean}  global     - True if the subscriber was subscribed as global.
             */
            unsubscriber: ( address, subscriber, global ) => {
                let addressSubscribers = this.subscribers[address];

                if ( addressSubscribers != null ) {
                    let remix = addressSubscribers.indexOf( subscriber );
                    addressSubscribers.splice( remix, 1 );
                }
            }
        };

        this.addReceiver( this.defaultReceiver )
    }

    /**
     * This adds a subscriber for an address.
     *
     * @param {String} address                      - The address to subscribe to messages from.
     * @param {function(String, String)} subscriber - A function taking an address and a message.
     * @param {Boolean} global                      - Subscribe to global messages.
     */
    subscribe( address, subscriber, global = false ) {

        for ( let receiver of this.receivers ) {

            receiver.subscriber( address, subscriber, global );
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

        for ( let receiver of this.receivers ) {

            receiver.unsubscriber( address, subscriber, global );
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

        for ( let sender of this.senders ) {

            sender( address, message, global );
        }
    }

    /**
     * Adds a router to the bus.
     *
     * @param sender {function(String, Boolean, Object)} - The sender to add.
     */
    addSender( sender ) {

        this.senders.push( sender );
    }

    /**
     * Adds a receiver to the
     *
     * The receiver must be an object with 2 function properties:
     *
     *     subscriber:   function(String, boolean, function)
     *     unsubscriber: function(String, boolean, function)
     *
     * The arguments are _address_, _global_, and the _callback_ to call on messages.
     *
     * __global__ set to true means that the subscriber is not only interested in local messages.
     *
     * @param {Object} receiver - The receiver to add.
     */
    addReceiver( receiver ) {

        this.receivers.push( receiver );
    }

}

export default LocalEventBus;