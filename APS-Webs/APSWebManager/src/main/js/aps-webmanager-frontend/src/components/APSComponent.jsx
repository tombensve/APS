// This code uses Flow syntax!
import { Component } from 'react'
import '../APSEventBus'
import PropTypes from "prop-types"
import APSLogger from "../APSLogger"
import { APP_NAME, EVENT } from "../Constants"
import APSAlerter from "../APSAlerter";

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

    constructor( props: {} ) {
        super( props );

        this.logger = new APSLogger( this.componentType() );

        this._empty = true;
        this._busMember = true;
        this._hasValue = true;
        this.collected = {};

        if ( this._busMember ) {
            this.subscribe( ( message ) => {

                this.messageHandler( message );

            } );
        }

        this.alerter = new APSAlerter( this.props.eventBus );
    }

    //
    // Properties (subclasses should override or set these as needed)
    //

    // Is this component part of the bus ? Most are.

    get busMember(): boolean {
        return this._busMember;
    }

    set busMember( busMember: boolean ) {
        this._busMember = busMember;
    }

    // Component has empty value.

    get empty(): boolean {
        return this._empty;
    }

    set empty( empty: boolean ) {
        this._empty = empty;
    }

    // Component has a value to provide. Default is true, so a button for example should do this.hasValue = false.

    get hasValue(): boolean {
        return this._hasValue;
    }

    set hasValue( hasValue: boolean ) {
        this._hasValue = hasValue;
    }

    // The default starting value of the component.

    get defaultValue(): * {
        return this._defaultValue ? this._defaultValue : "";
    }

    set defaultValue( defaultValue: * ) {
        this._defaultValue = defaultValue;
    }

    // Sets component disabled state. This must be overridden by sub components.

    set disabled( state: boolean ) {
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
     * If render() is not overridden bu subclass then this must be overridden! This is just a convenience.
     *
     * @param comps An initialized array to add components to.
     */
    doRender( comps ) {
    }

    /**
     * The actual React render method.
     *
     * @returns {Array} The component(s) to render.
     */
    render() {
        let comps = [];
        this.doRender( comps );
        return comps;
    }

    /**
     * Sends a message on the bus with the default value. This only sends within the client. This message
     * is not relevant for anything else. The point of this is to initialize "collector" components with
     * initial values. Most usually "collector" components are also routed to the backend for their events
     * which will include collected data. But there can be other reasons for components to collect data
     * from other components. In either case a collector component wants a default value before a user
     * has manipulated a component. This supplies that.
     *
     * Certain components wants to do this on componentDidMount(). this.defaultValue = value must be done first!
     */
    sendDefaultValueLocalOnly() {
        this.props.eventBus.message( {
            headers: {
                outgoing: "client"
            },
            message: this.changeEvent(
                {
                    componentType: this.componentType(),
                    value: this.defaultValue
                }
            )
        } );
    }

    /**
     * Provides default classes or overridden classes in guiProps. Its either or.
     *
     * @param {string} classes The components default classes.
     *
     * @returns {string} the passes classes or the overridden ones.
     */
    clsName( classes: String ): string {
        return this.props.guiProps.class != null ? this.props.guiProps.class : classes;
    }

    /**
     * This should be overridden by subclasses to provide the name of the component. This is useful
     * when doing logging.
     *
     * @returns {string}
     */
    componentType(): string {

        return "APSComponent";
    }

    /**
     * Updates and shows a specified APSAlert component.
     *
     * @param alerterId The id of the alert component to update.
     * @param message The message to update with.
     */
    alert( alerterId: string, message: * ) {
        this.alerter.alert( alerterId, message );
    }

    /**
     * Helper to message messages from event handlers. This gets everything but the message from the gui spec JSON.
     *
     * @param {object} message The message to message. Should be a string of JSON. Use eventMessage() as input to this.
     */
    message( message: {} ) {

        this.props.eventBus.message( { headers: this.props.guiProps.headers, message: message } );
    }

    /**
     * Helper to subscribe to messages. This gets everything else from the gui spec JSON.
     *
     * @param {function(string, string)} subscriber The function to call with messages.
     */
    subscribe( subscriber: () => mixed ) {

        if ( this.props.guiProps.headers != null ) {

            this.props.eventBus.subscribe( { headers: this.props.guiProps.headers, subscriber: subscriber } );

            this.logger.debug( `%%%% Subscribed with headers: ${JSON.stringify( this.props.guiProps.headers )} and callback: ${subscriber}` )
        }
        else {
            throw new Error( `Tried to subscribe without guiProps.headers being available!` );
        }
    }

    /**
     * Does the opposite of subscribe :-)
     *
     * @param {function(string, string)} subscriber The function to no longer call with messages.
     */
    unsubscribe( subscriber: () => mixed ) {

        this.props.eventBus.unsubscribe( { headers: this.props.guiProps.headers, subscriber: subscriber } );
    }

    /**
     * Event message helper.
     *
     * @param {object} content - The content of the message to send. A standard aps: object is added to this,
     *                           and passed content is places under content:.
     *
     * @returns {object} The passed and upgraded object as a JSON string.
     */
    eventMsg( content: APSComponentEventMessage ): APSCollectorComponentEventMessage {
        return {
            aps: {
                origin: this.props.origin,
                app: APP_NAME,
                type: "gui-event"
            },
            content: Object.assign( content, {
                group: this.props.guiProps.group,
                managerId: this.props.mgrId,
                componentId: this.props.guiProps.name,
                componentName: this.props.guiProps.name,
                empty: this.empty,
                hasValue: this.hasValue,
                collected: Object.keys( this.collected ).length !== 0 ? this.collected : undefined
            } )
        };
    }

    /**
     * Creates a "changeEvent" message.
     *
     * @param {Object} msg Base message to append to.
     * @returns {Object} An updated message.
     */
    changeEvent( msg: { aps: { type: string }, content: {} } ): {} {
        msg = this.eventMsg( msg );
        msg.content.eventType = EVENT.TYPES.CHANGE;
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
    actionEvent( msg: APSComponentEventMessage, action: string ): APSActionEventMessage {
        msg = this.eventMsg( msg );
        msg.content.eventType = "action";
        msg.content.action = action;
        return msg;
    }

    /**
     * A more specific submit action event.
     *
     * @param msg The message to append to.
     *
     * @returns {Object} The updated message.
     */
    submitActionEvent( msg: {} ) {
        return this.actionEvent( msg, "submit" );
    }

    /**
     * Component generic message handler.
     *
     * @param {object} message The actual message as JSON string.
     */
    // noinspection JSMethodCanBeStatic
    messageHandler( message: APSMessage ) {

        try {
            this.logger.debug( `messageHandler > Received: ${JSON.stringify( message )}` );
        } catch ( e ) {
            this.logger.error( `Failed logging: ${e}` );
        }

        switch ( message.aps.type ) {
            case "gui-created":
                // Consider doing this only for group members!
                this.sendDefaultValueLocalOnly();
                break;

            case "gui-event":
                // If this component wants to collect values sent by other components, we
                // just save the whole message under 'collected' and using the components id as key.
                // This is for submit type components. Rather than having all components publish
                // all data over the network, submit type components collect their data and passes
                // it along on a submit.

                if ( message.content.hasValue && this.props.guiProps.collectGroups != null &&
                    this.props.guiProps.collectGroups.indexOf( message.content.group ) !== -1 ) {

                    // Save message using the originating components id.
                    this.collected[message.content.componentId] = message.content;

                    this.logger.debug( "@@@@" + this.props.guiProps.id + "] Collected: " + JSON.stringify( message ) );
                }

                // Handle enable and disable of a component that have supplied a criteria for that.
                // Part of me wants to be more flexible and dynamic here, but that can also lead to
                // security issues.
                if ( this.props.guiProps.enabled != null ) {

                    let enableDisableParameters = this.props.guiProps.enabled.split( ':' );
                    let _disable = true;

                    if ( enableDisableParameters[0] === "groupNotEmpty" ) {
                        _disable = !this.enableDisableOnGroupNotEmpty( enableDisableParameters[1] );
                    }
                    else if ( enableDisableParameters[0] === "namedComponentsNotEmpty" ) {
                        _disable = !this.enableDisableOnNamedComponentsNotEmpty( enableDisableParameters[1] );
                    }
                    else {
                        this.logger.error( `Unknown enable/disable property value: ${this.props.guiProps.enabled}` )
                    }

                    this.disabled = _disable;
                }
                break;

            default:
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
    enableDisableOnGroupNotEmpty( group: string ) {
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
    enableDisableOnNamedComponentsNotEmpty( names: string ) {
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
        value: PropTypes.any,
        class: PropTypes.string,
        collectGroups: PropTypes.string,
        headers: {
            routing: {
                incoming: PropTypes.string,
                outgoing: PropTypes.string
            }
        },
        width: PropTypes.number,
        cols: PropTypes.number,
        rows: PropTypes.number,
        label: PropTypes.string,
        children: [APSComponent.propTypes]
    }
};

export default APSComponent;
