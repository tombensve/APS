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

        this._empty = true;
        this._hasValue = true;
        this.collected = {};

        this.subscribe( ( address, message ) => {

            this.messageHandler( address, message );

        } );
    }

    //
    // Properties (subclasses should override or set these as needed)
    //

    // Component has empty value.

    get empty() {
        return this._empty;
    }

    set empty( empty ) {
        this._empty = empty;
    }

    // Component has a value to provide. Default is true, so a button for example should do this.hasValue = false.
    get hasValue() {
        return this._hasValue;
    }

    set hasValue( hasValue ) {
        this._hasValue = hasValue;
    }

    // Sets component disabled state. This must be overridden by sub components.

    set disabled( state ) {
        console.log( "ERROR: 'disabled' in APSComponent called! This should be overridden!" )
    }

    //
    // Methods
    //

    /**
     * React callback for when component is available.
     */
    componentDidMount() {
        this.send( this.eventMsg( {
            eventType: "hello"
        } ) );
    }


    /**
     * This should be overridden by subclasses to provide the name of the component. This is useful
     * when doing debug logging.
     *
     * @returns {string}
     */
    componentId() {

        return "APSComponent";
    }

    /**
     * Helper to send messages from event handlers. This gets everything but the message from the gui spec JSON.
     *
     * @param {string} message The message to send. Should be a string of JSON. Use eventMessage() as input to this.
     */
    send( message ) {

        this.props.eventBus.send( this.props.guiProps.publishTo, message, this.props.guiProps.routing );
    }

    /**
     * Helper to subscribe to messages. This gets everything else from the gui spec JSON.
     *
     * @param {function(string, string)} subscriber The function to call with messages.
     */
    subscribe( subscriber ) {

        this.props.eventBus.subscribe( this.props.guiProps.listenTo, subscriber, this.props.guiProps.routing );
    }

    /**
     * Does the opposite of subscribe :-)
     *
     * @param {function(string, string)} subscriber The function to no longer call with messages.
     */
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
        if ( msg.eventType == null ) msg.eventType = "change";
        msg.group = this.props.guiProps.group;
        msg.managerId = this.props.mgrId;
        msg.componentId = this.props.guiProps.id;
        msg.componentName = this.props.guiProps.name;
        //msg.submitter = this.props.guiProps.submitter != null  && this.props.guiProps.submitter;
        msg.empty = this.empty;
        msg.hasValue = this.hasValue;

        if ( Object.keys( this.collected ).length !== 0 ) {

            msg.additional = this.collected;
        }

        return JSON.stringify( msg );
    }


    /**
     * Component generic message handler.
     *
     * @param {string} address The address of the message.
     * @param {string} message The actual message as JSON string.
     */
    // noinspection JSMethodCanBeStatic
    messageHandler( address, message ) {

        console.log( this.componentId() + ": Received(address:" + address + "): " + message );

        let msg = JSON.parse( message );

        // If this component wants to collect values sent by other components, we
        // just save the whole message under 'collected' and using the componentId as key.
        // This is for submit type components. Rather than having all components publish
        // all data over the network, submit type components collect their data and passes
        // it along on a submit.
        if ( msg.hasValue && this.props.guiProps.collectGroups != null &&
            this.props.guiProps.collectGroups.indexOf( msg.group ) !== -1 ) {

            // Save message using the originating components id.
            this.collected[msg.componentId] = msg;

            //console.log(">>>> [" + this.props.guiProps.id + "] Collected: " + message)
        }

        // Handle enable and disable of a component that have supplied a criteria for that.
        if ( this.props.guiProps.enabled != null ) {

            let enabparams = this.props.guiProps.enabled.split( ':' );
            let _disable = true;

            if ( enabparams[0] === "groupNotEmpty" ) {
                _disable = !this.enableDisableOnGroupNotEmpty( enabparams[1] );
            }
            else if ( enabparams[0] === "namedComponentsNotEmpty" ) {
                _disable = !this.enableDisableOnNamedComponentsNotEmpty( enabparams[1] );
            }

            this.disabled = _disable;
        }

    }

    enableDisableOnGroupNotEmpty( group ) {
        let notEmpty = true;
        for ( let key of Object.keys( this.collected ) ) {
            let msg = this.collected[key];

            if ( ( msg.group === group ) && ( msg.empty != null ) && ( msg.empty === true ) ) {
                notEmpty = false;
                break;
            }
        }

        return notEmpty;
    }

    enableDisableOnNamedComponentsNotEmpty( names ) {
        if ( this.collected == null ) return true;

        let namesArr = names.split( "," );
        let notEmpty = true;

        for ( let name of namesArr ) {

            let msg = this.collected[name];

            if ( msg != null ) {
                if ( msg.empty != null && msg.empty === true ) {
                    notEmpty = false;
                    break;
                }
            }
            else {
                console.log( "Oops! Bad gui spec! For enable: \"namedComponentsNotEmpty:<compid>,...\" the component " +
                    "must have at least one \"collectGroups:<group>\" in the gui spec and the given compid:s must be " +
                    "part of one of the groups." )
            }
        }

        return notEmpty;
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
