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
 * ### Router
 *
 * This routes/sends messages. The default router sends all messages to all subscribers for the address.
 * The default router is only local.
 *
 * A router is a function taking an address, a message (JSON), and a boolean global flag.
 *
 * The global flag is for routers that forwards messages on the network. They should only do that
 * if the global flag is true.
 *
 */
class LocalEventBus {

    static instance = new LocalEventBus(null);

    static get INSTANCE() {
        return this.instance;
    }

    /**
     * Creates a new LocalEventBus.
     *
     * @constructor
     */
    constructor() {
        this.subscribers = {};

        this.routers = [];

        // Sends all messages to all subscribers. Note that the default router does not use
        // the 'global' argument. It is however passed when the router function is called.
        // noinspection JSUnusedLocalSymbols
        this.defaultRouter = function (address, message, global = false) {
            let addressSubscribers = this.subscribers[address];

            if (addressSubscribers != null) {

                for (let subscriber of addressSubscribers) {
                    subscriber(address, message)
                }
            }
        };

        this.routers.push(this.defaultRouter);

    }

    // noinspection JSUnusedGlobalSymbols
    /**
     * This adds a subscriber for an address.
     *
     * @param {string} address                        - The address to subscribe to messages from.
     * @param {function(string, string)} subscriber   - A function taking and address and a message. It
     *                                                  will be called with messages.
     */
    subscribe(address, subscriber) {
        let addressSubscribers = this.subscribers[address];

        if (addressSubscribers == null) {
            addressSubscribers = [];
            this.subscribers[address] = addressSubscribers;
        }

        addressSubscribers.push(subscriber)
    }

    // noinspection JSUnusedGlobalSymbols
    /**
     * Unsubscribes to a previously done subscription.
     *
     * @param {string} address                        - The address of the subscription.
     * @param {function(address, message)} subscriber - The subscriber to unsubscribe.
     */
    unsubscribe(address, subscriber) {
        let addressSubscribers = this.subscribers[address];

        if (addressSubscribers != null) {
            let remix = addressSubscribers.indexOf(subscriber);
            addressSubscribers.splice(remix, 1);
        }
    }

    // noinspection JSUnusedGlobalSymbols
    /**
     * Adds a router to the bus.
     *
     * @param router {function(address, message, global)} - The router to add.
     */
    addRouter(router) {
        this.routers.push(router);
    }

    /**
     * Sends a message.
     *
     * @param {string}  address - The address to send to.
     * @param {object}  message - The message to send in JSON format.
     * @param {boolean} global  - If true the message can be routed over the network.
     */
    send(address, message, global = false) {
        let jsonMsg = JSON.stringify(message);
        for (let router of this.routers) {
            router(address, jsonMsg, global);
        }
    }
}

// noinspection JSUnusedGlobalSymbols
export default LocalEventBus;