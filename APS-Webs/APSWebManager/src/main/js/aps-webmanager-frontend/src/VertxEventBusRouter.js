import EventBus from 'vertx3-eventbus-client'
import { EVENT_ROUTING, EVENT_ROUTES } from "./Constants"
import APSLogger from "./APSLogger"

/**
 * This represents a router and is responsible for sending and subscribing to messages.
 */
export default class VertxEventBusRouter {

    /**
     * Creates a new LocalBusRouter.
     */
    constructor() {

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
            this.subscribe( sub.address, sub.headers, sub.callback );
        }

        this.subscribes = [];

        for ( let msg of this.cache.messages ) {
            this.logger.info( "Executing cached send!" );
            // noinspection JSUnresolvedFunction
            this.message( msg.address, msg.headers, msg.message );
        }

        this.sendMsgs = [];
    }

    /**
     * Sends a message.
     *
     * @param {string} address - Address to message to.
     * @param {object} headers - The headers for the message.
     * @param {object} message - The message to message.
     */
    message( address, headers, message ) {
        this.logger.debug( "Sending to {} with headers: {} and message: {}", [address, headers, message] );

        if ( this.busReady ) {

            // Note here that we do no imitate the vertx event bus totally. We only have one message(...)
            // method to send messages. It is the 'routing' in the header that determines if a send or a
            // publish should be done.

            if ( headers[EVENT_ROUTING] != null ) {
                if ( headers[EVENT_ROUTING].indexOf( EVENT_ROUTES.BACKEND ) >= 0 ) {

                    // Note that we do not support request/response type messages that Vert.x does. That is why we just
                    // pass null for reply handler. In APS we only react to messages when and if they come. There is no
                    // hard connection between a message message and a received message. But it is OK to message a message
                    // and expect another message to be received at some time.

                    // noinspection JSUnresolvedFunction
                    this.eventBus.send( address, message, headers, null );
                }
                else if ( headers[EVENT_ROUTING].indexOf( EVENT_ROUTES.CLUSTER ) >= 0 ) {

                    // noinspection JSUnresolvedFunction
                    this.eventBus.publish( address, message, headers );
                }
            }
        }
        else {
            this.cache.messages.push( { address: address, message: message, headers: headers } );
        }
    }

    /**
     * Subscribes to messages.
     *
     * @param {string} address            - Address to subscribe to.
     * @param {object} headers            - The relevant headers for the subscription.
     * @param {function(object)} callback - Callback to call with messages.
     */
    subscribe( address, headers, callback ) {
        this.logger.debug( "Subscribing to address {} with headers: {}", [address, headers] );

        if ( this.busReady ) {

            // noinspection JSUnresolvedFunction
            this.eventBus.registerHandler( address, headers, ( alwaysNull, message ) => {
                // console.log( "ADDSRESS: " + address );
                // console.log( "RECEIVED: " + JSON.stringify( message ) );
                // console.info("CALLBACK: " + callback);
                // For some reason we get the full internal vertx evemntbus message, not just
                // the client relevant 'body' part.
                callback( message['body'] );
            } );
        }
        else {

            this.cache.subscribers.push( { address: address, headers: headers, callback: callback } )
        }
    }

    /**
     * Unsubscribe from receiving messages.
     *
     * @param {string} address - The address to unsubscribe for.
     * @param {object} headers                    - The headers used to subscribe.
     * @param {function(string, string)} callback - The callback to unsubscribe.
     */
    unsubscribe( address, headers, callback ) {
        this.logger.debug( "Unsubscribing from address '{}' with headers: {}", [address, headers] );

        // I expect the bus to be upp by the time this is done :-)

        // noinspection JSUnresolvedFunction
        this.eventBus.unregisterHandler( address, headers, callback )
    }
}
