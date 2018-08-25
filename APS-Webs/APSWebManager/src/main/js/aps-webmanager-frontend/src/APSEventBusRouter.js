/**
 * This defines an event bus router API, and is used as a fake interface.
 */
export default class APSEventBusRouter {

    /**
     * sends a message to all listeners.
     *
     * @param headers The headers for the message.
     * @param message The message to send.
     */
    message( headers: {}, message: {} ) {
    }

    /**
     * Subscribes to messages.
     *
     * @param headers  - Relevant headers for subscription.
     * @param callback - Callback to call with messages.
     */
    subscribe( headers: object, callback: func ) {
    }

    /**
     * Unsubscribe from receiving messages.
     *
     * @param headers - The headers used when subscribing.
     * @param callback - The callback to unsubscribe.
     */
    unsubscribe( headers: {}, callback: {} ) {
    }
}


