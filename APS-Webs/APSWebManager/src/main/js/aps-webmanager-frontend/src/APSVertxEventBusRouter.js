import EventBus from 'vertx3-eventbus-client'
import { EVENT_ROUTES, EVENT_ROUTING } from "./Constants"
import APSLogger from "./APSLogger"
import APSEventBusRouter from "./APSEventBusRouter";
import APSBusAddress from "./APSBusAddress";

/**
 * This represents a router and is responsible for sending and subscribing to messages.
 */
export default class APSVertxEventBusRouter implements APSEventBusRouter {

    /**
     * Creates a new LocalBusRouter.
     */
    constructor( busAddress: APSBusAddress ) {

        this.busAddress = busAddress;

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

        this.callbackHandlers = new Map();

        /** Will change to true when bus is connected. */
        this.busReady = false;

        /** Our bus instance. */
        // noinspection JSValidateTypes
        this.eventBus = new EventBus( window.location.protocol + "//" + window.location.hostname +
            ( window.location.port !== "" ? ( ":" + window.location.port ) : "" ) + "/eventbus/", {} );

        /** Callback for when bus is connected. */
        this.eventBus.onopen = () => {

            this.onBusOpen();
        }

    }

    /**
     * Handles the bus becoming available and triggers any actions that has been done before the bus was
     * connected.
     */
    onBusOpen() {
        console.info( "Vertx EventBuss is now connected!" );

        this.busReady = true;

        for ( let sub of this.cache.subscribers ) {
            this.logger.info( "Executing cached subscription!" );
            this.subscribe( sub.headers, sub.callback );
        }

        this.subscribes = [];

        for ( let msg of this.cache.messages ) {
            this.logger.info( "Executing cached send!" );
            // noinspection JSUnresolvedFunction
            this.message( msg.headers, msg.message );
        }

        this.sendMsgs = [];
    }

    /**
     * Sends a message.
     *
     * @param headers - The headers for the message.
     * @param message - The message to message.
     */
    message( headers: object, message: object ) {
        this.logger.debug( "Sending with headers: {} and message: {}", headers, message );

        if ( this.busReady ) {

            // Note here that we do no imitate the vertx event bus totally. We only have one message(...)
            // method to send messages. It is the 'routing' in the header that determines if a send or a
            // publish should be done.

            if ( headers[EVENT_ROUTING] != null ) {
                for ( let route: string in headers[EVENT_ROUTING].split( ',' ) ) {
                    // noinspection JSUnfilteredForInLoop
                    switch ( route ) {
                        case EVENT_ROUTES.CLIENT:
                            break;

                        case EVENT_ROUTES.BACKEND:
                            // noinspection JSUnresolvedFunction
                            this.eventBus.send( this.busAddress.backend, message, headers, null );
                            break;

                        case EVENT_ROUTES.ALL:
                            // noinspection JSUnresolvedFunction
                            this.eventBus.publish( this.busAddress.all, message, headers );
                            break;

                        case EVENT_ROUTES.ALL_BACKENDS:
                            // noinspection JSUnresolvedFunction
                            this.eventBus.publish( this.busAddress.allBackends, message, headers );
                            break;

                        case EVENT_ROUTES.ALL_CLIENTS:
                            // noinspection JSUnresolvedFunction
                            this.eventBus.publish( this.busAddress.allClients, message, headers );
                            break;

                        default:
                            // noinspection JSUnfilteredForInLoop
                            throw new Error( `Bad routing value: ${route}!` );
                    }
                }
            }
            else {
                //this.logger.error(`No 'routing:' entry in headers: ${headers}!`);
                throw new Error( `No 'routing:' entry in headers: ${headers}!` );
            }
        }
        else {
            this.cache.messages.push( { message: message, headers: headers } );
        }
    }

    /**
     * Subscribes to messages.
     *
     * @param headers  - The relevant headers for the subscription.
     * @param callback - Callback to call with messages.
     */
    subscribe( headers: object, callback: func ) {
        this.logger.debug( `Subscribing with headers: ${headers}` );

        // This is a wrapper handler that extracts the 'body' part of the message and
        // forwards to the callback.
        let handler = ( alwaysNull, message ) => {
            this.logger.debug( "RECEIVED: " + JSON.stringify( message ) );
            // this.logger.debug("CALLBACK: " + callback);

            if ( typeof message !== "undefined" ) {
                // For some reason we get the full internal vertx eventbus message, not just
                // the client relevant 'body' part.
                callback( message['body'] );
            }
            else {
                this.logger.error( "Got an 'undefined' message!" );
            }
        };

        this.callbackHandlers.set( callback, handler );

        if ( this.busReady ) {

            if ( headers[EVENT_ROUTING] != null ) {
                for ( let route: string in headers[EVENT_ROUTING].split( ',' ) ) {
                    // noinspection JSUnfilteredForInLoop
                    switch ( route ) {
                        case EVENT_ROUTES.CLIENT:
                            // noinspection JSUnresolvedFunction
                            this.eventBus.registerHandler( this.busAddress.client, headers, handler );
                            break;

                        case EVENT_ROUTES.BACKEND:
                            break;

                        case EVENT_ROUTES.ALL_BACKENDS:
                            break;

                        case EVENT_ROUTES.ALL_CLIENTS:
                            // noinspection JSUnresolvedFunction
                            this.eventBus.registerHandler( this.busAddress.allClients, headers, handler );
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
    unsubscribe( headers: object, callback: func ) {
        this.logger.debug( `Unsubscribing with headers: ${headers}` );

        // I expect the bus to be upp by the time this is done :-)

        let handler: func = this.callbackHandlers.get( callback );
        this.callbackHandlers.delete( callback );

        if ( headers[EVENT_ROUTING] != null ) {
            for ( let route: string in headers[EVENT_ROUTING].split( ',' ) ) {
                // noinspection JSUnfilteredForInLoop
                switch ( route ) {
                    case EVENT_ROUTES.CLIENT:
                        // noinspection JSUnresolvedFunction
                        this.eventBus.unregisterHandler( this.busAddress.client, handler );
                        break;

                    case EVENT_ROUTES.BACKEND:
                        break;

                    case EVENT_ROUTES.ALL_BACKENDS:
                        break;

                    case EVENT_ROUTES.ALL_CLIENTS:
                        // noinspection JSUnresolvedFunction
                        this.eventBus.unregisterHandler( this.busAddress.allClients, handler );
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
