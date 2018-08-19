import React, { Component } from 'react'
import APSLocalEventBus from "../APSLocalEventBus"
import APSLocalEventBusRouter from "../APSLocalEventBusRouter"
import APSVertxEventBusRouter from "../APSVertxEventBusRouter"
import APSBusAddress from "../APSBusAddress";
import APSBusRoutes from "../APSBusRoutes"

class APSPage extends Component {

    constructor( props ) {
        super( props );

        this.busAddresses = new APSBusAddress( this.props.app );

        this.localEventBus = new APSLocalEventBus();

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
        this.localEventBus.addBusRouter( new APSLocalEventBusRouter() );
        this.localEventBus.addBusRouter( new APSVertxEventBusRouter() );

        this.localEventBus.subscribe( this.busAddresses.client, { }, this.compSubscriber );
    }

    /**
     * React is telling us the component will be removed.
     */
    componentWillUnmount() {

        // Since we are going away, stop listening for events.
        this.localEventBus.unsubscribe( this.busAddresses.client,
            { routing: APSBusRoutes.BACKEND }, this.compSubscriber );
    }

}