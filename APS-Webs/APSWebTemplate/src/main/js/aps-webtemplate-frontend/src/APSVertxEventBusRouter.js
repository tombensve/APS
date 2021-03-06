import EventBus from 'vertx3-eventbus-client'
import { EVENT_ROUTES, EVENT_ROUTING, ROUTE_INCOMING, ROUTE_OUTGOING } from "./Constants"
import APSLogger from "./APSLogger"
import APSEventBusRouter from "./APSEventBusRouter"
import APSBusAddress from "./APSBusAddress"
import APSAlerter from "./APSAlerter"
import { apsObject } from "./Utils"

/**
 * This represents a router and is responsible for sending and subscribing to messages.
 *
 * This specific router routes to the Vert.x event bus bridge (websocket).
 */
export default class APSVertxEventBusRouter implements APSEventBusRouter {

    /**
     * Creates a new APSVertxEventBusRouter.
     */
    constructor( alerter: APSAlerter ) {

        this.alerter = alerter;

        this.logger = new APSLogger( "VertxEventBusRouter" );

        /**
         * A cache of messages and subscribers for when these are registered before the bus is
         * connected.
         *
         * @type {{messages: Array, subscribers: Array}}
         */
        this.cache = {
            messages: [],
            subscribers: []
        };

        this.activeSubscribers = [];

        this.callbackHandlers = new Map();

        /** Will change to true when bus is connected. */
        this.busReady = false;

        /** Our bus instance. */
        this.re_startBus();

        /** Callback for when bus is connected. */
        this.eventBus.onopen = () => {

            this.onBusOpen();
        };

        this.eventBus.onclose = ( e ) => {
            this.onBusClose( e );
        };

        this.eventBus.onerror = ( err ) => {
            this.logger.error( err );
        };
    }

    /**
     * Provides a bus address for the router.
     *
     * @param busAddress The bus address to provide.
     */
    setBusAddress( busAddress: APSBusAddress ) {
        this.busAddress = busAddress;
    }

    re_startBus() {
        // noinspection JSValidateTypes
        this.eventBus = new EventBus( window.location.protocol + "//" + window.location.hostname +
            ( window.location.port !== "" ? ( ":" + window.location.port ) : "" ) + "/eventbus/", {} );

        // This should be empty first time.
        for ( let sub of this.activeSubscribers ) {
            this.subscribe( sub.headers, sub.callback );
        }
    }

    /**
     * Handles the bus becoming available and triggers any actions that has been done before the bus was
     * connected.
     */
    onBusOpen() {
        console.info( "Vertx EventBuss is now connected!" );

        // This seem to help the problem of the bus shutting down after 2-4 minutes. It now takes 5-7 minutes!
        // This problem is however only in Safari on OSX, other browsers handles websocket much better. Interestingly
        // enough Safari on iOS does not suffer from this problem, only on OSX.
        // noinspection JSUnresolvedFunction
        this.eventBus.enableReconnect( true );

        this.busReady = true;

        for ( let sub of this.cache.subscribers ) {
            this.logger.info( "Executing cached subscription!" );
            this.subscribe( sub.headers, sub.callback );
        }
        this.cache.subscribers = [];

        this.subscribes = [];

        for ( let msg of this.cache.messages ) {
            this.logger.info( "Executing cached send!" );
            // noinspection JSUnresolvedFunction
            this.message( msg.headers, msg.message );
        }
        this.cache.messages = [];

        this.sendMsgs = [];

        this.alerter.hide( "aps-default-alert" );

        // this.keepAlive = setInterval( () => {
        //     this.message( { routing: { outgoing: `${EVENT_ROUTES.BACKEND}` } }, {
        //         aps: { type: "keep-alive" },
        //         content: {}
        //     } );
        // }, 20000 );
    }

    // noinspection JSUnusedLocalSymbols
    onBusClose( e ) {
        this.logger.info( "The eventbus has been closed!" );
        this.alerter.alert( "aps-default-alert", "## No contact!\n\nWe are currently alone! " +
            "Waiting to connect to our universe again ..." );
    }

