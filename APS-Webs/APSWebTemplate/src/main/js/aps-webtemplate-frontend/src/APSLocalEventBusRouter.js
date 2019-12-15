import { EVENT_ROUTING, EVENT_ROUTES, ROUTE_OUTGOING, ROUTE_INCOMING } from "./Constants";
import APSBusAddress from "./APSBusAddress";
import APSEventBusRouter from "./APSEventBusRouter";
import APSLogger from "./APSLogger";

/**
 * This represents a router and is responsible for sending and subscribing to messages.
 */
export default class APSLocalEventBusRouter implements APSEventBusRouter {


    /**
     * Creates a new LocalBusRouter.
     */
    constructor(  ) {

        // noinspection JSValidateTypes
        /** @type {object.<string, function(object)>} subscribers */
        this.subscribers = {};

        this.logger = new APSLogger( 'APSLocalEventBusRouter' );
    }

    /**
     * Provides a bus address for the router.
     *
     * @param busAddress The bus address to provide.
     */
    setBusAddress( busAddress: APSBusAddress ) {
        this.busAddress= busAddress;
    }

    /**
     * sends a message to all listeners.
     *
     * @param headers The headers for the message.
     * @param message The message to send.
     */
    message( headers: {}, message: {} ) {
        if (!this.busAddress) throw new Error("Required bus address not provided!");

        if ( headers[EVENT_ROUTING] != null ) {
            let routes = headers[EVENT_ROUTING][ROUTE_OUTGOING];

            for ( let route of routes.split( ',' ) ) {

                // this.logger.debug( `route: ${route}` );

                // I decided to still use the switch statement even though this router only have one
                // single route ... currently!
                // noinspection JSUnfilteredForInLoop,JSRedundantSwitchStatement
                switch ( route ) {
                    case EVENT_ROUTES.CLIENT:
                        let addressSubscribers = this.subscribers[this.busAddress.client];

                        if ( addressSubscribers ) {

                            for ( let callback of addressSubscribers ) {

                                if (callback !== undefined && callback != null) {
                                    callback( message );
                                }
                                else {
                                    this.logger.warn(`Non functional callback found: ${callback} `)
                                }
                            }
                        }
                        break;

                    default:
                        // OK
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
    subscribe( headers: {}, callback: () => mixed ) {
        if (!this.busAddress) throw new Error("Required bus address not provided!");

        if ( headers[EVENT_ROUTING] != null ) {

            let routes = headers[EVENT_ROUTING][ROUTE_INCOMING];

            for ( let route: string of routes.split( ',' ) ) {

                // this.logger.debug( `Subscribe: route: ${route}` );

                // I decided to still use the switch statement even though this router only have one
                // single route ... currently!
                // noinspection JSUnfilteredForInLoop,JSRedundantSwitchStatement
                switch ( route ) {
                    case EVENT_ROUTES.CLIENT:
                        let addressSubscribers = this.subscribers[this.busAddress.client];

                        // IDEA isn't always right about these things ...
                        // noinspection SuspiciousTypeOfGuard,PointlessBooleanExpressionJS
                        if ( addressSubscribers == null || addressSubscribers === undefined ) {

                            addressSubscribers = [];
                            this.subscribers[this.busAddress.client] = addressSubscribers;
                        }

                        addressSubscribers.push( callback );
                        break;

                    default:
                        // OK.
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
    unsubscribe( headers: {}, callback: () => mixed ) {
        if (!this.busAddress) throw new Error("Required bus address not provided!");

        if ( headers[EVENT_ROUTING] != null ) {
            let routes = headers[EVENT_ROUTING][ROUTE_INCOMING];

            for ( let route: string of routes.split( ',' ) ) {
                // this.logger.debug( ` subscribe: route: ${route}` );

                // noinspection JSUnfilteredForInLoop,JSRedundantSwitchStatement
                switch ( route ) {

                    case EVENT_ROUTES.CLIENT:
                        let addressSubscribers = this.subscribers[this.busAddress.client];

                        // noinspection SuspiciousTypeOfGuard,PointlessBooleanExpressionJS
                        if ( addressSubscribers != null && addressSubscribers !== undefined ) {

                            let remix = addressSubscribers.indexOf( callback );
                            addressSubscribers.splice( remix, 1 );
                        }
                        break;

                    default:
                        // OK
                }
            }
        }
        else {
            throw new Error( `No 'routing:' entry in headers: ${headers}!` );
        }
    }

    static validRoutes = {
        message: [ EVENT_ROUTES.CLIENT ],
        subscribe: [ EVENT_ROUTES.CLIENT ],
        unsubscribe: [ EVENT_ROUTES.CLIENT ]
    };

    // noinspection JSMethodCanBeStatic
    /**
     * Returns a list of valid routes for the router.
     */
    getValidRoutes() : { message: [], subscribe: [], unsubscribe: [] } {

        return APSLocalEventBusRouter.validRoutes;
    }
}
