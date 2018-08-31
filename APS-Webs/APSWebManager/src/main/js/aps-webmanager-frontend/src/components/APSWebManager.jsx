// @flow
// React
import React, { Component } from 'react'

// Misc
import { APP_NAME, EVENT_ROUTES } from "../Constants"

// Functional / Classes
import uuid from "../APSUUID"
import APSEventBus from "../APSEventBus"
import APSLocalEventBusRouter from "../APSLocalEventBusRouter"
import APSVertxEventBusRouter from "../APSVertxEventBusRouter"
import APSBusAddress from "../APSBusAddress"
import APSAlerter from "../APSAlerter"

// Components
import APSLayout from "./APSLayout"
import APSPanel from "./APSPanel"
import APSButton from "./APSButton"
import APSTextField from "./APSTextField"
import APSTextArea from "./APSTextArea"
import APSNumber from "./APSNumber"
import APSDate from "./APSDate"
import APSMarkdown from "./APSMarkdown"
import APSAlert from "./APSAlert"
import APSCheckBox from "./APSCheckBox"
import APSLogger from "../APSLogger"


/**
 * A component reading a JSON spec of components to render. The GuiMgr also creates a local
 * event bus passed on to components. The JSON spec provides messages for components to message
 * on the bus. Some will be forwarded on a global cluster bus also reaching the backend.
 *
 * The JSON spec to parse is provided on the event bus, and usually comes from the backend,
 * but may also come from the frontend. This component does not care in any way where the
 * JSON spec comes from.
 */
export default class APSWebManager extends Component {

    //
    // Constructors
    //

    /**
     * Creates a new GuiMgr component.
     *
     * @param props The standard properties.
     */
    constructor( props: {} ) {
        super( props );

        this.logger = new APSLogger( "APSWebManager" );

        this.state = {
            gui: null,
            comps: []
        };

        this.busAddress = new APSBusAddress( APP_NAME );

        this.apsEventBus = new APSEventBus();

        this.alerter = new APSAlerter(this.apsEventBus);
    }

    //
    // Component Interactions
    //

    /**
     * React callback for when component is available.
     */
    componentDidMount() {

        this.apsEventBus.addBusRouter( new APSLocalEventBusRouter( this.busAddress ) );
        this.apsEventBus.addBusRouter( new APSVertxEventBusRouter( this.busAddress, this.alerter ) );

        // Subscribe to eventbus for content events.
        this.apsEventBus.subscribe( {
            headers: { routing: { incoming: `${EVENT_ROUTES.BACKEND},${EVENT_ROUTES.CLIENT},${EVENT_ROUTES.ALL},${EVENT_ROUTES.ALL_CLIENTS}` } },
            subscriber: ( message ) => {
                this.messageHandler( message );
            }
        } );

        // Inform a backend that there is a new client available and provide clients unique address.
        this.apsEventBus.message( {

            headers: { routing: { outgoing: `${EVENT_ROUTES.BACKEND}` } },

            message: {
                aps: {
                    origin: this.busAddress.client,
                    app: "aps-web-manager",
                    type: "avail"
                }
            }
        } );

        // For testing ...
        //this.fakeContentForTestDebugAndPOC();
    }

    /**
     * Handles received messages.
     *
     * @param message The received message.
     */
    messageHandler( message: {} ) {
        try {
            this.logger.debug( `>>>>>>>: message: ${JSON.stringify( message )}` );
        }
        catch ( e ) {
            this.logger.error(`Failed to log: ${e}`);
        }

        if (message.aps) {
            switch ( message.aps.type.toLowerCase() ) {

                case "gui":
                    this.updateGui( message.content );
                    break;

                // Unknown message are OK and expected. But a default: i required to not get a Babel warning.
                default:
            }
        }
        else {
            this.logger.error( `RECEIVED MESSAGE OF UNKNOWN FORMAT: ${message}` )
        }
    }

    /**
     * React is telling us the component will be removed.
     */
    componentWillUnmount() {

        // Since we are going away, stop listening for events.
        this.apsEventBus.unsubscribe( {
            headers: { routing: this.subscribingRoute },
            subscriber: this.messageHandler
        } );
    }

