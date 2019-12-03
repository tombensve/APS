/**
 * ## Types
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
 * backend.
 *
 * Also if at some later time I decided to use something else than Vert.x for example, then I only
 * need to change the router handling Vert.x. It will not affect the components which only uses
 * this.
 *
 * ### Routes / absolute address
 *
 * The bus routers in general works with "routes", which can be more than one, separated with a
 * comma. A route is a more general than an address. A route can resolve to one specific receiver
 * or many. Since this is completely message driven and no REST requests are ever done and this
 * is using Vert.x EventBus bridge, routes can lead to:
 *
 * - A general GUI message handler on one node in a cluster, with round robin between nodes in
 *   the cluster.
 * - To a message listener on all backend nodes of the cluster (*1).
 * - To all clients of the application (*1).
 *
 * (*1): Be very, very careful of this! Yes, it is physically possible to do, but in general
 *       probably a very bad idea. I might inhibit this flexibility in the future.
 *
 * Routes have simple names like 'backend', 'all:backend', 'client', 'all:client', ...
 *
 * This means that components doesn't have to know about anything else than these routes, and where
 * they go is determined by the bus routers.
 *
 * This works well when the application has a generic GUI subscriber that checks the message and
 * acts on it depending on what it is. Don't let all component send messages to backend! Most
 * components should only send messages locally to interact with other components. A nicer way
 * is to have local services that listens to messages from components and those services then
 * interact with backend, rather than components going directly to backend.
 *
 * APS is about simplicity and also flexibility. It thereby does not want to force how to handle
 * interaction between GUI and backend, other than that the bus must be used in one way or another.
 *
 * There are several ways of handling the GUI on the frontend. These are either one or the other:
 *
 * - Make a React GUI using the APS components just like any other React app.
 *
 * - Use the APSWebManager component which listens for a JSON message describing the GUI to render,
 *   and renders it. The JSON also contains routes for sending messages, and for subscribing to
 *   messages. The JSON document then comes from the backend so the backend fully controls the
 *   rendered GUI. This is what the demo "APSWebManager" project does. It works for relatively
 *   static GUIs.
 *
 *   Each of the APS React components have its representative in a backend class. The purpose of
 *   these classes are mostly to be configured and then generate JSON data as part of the GUI
 *   spec sent to the client. This then provides a backend API that does not require any frontend
 *   coding. Similar, but very different from Vaadin. These backend GUI classes are ONLY used to
 *   create a GUI!! They are not used thereafter! When the GUI is upp all communication is done
 *   via bus messages. They can be seen as a utility.
 *
 * You are thereby very free in how to use this. Note that the APSWebTemplate project has all the
 * functionality, but GUI wise is only a demo and also used for testing components. To make your
 * own web using this, copy the APSWebTemplate project and adapt to your needs.
 */
import APSLogger from "./APSLogger";
import APSEventBusRouter from "./APSEventBusRouter";
import NamedParams from "./NamedParams"
import APSLocalEventBusRouter from "./APSLocalEventBusRouter";
import APSVertxEventBusRouter from "./APSVertxEventBusRouter";
import APSAlerter from "./APSAlerter";
import APSBusAddress from "./APSBusAddress";
import { apsObject } from "./Utils"

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
    static createBus( name: string, address: APSBusAddress ): APSEventBus {
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
     * @param busAddress The bus address to set. NOTE that this is an APSBusAddress!!
     */
    setBusAddress( busAddress: APSBusAddress ) {
        this.busAddress = busAddress;

        for ( let router of this.busRouters ) {
            router.setBusAddress( busAddress );
        }
        this.busAddressSet = true;
    }

    /**
     * Returns the address of this bus. NOTE that this is an APSBusAddress!!
     */
    getBusAddress() : APSBusAddress {
        return this.busAddress;
    }

    /**
     * Validates that the operation is valid for this router based on header info.
     *
     * @param type - The type to validate: "message", "subscribe", "unsubscribe".
     * @param routing - The routing string to check for validity.
     *
     * @throws Error on bad headers.
     *
     * @private
     */
    _validRoutingHeaders( type: string, routing: string ) {

        let valid = false;
        let invalid = "";

        // Make an exception for absolute addresses. See class comment for more info.
        if (routing.startsWith("address:")) {
            valid = true;
        }
        else {
            for ( let busRouter of this.busRouters ) {

                //this.logger.debug(`Valid routes: ${JSON.stringify(busRouter.getValidRoutes())}`);

                for ( let route: string of routing.split( ',' ) ) {
                    if ( busRouter.getValidRoutes()[type].includes( route, 0 ) ) {
                        valid = true;
                    } else {
                        invalid += ( " " + route );
                    }
                }
            }
        }

        if (!valid) {
            throw new Error( `Routing values of "'${invalid.trim().replace(' ', ',')}'" is illegal for '${type}'!` );
        }
    }

    // I though it was a good idea to use named parameters in the form of an object with named values.
    // This so that arguments wouldn't accidentally be passed in the wrong order. What I missed was the
    // extreme dynamicness (is that a word ?) of JS. It is still possible to misspell names, and even pass
    // 2 separate argument rather than one object. Things happily builds anyhow.

    /**
     * This adds a subscriber.
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

        let headers = apsObject(APSEventBus.ensureHeaders( pars.param( "headers" ) ));
        let subscriber = pars.requiredParam( "subscriber" );

        this._validRoutingHeaders( "subscribe",  headers.routing.incoming );

        for ( let busRouter of this.busRouters ) {

            busRouter.subscribe( headers, subscriber );
        }
    }

    /**
     * Unsubscribes a previously done subscription.
     *
     * @param params params - Named parameters: { headers: ..., subscriber: ... }
     */
    unsubscribe( params: { headers: { routing: { outgoing: string, incoming: string } }, subscriber: () => mixed } ) {
        if ( !this.busAddressSet ) throw new Error( "A required bus address [setBusAddress(address)] has not been done yet!" );

        let pars = new NamedParams( params, "APSEventBus.unsubscribe" );

        let headers = apsObject(APSEventBus.ensureHeaders( pars.param( "headers" ) ));
        let subscriber = pars.requiredParam( "subscriber" );

        this._validRoutingHeaders( "unsubscribe", headers.routing.incoming );

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

        let headers = apsObject(APSEventBus.ensureHeaders( pars.param( "headers" ) ));
        let message = apsObject(pars.requiredParam( "message" ));

        this._validRoutingHeaders( "message", headers.routing.outgoing );

        // this.logger.debug( `EventBus: sending( headers: ${headers.display()}): ${message.display()}` );

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


}

