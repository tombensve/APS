/**
 * This defines an event bus API.
 * <p/>
 * Note that this API exactly matches the API of the vertx3 eventbus. This API is here to make this easier to fake for tests,
 * and to actually provide another implementation if wanted for some reason.
 */
export interface EventBusProvider {

    /**
     * Sends a message that will have one received and one reply.
     *
     * @param address The address of the message.
     * @param message The message to send.
     * @param replyCallback This will be called with a reply to this specific message.
     * @param headers Array of data, depends on implementation. Optional.
     */
    send(address : string, message : string, replyCallback : Function, headers? : Array<string> ) : EventBusProvider;

    /**
     * Publishes a message to all listeners on the bus.
     *
     * @param address The address of the message.
     * @param message The message to publish.
     * @param headers Array of data, depends on implementation. Optional.
     */
    publish(address : string, message : string, headers? : Array<string>) : EventBusProvider;

    /**
     * Adds a handler for when messages are received.
     *
     * @param address The address to listen to.
     * @param callback The callback that will be called with new messges.
     * @param headers Filter messages on these headers. Optional.
     */
    addHandler (address : string, callback : Function, headers? : Array<string>) : EventBusProvider;

    /**
     * Removes a previously added handler.
     *
     * @param address The address to listen to.
     * @param callback The callback that should be removed.
     * @param headers Filer messages on these headers. Optional.
     */
    removeHandler(address : string, callback : Function, headers? : Array<string>) : EventBusProvider;

    /**
     * Closes the connection to the eventbus. After this the instance is useless.
     */
    close() : void;

}
