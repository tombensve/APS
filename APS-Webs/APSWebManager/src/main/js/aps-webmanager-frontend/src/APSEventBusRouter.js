/**
 * This defines an event bus router API, and is used as a fake interface. Interestingly enough
 * there is an implements keyword that babel compiles fine. You can not however skip the method
 * bodies and just end with ';'. That fails compilation. Anyhow, APSEventBus only makes use of
 * this, with the exception of the static createBus().
 */
export default class APSEventBusRouter {

    // noinspection ES6ModulesDependencies
    /**
     * Provides a bus address for the router.
     *
     * @param busAddress The bus address to provide.
     */
    setBusAddress( busAddress: APSBusAddress ) {
    }

    /**
     * sends a message to all listeners.
     *
     * @param headers The headers for the message.
     * @param message The message to send.
     */
    message( headers: APSMessageHeaders, message: APSMessage ) {
    }

    /**
     * Subscribes to messages.
     *
     * @param headers  - Relevant headers for subscription.
     * @param callback - Callback to call with messages.
     */
    subscribe( headers: APSMessageHeaders, callback: () => mixed ) {
    }

    /**
     * Unsubscribe from receiving messages.
     *
     * @param headers - The headers used when subscribing.
     * @param callback - The callback to unsubscribe.
     */
    unsubscribe( headers: APSMessageHeaders, callback: () => mixed ) {
    }
}


