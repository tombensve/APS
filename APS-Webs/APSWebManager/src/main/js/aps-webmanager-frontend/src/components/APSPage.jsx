import React, { Component } from 'react'
import APSEventBus from "../APSEventBus"
import APSLocalEventBusRouter from "../APSLocalEventBusRouter"
import APSVertxEventBusRouter from "../APSVertxEventBusRouter"
import APSBusAddress from "../APSBusAddress";

class APSPage extends Component {

    constructor( props: {} ) {
        super( props );

        this.busAddresses = new APSBusAddress( this.props.app );

        this.apsEventBus = new APSEventBus();

        /**
         * Event bus subscriber. We need to keep the instance of this so that we can unsubscribe later.
         *
         * @param {object} message - A received message in JSON format.
         */
        this.compSubscriber = ( message ) => {

            this.logger.debug( ">>>>>>>: message: {}", message );

        };

    }

    /**
     * React callback for when component is available.
     */
    componentDidMount() {
        this.apsEventBus.addBusRouter( new APSLocalEventBusRouter() );
        this.apsEventBus.addBusRouter( new APSVertxEventBusRouter() );

        //this.apsEventBus.subscribe( { headers: {},  subscriber:this.compSubscriber} );
    }

    /**
     * React is telling us the component will be removed.
     */
    componentWillUnmount() {

        // Since we are going away, stop listening for events.
        // this.apsEventBus.unsubscribe( this.busAddresses.client,
        //     { routing: APSBusRoutes.BACKEND }, this.compSubscriber );
    }

}