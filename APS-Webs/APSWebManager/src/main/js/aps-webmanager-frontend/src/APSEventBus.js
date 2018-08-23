/**
 * ## Types
 *
 * ### Address
 *
 * This is a target group for messages. A message sent to an address will only be received by
 * those listening to that address.
 *
 * ### Routers
 *
 * This class actually does nothing at all! It just provides an API to one or more "routers".
 * Well you can have zero also, but nothing will happen then. This just calls the corresponding
 * subscribe(...), unsubscribe(...), message(...), and publish(...) method of each added router.
 *
 * The intention with this is to have at least 2 different "routers". One that only sends locally
 * among the subscribers within the JS instance, that is no networking, just calling another object
 * directly. One that uses the Vert.x eventbus JS client over the eventbus bridge to the Vert.x server
 * side. Most of the messages sent and received are only between the local components and it would be
 * a waste to message them out on the network and back again. Some of the messages we want to go to the
 * backend. This will also allow different clients to indirectly communicate with each other.
 *
 * Also if at some later time I decided to use something else than Vert.x for example, then I only
 * need to change the router handling Vert.x. It will not affect the components which only uses
 * this.
 */
import APSLogger from "./APSLogger";
import { EVENT_ROUTES } from "./Constants";
import APSEventBusRouter from "./APSEventBusRouter";
import NamedParams from "./NamedParams"

export default class APSEventBus {

    /**
     * Creates a new LocalEventBus.
     *
     * @constructor
     */
    constructor() {

        this.logger = new APSLogger( "APSEventBus" );

        // noinspection JSValidateTypes
        /**
         * @type {array} busRouters. These routers must provide the subscribe, unsubscribe, and message methods
         *               and parameters as this API have. These calls will be forwarded to each added router.
         *               Router is a bad name here since each actually takes full responsibility for any
         *               messaging. But it also makes things clear and very flexible. The components only
         *               uses / knows about this class. Where messages go and where they come from is the
         *               responsibility for other code.
         */
        this.busRouters = [];
    }

    /**
     * This adds a subscriber for an address. The first param can also be an object containing 3 keys for each
     * parameter. In that case the other 2 are ignored.
     *
     * @param params - Named parameters: { headers: ..., subscriber: ... }
     */
    subscribe( params : {headers: {routing: { outgoing: string, incoming: string }}, subscriber: () => mixed} ) {

        let pars = new NamedParams(params, "APSEventBus.subscribe");

        let headers = APSEventBus.ensureHeaders( pars.requiredParam("headers") );
        let subscriber = pars.requiredParam("subscriber");

        APSEventBus.validRoutingHeaders( headers.routing.incoming );

        for ( let busRouter of this.busRouters ) {

            busRouter.subscribe( headers, subscriber );
        }
    }

    /**
     * Unsubscribes to a previously done subscription.
     *
     * @param params params - Named parameters: { headers: ..., subscriber: ... }
     */
    unsubscribe( params : {headers: {routing: { outgoing: string, incoming: string }}, subscriber: () => mixed} ) {

        let pars = new NamedParams(params, "APSEventBus.unsubscribe");

        let headers = APSEventBus.ensureHeaders( pars.requiredParam("headers") );
        let subscriber = pars.requiredParam("subscriber");

        APSEventBus.validRoutingHeaders( headers.routing.incoming );

        for ( let busRouter of this.busRouters ) {

            busRouter.unsubscribe( headers, subscriber );
        }
    }

    /**
     * Sends a message.
     *
     * @param params - Named parameters: { headers: {...}, message: {...}}
     */
    message( params: {headers: {routing: { outgoing: string, incoming: string }}, message: {aps:{}, content?:{}}} ) {

        let pars = new NamedParams(params, "APSEventBus.message");

        let headers = APSEventBus.ensureHeaders(pars.requiredParam("headers"));
        let message = pars.requiredParam("message");

        APSEventBus.validRoutingHeaders( headers.routing.outgoing );

        this.logger.debug( `EventBus: sending( headers: ${headers}): ${message}` );

        for ( let busRouter of this.busRouters ) {

            busRouter.message( headers, message );
        }
    }

    addBusRouter( busRouter: APSEventBusRouter ) {
        this.busRouters.push( busRouter )
    }

    /**
     * Helper to ensure default headers if none is specified.
     *
     * @param headers The headers to check.
     *
     * @returns {{routing: {outgoing: string, incoming: string}}}
     */
    static ensureHeaders( headers: { routing: { outgoing: string, incoming: string } } ): {} {
        if ( headers == null ) {
            headers = {}
        }
        if ( headers.routing == null ) {
            headers.routing = {
                outgoing: "client",
                incoming: "client"
            }
        }

        return headers;
    }

    /**
     * Validates that the operation is valid for this router based on header info.
     *
     * @param routing - The routing string to check for validity.
     *
     * @throws Error on bad headers.
     *
     * @private
     */
    static validRoutingHeaders( routing: string ) {

        if (
            routing != null && (
                routing.indexOf( EVENT_ROUTES.CLIENT ) >= 0 ||
                routing.indexOf( EVENT_ROUTES.BACKEND ) >= 0 ||
                routing.indexOf( EVENT_ROUTES.ALL ) >= 0 ||
                routing.indexOf( EVENT_ROUTES.ALL_BACKENDS ) >= 0 ||
                routing.indexOf( EVENT_ROUTES.ALL_CLIENTS ) >= 0
            )
        ) {
            // OK.
        }
        else {
            throw new Error( `Bad routing headers: ${routing} One or more of the following are valid: \
${EVENT_ROUTES.CLIENT} |& ${EVENT_ROUTES.BACKEND} |& ${EVENT_ROUTES.ALL} |& ${EVENT_ROUTES.ALL_CLIENTS} \
|&" ${EVENT_ROUTES.ALL_BACKENDS}!` );
        }
    }


}

