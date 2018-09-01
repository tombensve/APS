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
import APSLocalEventBusRouter from "./APSLocalEventBusRouter";
import APSVertxEventBusRouter from "./APSVertxEventBusRouter";
import APSAlerter from "./APSAlerter";
import APSBusAddress from "./APSBusAddress";

export default class APSEventBus {

    // noinspection SpellCheckingInspection
    /**
     * Holds created busses.
     *
     * @type {{}}
     */
    static busses = {};

    /**
     * Creates a new named bus.
     *
     * @param name The name of the bus to create.
     * @param address The address of the bus to create.
     */
    static createBus( name: string, address: APSBusAddress ) {
        if ( !name ) name = "default";
        if ( !address ) throw new Error( "An address of type APSBusAddress must be supplied!" );
        let bus = new APSEventBus();
        let alerter = APSAlerter.getOrCreateAlerter( name, bus );
        bus.addBusRouter( new APSLocalEventBusRouter() );
        bus.addBusRouter( new APSVertxEventBusRouter( alerter ) );
        bus.setBusAddress( address );

        APSEventBus.busses[name] = bus;

        return bus;
    }

    /**
     * Returns a named bus. This so that other code can pick up the same bus.
     *
     * @param name The name of the bus to return.
     *
     * @returns {APSEventBus}
     */
    static getBus( name: string ): APSEventBus {
        let bus = APSEventBus.busses[name];
        if ( !bus ) throw new Error( `No bus named '${name}' exist!` );
        return bus;
    }

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

        this.busAddressSet = false;
    }

    /**
     * Provides a bus address to all added routers. The bus cannot be used before this is done!
     *
     * @param busAddress The bus address to set.
     */
    setBusAddress( busAddress: APSBusAddress ) {
        for ( let router of this.busRouters ) {
            router.setBusAddress( busAddress );
        }
        this.busAddressSet = true;
    }

    // I though it was a good idea to use named parameters in the form of an object with named values.
    // This so that arguments wouldn't accidentally be passed in the wrong order. What I missed was the
    // extreme dynamicness (is that a word ?) of JS. It is still possible to misspell names, and even pass
    // 2 separate argument rather than one object. Think happilly builds anyhow.

    /**
     * This adds a subscriber for an address. The first param can also be an object containing 3 keys for each
     * parameter. In that case the other 2 are ignored.
     *
     * @param params - Named parameters: { headers: ..., subscriber: ... }
     * @param jic - Just In Case something still uses (headers, subscriber).
     */
    subscribe( params: { headers: { routing: { outgoing: string, incoming: string } }, subscriber: () => mixed }, jic: * = undefined ) {
        if ( !this.busAddressSet ) throw new Error( "A required bus address [setBusAddress(address)] has not been done yet!" );

        if ( jic !== undefined ) {
            throw new Error( "Old method call subscribe(headers, subscriber) done!" )
        }

        let pars = new NamedParams( params, "APSEventBus.subscribe" );

        let headers = APSEventBus.ensureHeaders( pars.param( "headers" ) );
        let subscriber = pars.requiredParam( "subscriber" );

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
    unsubscribe( params: { headers: { routing: { outgoing: string, incoming: string } }, subscriber: () => mixed } ) {
        if ( !this.busAddressSet ) throw new Error( "A required bus address [setBusAddress(address)] has not been done yet!" );

        let pars = new NamedParams( params, "APSEventBus.unsubscribe" );

        let headers = APSEventBus.ensureHeaders( pars.param( "headers" ) );
        let subscriber = pars.requiredParam( "subscriber" );

        APSEventBus.validRoutingHeaders( headers.routing.incoming );

        for ( let busRouter of this.busRouters ) {

            busRouter.unsubscribe( headers, subscriber );
        }
    }

    /**
     * Sends a message.
     *
     * @param params - Named parameters: { headers: {...}, message: {...}}
     * @param jic - Just In Case something still uses (headers, subscriber).
     */
    message( params: { headers: { routing: { outgoing: string, incoming: string } }, message: { aps: {}, content?: {} } }, jic: * = undefined ) {
        if ( !this.busAddressSet ) throw new Error( "A required bus address [setBusAddress(address)] has not been done yet!" );

        if ( jic !== undefined ) {
            throw new Error( "Old method call message(headers, subscriber) done!" )
        }

        let pars = new NamedParams( params, "APSEventBus.message(...)" );

        let headers = APSEventBus.ensureHeaders( pars.param( "headers" ) );
        let message = pars.requiredParam( "message" );

        APSEventBus.validRoutingHeaders( headers.routing.outgoing );

        this.logger.debug( `EventBus: sending( headers: ${JSON.stringify( headers )}): ${JSON.stringify( message )}` );

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
                routing.indexOf( EVENT_ROUTES.ALL_CLIENTS ) >= 0 ||
                routing.indexOf( EVENT_ROUTES.NONE ) >= 0
            )
        ) {
            // OK.
        }
        else {
            throw new Error( `Bad routing headers: ${routing} One or more of the following are valid: \
${EVENT_ROUTES.CLIENT} |& ${EVENT_ROUTES.BACKEND} |& ${EVENT_ROUTES.ALL} |& ${EVENT_ROUTES.ALL_CLIENTS} \
|&" ${EVENT_ROUTES.ALL_BACKENDS} |& ${EVENT_ROUTES.NONE}!` );
        }
    }


}

