import { Component } from 'react'
import '../LocalEventBus'
import PropTypes from "prop-types";

/**
 * A common base component for all APS components.
 *
 * It helps out with:
 * - Messaging.
 * - Declaring 'guiProps' containing the components original JSON spec.
 */
class APSComponent extends Component {

    constructor( props ) {
        super( props );

        this.subscribe( ( address, message ) => {

            this.internalMsgHandler( address, message );

        } );
    }

    componentId() {
        return "APSComponent";
    }

    /**
     * @private
     * @returns {boolean}
     */
    isPublishGlobal() {
        return this.props.guiProps.publishGlobal != null ? this.props.guiProps.publishGlobal : false;
    }

    /**
     * @private
     * @returns {boolean}
     */
    isSubscribeGlobal() {
        return this.props.guiProps.subscribeGlobal != null ? this.props.guiProps.subscribeGlobal : false;
    }

    send( message ) {

        this.props.eventBus.send( this.props.guiProps.publishTo, message, this.isPublishGlobal() );
    }

    subscribe( subscriber ) {

        this.props.eventBus.subscribe( this.props.guiProps.listenTo, subscriber, this.isSubscribeGlobal() );
    }

    unsubscribe( subscriber ) {

        this.props.eventBus.unsubscribe( this.props.guiProps.listenTo, subscriber );
    }

    // noinspection JSMethodCanBeStatic
    internalMsgHandler( address, message ) {
        console.log(this.componentId() + ": Received(address:" + address + "): " + message);
    }
}

// Workarounds like this is why I don't like typeless languages! You end up faking types in different ways anyhow.
APSComponent.propTypes = {
    // The eventbus we send messages when needed.
    eventBus: PropTypes.object,
    // The unique id of the GuiMgr creating the component.
    mgrId: PropTypes.string,
    // This is part of a JSON document that is the spec for this component.
    guiProps: PropTypes.object
};

export default APSComponent;
