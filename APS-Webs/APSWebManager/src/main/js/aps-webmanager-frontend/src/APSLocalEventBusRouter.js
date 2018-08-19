import { EVENT_ROUTING, EVENT_ROUTES } from "./Constants";
import APSBusAddress from "./APSBusAddress";
import APSEventBusRouter from "./APSEventBusRouter";

/**
 * This represents a router and is responsible for sending and subscribing to messages.
 */
export default class APSLocalEventBusRouter implements APSEventBusRouter {


    /**
     * Creates a new LocalBusRouter.
     *
     * @param busAddress - The address for the app.
     */
    constructor( busAddress: APSBusAddress ) {
        //super();

        this.busAddress = busAddress;

        // noinspection JSValidateTypes
        /** @type {object.<string, function(object)>} subscribers */
        this.subscribers = {};
    }

    /**
     * sends a message to all listeners.
     *
     * @param headers The headers for the message.
     * @param message The message to send.
     */
    message( headers: object, message: object ) {

        if ( headers[EVENT_ROUTING] != null ) {

            for ( let route: string in headers[EVENT_ROUTING].split( ',' ) ) {

                // noinspection JSUnfilteredForInLoop
                switch ( route ) {
                    case EVENT_ROUTES.CLIENT:
                        let addressSubscribers = this.subscribers[this.busAddress.client];

                        if ( addressSubscribers != null ) {

                            for ( let callback of addressSubscribers ) {
                                callback( message );
                            }
                        }
                        break;

                    case EVENT_ROUTES.BACKEND:
                        break;

                    case EVENT_ROUTES.ALL_BACKENDS:
                        break;

                    case EVENT_ROUTES.ALL_CLIENTS:
                        break;

                    default:
                        // noinspection JSUnfilteredForInLoop
                        throw new Error( `Bad routing value: ${route}!` );
                }
            }
        }
        else {
            throw new Error( `No 'routing:' entry in headers: ${headers}!` );
        }
    }

    /**
     * Subscribes to messages.
     *
     * @param headers  - Relevant headers for subscription.
     * @param callback - Callback to call with messages.
     */
    subscribe( headers: object, callback: func ) {

        if ( headers[EVENT_ROUTING] != null ) {

            for ( let route: string in headers[EVENT_ROUTING].split( ',' ) ) {
                // noinspection JSUnfilteredForInLoop
                switch ( route ) {
                    case EVENT_ROUTES.CLIENT:
                        let addressSubscribers = this.subscribers[this.busAddress.client];

                        if ( addressSubscribers == null ) {

                            addressSubscribers = [];
                            this.subscribers[this.busAddress.client] = addressSubscribers;
                        }

                        addressSubscribers.push( callback );
                        break;

                    case EVENT_ROUTES.BACKEND:
                        break;

                    case EVENT_ROUTES.ALL_BACKENDS:
                        break;

                    case EVENT_ROUTES.ALL_CLIENTS:
                        break;

                    default:
                        // noinspection JSUnfilteredForInLoop
                        throw new Error( `Bad routing value: ${route}!` );
                }
            }
        }
        else {
            throw new Error( `No 'routing:' entry in headers: ${headers}!` );
        }
    }

    /**
     * Unsubscribe from receiving messages.
     *
     * @param headers - The headers used when subscribing.
     * @param callback - The callback to unsubscribe.
     */
    unsubscribe( headers: object, callback: func ) {

        if ( headers[EVENT_ROUTING] != null ) {
            for ( let route: string in headers[EVENT_ROUTING].split( ',' ) ) {

                // noinspection JSUnfilteredForInLoop
                switch ( route ) {

                    case EVENT_ROUTES.CLIENT:
                        let addressSubscribers = this.subscribers[this.busAddress.client];

                        if ( addressSubscribers != null ) {

                            let remix = addressSubscribers.indexOf( callback );
                            addressSubscribers.splice( remix, 1 );
                        }
                        break;

                    case EVENT_ROUTES.BACKEND:
                        break;

                    case EVENT_ROUTES.ALL_BACKENDS:
                        break;

                    case EVENT_ROUTES.ALL_CLIENTS:
                        break;

                    default:
                        // noinspection JSUnfilteredForInLoop
                        throw new Error( `Bad routing value: ${route}!` );
                }
            }
        }
        else {
            throw new Error( `No 'routing:' entry in headers: ${headers}!` );
        }
    }
}