    /**
     * Sends a message.
     *
     * @param headers - The headers for the message.
     * @param message - The message to message.
     */
    message( headers: {}, message: {} ) {
        if ( !this.busAddress ) throw new Error( "Required bus address not provided!" );

        headers = apsObject( headers );
        message = apsObject( message );

        this.tries = 0;

        try {

            if ( this.busReady ) {

                // Note here that we do not imitate the vertx event bus totally. We only have one message(...)
                // method to send messages. It is the 'routing' in the header that determines if a send or a
                // publish should be done.

                let routes = headers[EVENT_ROUTING];

                if (routes !== undefined && routes != null) {

                    // A route starting with 'address:' is not a route but a direct address. This allows for
                    // components to have a direct and unique address handled by a direct counterpart on the
                    // backend, rather than sending to a more generic handler receiving messages from multiple
                    // components.
                    if ( routes[ROUTE_OUTGOING].startsWith( "address:" ) ) {
                        this.eventBus.send( routes.substring( 7 ), message, headers, null )
                    }
                    // noinspection SuspiciousTypeOfGuard
                    else {
                        for ( let route: string of routes[ROUTE_OUTGOING].split( ',' ) ) {

                            switch ( route ) {
                                case EVENT_ROUTES.BACKEND:
                                    // this.logger.debug( `Sending to BACKEND with headers: ${headers.display()} and message: ${JSON.stringify( message )}` );

                                    // noinspection JSUnresolvedFunction
                                    this.eventBus.send( this.busAddress.backend, message, headers, null );
                                    break;

                                case EVENT_ROUTES.ALL:
                                    // this.logger.debug( `Publishing to ALL with headers: ${headers.display()} and message: ${JSON.stringify( message )}` );

                                    // noinspection JSUnresolvedFunction
                                    this.eventBus.publish( this.busAddress.all, message, headers );
                                    break;

                                case EVENT_ROUTES.ALL_BACKENDS:
                                    // this.logger.debug( `Publishing to ALL_BACKEND with headers: ${headers.display()} and message: ${JSON.stringify( message )}` );

                                    // noinspection JSUnresolvedFunction
                                    this.eventBus.publish( this.busAddress.allBackends, message, headers );
                                    break;

                                case EVENT_ROUTES.ALL_CLIENTS:
                                    // this.logger.debug( `Publishing to ALL_CLIENTS with headers: ${headers.display()} and message: ${JSON.stringify( message )}` );

                                    // noinspection JSUnresolvedFunction
                                    this.eventBus.publish( this.busAddress.allClients, message, headers );
                                    break;

                                default:
                                // OK
                            }
                        }
                    }
                }
                else {
                    // noinspection ExceptionCaughtLocallyJS
                    throw new Error( `No 'routing:' entry in headers: ${headers.display()}!` );
                }
            }
            else {
                this.cache.messages.push( { message: message, headers: headers } );
            }

            this.tries = 0;
        }
        catch ( error ) {

            this.tries = this.tries + 1;
            if ( this.tries <= 1 ) {
                this.re_startBus();
                message( headers, message );
            }
            else {
                if ( this.tries <= 3 )
                    throw new Error( `Vert.x eventbus has failed: ${error}` )
            }
        }
    }

