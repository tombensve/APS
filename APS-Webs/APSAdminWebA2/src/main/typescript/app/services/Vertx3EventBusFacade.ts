import { Injectable       } from "@angular/core";
import { EventBusProvider } from './EventBusProvider';

/**
 * External declaration of EventBus part of vertx3-eventbus-client.
 * <p/>
 * This is required in index.html:
 * <pre>
 *     <script src="node_modules/sockjs-client/dist/sockjs.min.js"></script>
 *     <script src="node_modules/vertx3-eventbus-client/vertx-eventbus.js"></script>
 * </pre>
 */
declare class EventBus {
    public constructor( url : string, options : Object);

    public onerror : Function;
    public send(address : string, message : string, headers : Array<string>, replyCallback : Function) : Function;
    public publish(address : string, message : string, headers : Array<string>) : Function;
    public registerHandler(address : string, headers : Array<string>, callback : Function) : Function;
    public unregisterHandler(address : string, headers : Array<string>, callback : Function) : Function;
    public close() : Function;
}

/**
 * Provides a facade around the JavaScript EventBusProvider code to make it play better with TypeScript.
 */
@Injectable()
export class Vertx3EventBusFacade implements EventBusProvider {
    //
    // Private Members
    //

    /** Our real eventbus client instance. */
    private eventBus : EventBus;

    private errMsg : string;

    //
    // Constructors
    //

    /**
     * Creates a new EventBusFacade
     * @param url
     * @param options
     */
    public constructor(url : string, options : Object) {
        this.eventBus = new EventBus(url, options);
        this.eventBus.onerror = (err: Error) : void => {
            console.log(err);
            this.errMsg = err.toString();
            this.eventBus = null;
        };
    }

    //
    // Methods
    //

    private validate() : void {
        if (this.eventBus == null) {
            throw new Error(`EventBus not connected! [${this.errMsg}]`);
        }
    }

    /**
     * Sends a message that will have one received and one reply.
     *
     * @param address The address of the message.
     * @param message The message to send.
     * @param replyCallback This will be called with a reply to this specific message.
     * @param headers Array of data, depends on implementation. Optional.
     */
    public send(address : string, message : string, replyCallback : Function, headers? : Array<string> ) : Vertx3EventBusFacade {
        this.validate();

        this.eventBus.send(address, message, headers, replyCallback);

        return this;
    }

    /**
     * Publishes a message to all listeners on the bus.
     *
     * @param address The address of the message.
     * @param message The message to publish.
     * @param headers Array of data, depends on implementation. Optional.
     */
    public publish(address : string, message : string, headers? : Array<string>) : Vertx3EventBusFacade {
        this.validate();

        this.eventBus.publish(address, message, headers);

        return this;
    }

    /**
     * Adds a handler for when messages are received.
     *
     * @param address The address to listen to.
     * @param callback The callback that will be called with new messges.
     * @param headers Filter messages on these headers. Optional.
     */
    public addHandler (address : string, callback : Function, headers : Array<string> ) : Vertx3EventBusFacade {
        this.validate();

        this.eventBus.registerHandler(address, headers, callback);

        return this;
    }

    /**
     * Removes a previously added handler.
     *
     * @param address The address to listen to.
     * @param callback The callback that should be removed.
     * @param headers Filer messages on these headers. Optional.
     */
    public removeHandler(address : string, callback : Function, headers : Array<string>) : Vertx3EventBusFacade {
        this.validate();

        this.eventBus.unregisterHandler(address, headers, callback);

        return this;
    }

    /**
     * Closes the connection to the eventbus. After this the instance is useless.
     */
    public close() : void {
        if (this.eventBus != null) {
            this.eventBus.close();
        }
    }
}
