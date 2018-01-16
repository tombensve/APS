/**
 * This defines an event bus API.
 * <p/>
 * Note that this API exactly matches the API of the vertx3 eventbus. This API is here to make this easier to
 * fake for tests, and to actually provide another implementation if wanted for some reason.
 */
export interface EventBus {

    /**
     * Connects to the the server side event bus bridge.
     *
     * @param {string} url The bridge url.
     */
    connect(url: string): void;

    /**
     * Connects to the the server side event bus bridge.
     *
     * @param {string} url The bridge url.
     * @param defaultHeaders as the name says!
     * @param options as the name says!
     */
    connectWithOpts(url: string, defaultHeaders: any, options: any): void;

    /**
     * Publish a message
     *
     * @param {String} address
     * @param {Object} body
     * @param {Object} [headers]
     */
    publish(address: string, body: any, headers?: any) : void;

    /**
     * Send a message
     *
     * @param {String} address
     * @param {Object} body
     * @param {Function} [replyHandler]
     * @param {Object} [headers]
     */
    send<T>(address: string, body: any, replyHandler?: Function, headers?: any): void;

    /**
     * Register a new handler
     *
     * @param {String} address
     * @param {Function} handler
     * @param {Object} [headers]
     */
    registerHandler<T>(address: string, handler: Function, headers?: any): void;

    /**
     * Unregister a handler
     *
     * @param {String} address
     * @param {Function} handler
     * @param {Object} [headers]
     */
    unregisterHandler<T>(address: string, handler: Function, headers?: any): void;

    /**
     * Closes the connection to the eventbus. After this the instance is useless.
     */
    disconnect() : void
}