    /**
     * Subscribes to messages.
     *
     * @param headers  - The relevant headers for the subscription.
     * @param callback - Callback to call with messages.
     */
    subscribe( headers: {}, callback: () => mixed ) {
        if ( !this.busAddress ) throw new Error( "Required bus address not provided!" );

        headers = apsObject( headers );

        // this.logger.debug( `Subscribing with headers: ${headers.display()}` );

        this.activeSubscribers.push( { headers: headers, callback: callback } );

        // This is a wrapper handler that extracts the 'body' part of the message and
        // forwards to the callback.
        let handler = ( alwaysNull, message ) => {

            message = apsObject( message );

            // this.logger.debug( "RECEIVED: " + message.display() );

            if ( typeof message !== "undefined" ) {
                callback( message['body'] );
            }
            else {
                this.logger.error( "Got an 'undefined' message!" );
            }
        };

        this.callbackHandlers.set( callback, handler );

        if ( this.busReady ) {

            let routes = headers[EVENT_ROUTING];

            // noinspection SuspiciousTypeOfGuard
            if ( routes != null && routes !== undefined ) {
                for ( let route: string of routes[ROUTE_INCOMING].split( ',' ) ) {

                    switch ( route ) {

                        case EVENT_ROUTES.CLIENT:
                            // noinspection JSUnresolvedFunction
                            this.eventBus.registerHandler( this.busAddress.client, headers, handler );
                            break;

                        case EVENT_ROUTES.BACKEND:
                            // Note to self: We are on the frontend, should thereby not handle backend routes!!!
                            break;

                        case EVENT_ROUTES.ALL:
                            // noinspection JSUnresolvedFunction
                            this.eventBus.registerHandler( this.busAddress.all, headers, handler );
                            break;

                        case EVENT_ROUTES.ALL_BACKENDS:
                            // Note to self: We are on the frontend, should thereby not handle backend routes!!!
                            break;

                        case EVENT_ROUTES.ALL_CLIENTS:
                            // noinspection JSUnresolvedFunction
                            this.eventBus.registerHandler( this.busAddress.allClients, headers, handler );
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
        else {

            this.cache.subscribers.push( { headers: headers, callback: callback } )
        }
    }

    /**
     * Unsubscribe from receiving messages.
     *
     * @param headers  - The headers used to subscribe.
     * @param callback - The callback to unsubscribe.
     */
    unsubscribe( headers: {}, callback: () => mixed ) {
        if ( !this.busAddress ) throw new Error( "Required bus address not provided!" );

        headers = apsObject( headers );

        // this.logger.debug( `Unsubscribing with headers: ${headers.display()}` );

        // I expect the bus to be upp by the time this is done :-)

        let handler: Function = this.callbackHandlers.get( callback );
        this.callbackHandlers.delete( callback );

        if ( this.busReady ) {
            let routes = headers[EVENT_ROUTING];

            // noinspection SuspiciousTypeOfGuard
            if ( routes != null && routes !== undefined ) {

                for ( let route: string of routes[ROUTE_INCOMING].split( ',' ) ) {

                    switch ( route ) {
                        case EVENT_ROUTES.CLIENT:
                            // noinspection JSUnresolvedFunction
                            this.eventBus.unregisterHandler( this.busAddress.client, handler );
                            break;

                        case EVENT_ROUTES.BACKEND:
                            break;

                        case EVENT_ROUTES.ALL:
                            // noinspection JSUnresolvedFunction
                            this.eventBus.unregisterHandler( this.busAddress.all, handler );
                            break;

                        case EVENT_ROUTES.ALL_BACKENDS:
                            break;

                        case EVENT_ROUTES.ALL_CLIENTS:
                            // noinspection JSUnresolvedFunction
                            this.eventBus.unregisterHandler( this.busAddress.allClients, handler );
                            break;

                        default:
                            // OK.
                    }
                }
            }
            else {
                throw new Error( `No 'routing:' entry in headers: ${headers.display()}!` );
            }
        }
        else {
            this.logger.info( "Vert.x eventbus not open at unsubscribe! This is most probably OK." )
        }

        let newSubs = [];
        for ( let sub of this.activeSubscribers ) {

            if ( sub.headers.incoming === headers.incoming && sub.headers.outgoing === headers.outgoing &&
                sub.callback === callback ) {
                // Do nothing
            }
            else {

                newSubs.push( sub );
            }
        }
        this.activeSubscribers = newSubs;
    }

    static validRoutes = {
        message: [ EVENT_ROUTES.BACKEND, EVENT_ROUTES.ALL, EVENT_ROUTES.ALL_BACKENDS, EVENT_ROUTES.ALL_CLIENTS ],
        subscribe: [ EVENT_ROUTES.CLIENT, EVENT_ROUTES.ALL, EVENT_ROUTES.ALL_CLIENTS ],
        unsubscribe: [ EVENT_ROUTES.CLIENT, EVENT_ROUTES.ALL, EVENT_ROUTES.ALL_CLIENTS ]
    };

    // noinspection JSMethodCanBeStatic
    /**
     * Returns a list of valid routes for the router.
     */
    getValidRoutes() : { message: [], subscribe: [], unsubscribe: [] } {

        return APSVertxEventBusRouter.validRoutes;

    }

}
