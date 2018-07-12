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

    //
    // Constructor
    //

    constructor( props ) {
        super( props );

        this._empty = false;

        this.subscribe( ( address, message ) => {

            this.messageHandler( address, message );

        } );
    }

    //
    // Properties
    //

    get empty() {
        return this._empty;
    }

    set empty( empty ) {
        this._empty = empty;
    }

    set disabled( state ) {
        console.log( "ERROR: 'disabled' in APSComponent called! This should be overridden!" )
    }

    get primaryGroup() {
        return this.props.guiProps.groups.split( "," )[0];
    }

    //
    // Methods
    //

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

    /**
     * Event message helper.
     *
     * @param {Object} msg The message to append standard info to. This will be returned after modifications.
     *
     * @returns {String} The passed and upgraded object as a JSON string.
     */
    eventMsg( msg ) {
        msg.type = "gui-event";
        msg.groups = this.props.guiProps.groups;
        msg.managerId = this.props.mgrId;
        msg.compoentId = this.props.guiProps.id;
        msg.empty = this.empty;

        return JSON.stringify( msg );
    }


    // noinspection JSMethodCanBeStatic
    messageHandler( address, message ) {
        console.log( this.componentId() + ": Received(address:" + address + "): " + message );

        // if ( this.props.guiProps.enabled != null ) {
        //
        //     let msg = JSON.parse( message );
        //
        // }

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