    /**
     * Provides component rendering.
     *
     * @returns {Array} Since this component is very dynamic we provide an array of components, and what
     *                  those are, are up to the input to the component.
     */
    render() {
        // gui: The gui spec received.
        // comps: The components created from the gui spec.
        // If the gui spec is empty no components should be rendered even if there happens to be comps.
        return this.state.gui != null ? this.state.comps : <b>Waiting for gui ...</b>;
    }

    //
    // Methods
    //

    /**
     * Updates the GUI. Component must be mounted before this can be called.
     *
     * @param gui A specification for the GUI.
     */
    updateGui( gui: {} ) {

        this.setState( {

            gui: gui,
            comps: this.parseGui( gui, { key: 0 } )
        } );
    }

    /**
     * Parses the GUI spec and creates components.
     *
     * @param {Object} gui The GUI spec to parse.
     * @param {Object} arrKeyCon Hold the array key (.key) to use. This is needed since we return an array
     *                           of components, and React wants a unique key of components in arrays. It won't
     *                           break, but it will complain loudly!
     *
     * @returns {Array} created components.
     */
    parseGui( gui: {}, arrKeyCon: { key: number } ): [] {

        let content = [];
        let childContent = [];

        let children = gui['children'];

        if ( children != null ) {

            for ( let child of children ) {

                childContent.push( this.parseGui( child, arrKeyCon ) )
            }
        }

        let type = gui['type'];
        let mgrId = this.props.mgrId;
        if ( mgrId == null ) mgrId = uuid();

        // noinspection JSUnresolvedVariable
        switch ( type ) {

            case 'aps-layout':

                content.push(
                    <APSLayout key={++arrKeyCon.key} eventBus={this.apsEventBus} mgrId={mgrId} guiProps={gui}
                               origin={this.busAddress.client}>
                        {childContent}
                    </APSLayout>
                );
                break;

            case 'aps-panel':

                content.push(
                    <APSPanel key={++arrKeyCon.key} eventBus={this.apsEventBus} mgrId={mgrId} guiProps={gui}
                              origin={this.busAddress.client}>
                        {childContent}
                    </APSPanel>
                );
                break;

            case 'aps-button':

                content.push(
                    <APSButton key={++arrKeyCon.key} eventBus={this.apsEventBus} mgrId={mgrId} guiProps={gui}
                               origin={this.busAddress.client}/>
                );
                break;

            case 'aps-text-field':

                content.push(
                    <APSTextField key={++arrKeyCon.key} eventBus={this.apsEventBus} mgrId={mgrId} guiProps={gui}
                                  origin={this.busAddress.client}/>
                );
                break;

            case 'aps-text-area':

                content.push(
                    <APSTextArea key={++arrKeyCon.key} eventBus={this.apsEventBus} mgrId={mgrId} guiProps={gui}
                                 origin={this.busAddress.client}/>
                );
                break;

            case 'aps-number':

                content.push(
                    <APSNumber key={++arrKeyCon.key} eventBus={this.apsEventBus} mgrId={mgrId} guiProps={gui}
                               origin={this.busAddress.client}/>
                );
                break;

            case 'aps-date':

                content.push(
                    <APSDate key={++arrKeyCon.key} eventBus={this.apsEventBus} mgrId={mgrId} guiProps={gui}
                             origin={this.busAddress.client}/>
                );
                break;

            case 'aps-check-box':

                content.push(
                    <APSCheckBox key={++arrKeyCon.key} eventBus={this.apsEventBus} mgrId={mgrId} guiProps={gui}
                            origin={this.busAddress.client}/>
                );
                break;

            case 'aps-markdown':

                content.push(
                    <APSMarkdown key={++arrKeyCon.key} eventBus={this.apsEventBus} mgrId={mgrId} guiProps={gui}
                            origin={this.busAddress.client}/>
                );
                break;

            case 'aps-alert':

                content.push(
                    <APSAlert key={++arrKeyCon.key} eventBus={this.apsEventBus} mgrId={mgrId} guiProps={gui}
                                 origin={this.busAddress.client}/>
                );
                break;

            default:
                console.error( "Bad 'type': " + type );
            //     throw "Bad type '" + type + "' in GUI specification JSON!"
        }

        return content;
    }
}
