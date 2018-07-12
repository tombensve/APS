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
        this.collected = {};

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

    //
    // Methods
    //

    componentId() {

        return "APSComponent";
    }

    send( message ) {

        this.props.eventBus.send( this.props.guiProps.publishTo, message, this.props.guiProps.routing );
    }

    subscribe( subscriber ) {

        this.props.eventBus.subscribe( this.props.guiProps.listenTo, subscriber, this.props.guiProps.routing );
    }

    unsubscribe( subscriber ) {

        this.props.eventBus.unsubscribe( this.props.guiProps.listenTo, subscriber, this.props.guiProps.routing );
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
        msg.group = this.props.guiProps.group;
        msg.managerId = this.props.mgrId;
        msg.componentId = this.props.guiProps.id;
        msg.componentName = this.props.guiProps.name;
        msg.submitter = this.props.guiProps.submitter != null  && this.props.guiProps.submitter;
        msg.empty = this.empty;

        if ( this.props.guiProps.collectGroup ) {

            msg.additional = this.collected;
        }

        return JSON.stringify( msg );
    }


    // noinspection JSMethodCanBeStatic
    messageHandler( address, message ) {

        console.log( this.componentId() + ": Received(address:" + address + "): " + message );

        let msg = JSON.parse( message );

        if ( this.props.guiProps.collectGroup ) {
            if (this.props.guiProps.group === msg.group) {
                this.collected[msg.componentId] = msg;

                //console.log(">>>> [" + this.props.guiProps.id + "] Collected: " + message)
            }
        }

    }

    //
    // Eval funcs
    //

    eval_boolCompsNotEmpty() {

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
