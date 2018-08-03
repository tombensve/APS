import EventBus from 'vertx3-eventbus-client'
import { HDR_ROUTING, ROUTE_EXTERNAL } from "./Constants"
import APSLogger from "./APSLogger"

/**
 * This represents a router and is responsible for sending and subscribing to messages.
 */
export default class VertxEventBusRouter {

    /**
     * Creates a new LocalBusRouter.
     */
    constructor() {

        this.logger = new APSLogger("VertxEventBusRouter");

        /** Cache of sends done before bus is connected. Will be sent when it is. */
        this.sendMsgs = [];

        /** Cache of publishes done before bus is connected. Will be published when it is. */
        this.publishMsgs = [];

        /** Cache of subscriptions done before bus is connected. Will be done when it is. */
        this.subscribes = [];

        /** Will change to true when bus is connected. */
        this.busReady = false;

        /** Our bus instance. */
        // noinspection JSValidateTypes
        this.eventBus = new EventBus( "http://localhost:8880/eventbus/", {} );

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

        for ( let sub of this.subscribes ) {
            this.logger.info( "Executing cached subscription!" );
            this.subscribe( sub.address, sub.headers, sub.callback );
        }

        this.subscribes = [];

        for ( let msg of this.sendMsgs ) {
            this.logger.info( "Executing cached send!" );
            this.send( msg.address, msg.headers, msg.message );
        }

        this.sendMsgs = [];

        for ( let msg of this.publishMsgs ) {
            this.logger.info( "Executing cached publish!" );
            this.publish( msg.address, msg.headers, msg.message );
        }

        this.publishMsgs = [];

    }

    /**
     * Sends a message.
     *
     * @param {string} address - Address to send to.
     * @param {object} headers - The headers for the message.
     * @param {object} message - The message to send.
     */
    send( address, headers, message ) {
        this.logger.debug( "Sending to {} with headers: {} and message: {}", [ address, headers, message ] );

        if ( this.busReady ) {

            if ( VertxEventBusRouter.valid( headers ) ) {

                // Note that we do not support request/response type messages that Vert.x does. That is why we just
                // pass null for reply handler. In APS we only react to messages when and if they come. There is no
                // hard connection between a send message and a received message. But it is OK to send a message
                // and expect another message to be received at some time.

                // noinspection JSUnresolvedFunction
                this.eventBus.send( address, message, headers, null );
            }
        }
        else {
            this.sendMsgs.push( { address: address, message: message, headers: headers } )
        }
    }

    /**
     * Publishes a message.
     *
     * @param {string} address - Address to send to.
     * @param {object} headers - The headers for the message.
     * @param {object} message - The message to send.
     */
    publish( address, headers, message ) {
        this.logger.debug( "Publishing to {} with headers: {} and message: {}", [ address, headers, message ] );

        if ( this.busReady ) {

            if ( VertxEventBusRouter.valid( headers ) ) {

                // noinspection JSUnresolvedFunction
                this.eventBus.publish( address, message, headers );
            }
        }
        else {

            this.publishMsgs.push( { adress: address, message: message, headers: headers } )
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
        this.logger.debug( "Subscribing to address {} with headers: {}", [ address, headers ] );

        if ( this.busReady ) {

            if ( VertxEventBusRouter.valid( headers ) ) {

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
        }
        else {

            this.subscribes.push( { address: address, headers: headers, callback: callback } )
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
        this.logger.debug( "Unsubscribing from address '{}' with headers: {}", [ address, headers ] ) ;

        // I expect the bus to be upp by the time this is done :-)

        if ( VertxEventBusRouter.valid( headers ) ) {

            // noinspection JSUnresolvedFunction
            this.eventBus.unregisterHandler( address, headers, callback )
        }
    }

    /**
     * Validates that the operation is valid for this router based on header info.
     *
     * @param headers The headers to check for validity.
     * @returns {boolean} true or false.
     *
     * @private
     */
    static valid( headers ) {
        return headers[HDR_ROUTING] != null && headers[HDR_ROUTING].indexOf( ROUTE_EXTERNAL ) >= 0;
    }

}
