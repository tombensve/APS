// This code uses Flow syntax!
import { Component } from 'react'
import '../APSEventBus'
import PropTypes from "prop-types"
import APSLogger from "../APSLogger"
import { EVENT } from "../Constants"

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

    constructor( props : object ) {
        super( props );

        this.logger = new APSLogger( this.componentType() );

        this._empty = true;
        this._busMember = true;
        this._hasValue = true;
        this.collected = {};

        if ( this.busMember ) {
            this.subscribe( ( message ) => {

                this.messageHandler( message );

            } );
        }
    }

    //
    // Properties (subclasses should override or set these as needed)
    //

    get busMember() : boolean {
        return this._busMember;
    }

    set busMember( busMember : boolean ) {
        this._busMember = busMember;
    }

    // Component has empty value.

    get empty() : boolean {
        return this._empty;
    }

    set empty( empty : boolean ) {
        this._empty = empty;
    }

    // Component has a value to provide. Default is true, so a button for example should do this.hasValue = false.
    get hasValue() : boolean {
        return this._hasValue;
    }

    set hasValue( hasValue : boolean ) {
        this._hasValue = hasValue;
    }

    // Sets component disabled state. This must be overridden by sub components.

    set disabled( state ) {
        this.logger.error( "ERROR: 'disabled' in APSComponent called! This should be overridden!" )
    }

    //
    // Methods
    //

    /**
     * React callback for when component is available.
     */
    componentDidMount() {
    }

    /**
     * Provides default classes or overridden classes in guiProps. Its either or.
     *
     * @param {string} classes The components default classes.
     *
     * @returns {string} the passes classes or the overridden ones.
     */
    clsName( classes : string ) : string {
        return this.props.guiProps.class != null ? this.props.guiProps.class : classes;
    }

    /**
     * This should be overridden by subclasses to provide the name of the component. This is useful
     * when doing debug logging.
     *
     * @returns {string}
     */
    componentType() : string {

        return "APSComponent";
    }

    /**
     * Helper to message messages from event handlers. This gets everything but the message from the gui spec JSON.
     *
     * @param {object} message The message to message. Should be a string of JSON. Use eventMessage() as input to this.
     */
    message( message : object ) {

        this.props.eventBus.message( this.props.guiProps.headers, message );
    }

    /**
     * Helper to subscribe to messages. This gets everything else from the gui spec JSON.
     *
     * @param {function(string, string)} subscriber The function to call with messages.
     */
    subscribe( subscriber : () => mixed ) {

        if ( this.props.guiProps.headers != null ) {

            this.props.eventBus.subscribe( this.props.guiProps.headers, subscriber );
        }
        else {
            throw new Error(`Tried to subscribe without guiProps.headers being available!`);
        }
    }

    /**
     * Does the opposite of subscribe :-)
     *
     * @param {function(string, string)} subscriber The function to no longer call with messages.
     */
    unsubscribe( subscriber : func ) {

        this.props.eventBus.unsubscribe( this.props.guiProps.headers, subscriber );
    }

    /**
     * Event message helper.
     *
     * @param {object} msg The message to append standard info to. This will be returned after modifications.
     *
     * @returns {object} The passed and upgraded object as a JSON string.
     */
    eventMsg( msg : {type: string, group: string, managerId: string, componentId: string, componentName: string,
        empty: boolean, hasValue: boolean} ) : object {

        msg.type = "gui-event";
        msg.group = this.props.guiProps.group;
        msg.managerId = this.props.mgrId;
        msg.componentId = this.props.guiProps.id;
        msg.componentName = this.props.guiProps.name;
        msg.empty = this.empty;
        msg.hasValue = this.hasValue;

        if ( Object.keys( this.collected ).length !== 0 ) {

            msg.additional = this.collected;
        }

        return msg;
    }

    /**
     * Creates a "changeEvent" message.
     *
     * @param {object} msg Base message to append to.
     * @returns {Object} An updated message.
     */
    changeEvent( msg : object ) : object {
        msg = this.eventMsg( msg );
        msg[EVENT.TYPE] = EVENT.TYPES.CHANGE;
        return msg;
    }

    /**
     * Creates a "changeEvent" message.
     *
     * @param {object} msg    - Base message to append to.
     * @param {string} action - The action.
     *
     * @returns {Object} An updated message.
     */
    actionEvent( msg : object , action : string ) : object {
        msg = this.eventMsg( msg );
        msg["eventType"] = "action";
        msg["action"] = action;
        return msg;
    }

    /**
     * A more specific submit action event.
     *
     * @param msg The message to append to.
     *
     * @returns {Object} The updated message.
     */
    submitActionEvent( msg : object ) {
        return this.actionEvent( msg, "submit" );
    }

    /**
     * Component generic message handler.
     *
     * @param {object} message The actual message as JSON string.
     */
    // noinspection JSMethodCanBeStatic
    messageHandler( message : object ) {

        this.logger.debug( "messageHandler > Received: {}", [message] );

        // If this component wants to collect values sent by other components, we
        // just save the whole message under 'collected' and using the componentType as key.
        // This is for submit type components. Rather than having all components publish
        // all data over the network, submit type components collect their data and passes
        // it along on a submit.
        // noinspection JSUnresolvedVariable
        if ( message.hasValue && this.props.guiProps.collectGroups != null &&
            this.props.guiProps.collectGroups.indexOf( message.group ) !== -1 ) {

            // Save message using the originating components id.
            this.collected[message.componentId] = message;

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

    /**
     * Enables or disables component depending on other components in same group being empty
     * or not empty.
     *
     * @param group The name of the group the components should belong to.
     *
     * @returns {boolean}
     */
    enableDisableOnGroupNotEmpty( group : string ) {
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

    /**
     * Enables or disables on named components being empty nor not.
     *
     * @param names A string with comma separated list of component names that should not be empty.
     *
     * @returns {boolean}
     */
    enableDisableOnNamedComponentsNotEmpty( names : string ) {
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
                this.logger.error(
                    `Oops! Bad gui spec! For enable: "namedComponentsNotEmpty:<compid>,..." the component`,
                    `must have at least one "collectGroups:<group>" in the gui spec and the given compid:s must be`,
                    `part of one of the groups.`
                );
            }
        }

        return notEmpty;
    }
}

// Workarounds like this is why I don't like typeless languages! You end up faking types in different ways anyhow.
APSComponent.propTypes = {
    // The eventbus we message messages when needed.
    eventBus: PropTypes.object,
    // The unique id of the GuiMgr creating the component.
    mgrId: PropTypes.string,
    // This is part of a JSON document that is the spec for this component.
    guiProps: {
        id: PropTypes.string,
        name: PropTypes.string,
        type: PropTypes.string,
        orientation: PropTypes.string,
        children: {
            id: PropTypes.string,
            name: PropTypes.string,
            group: PropTypes.string,
            type: PropTypes.string,
            value: PropTypes.any,
            listenTo: PropTypes.string,
            sendTo: PropTypes.string,
            class: PropTypes.string,
            headers: {
                routing: PropTypes.string
            },
            width: PropTypes.number,
            cols: PropTypes.number,
            rows: PropTypes.number,
            label: PropTypes.string
        }
    }
};

export default APSComponent;
