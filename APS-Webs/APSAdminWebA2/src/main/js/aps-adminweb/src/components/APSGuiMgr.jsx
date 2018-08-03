import React, { Component } from 'react'
import './APSGuiMgr.css'
import LocalEventBus from "./../LocalEventBus"
import LocalBusRouter from "./../LocalBusRouter"
import VertxEventBusRouter from "./../VertxEventBusRouter"
import APSLayout from "./APSLayout"
import APSDiv from "./APSDiv"
import APSButton from "./APSButton"
import APSTextField from "./APSTextField"
import APSTextArea from "./APSTextArea"
import { uuidv4 } from "./../UUID"
import { ADDR_NEW_CLIENT } from "../Constants"
import APSLogger from "../APSLogger"

/**
 * A component reading a JSON spec of components to render. The GuiMgr also creates a local
 * event bus passed on to components. The JSON spec provides messages for components to send
 * on the bus. Some will be forwarded on a global cluster bus also reaching the backend.
 *
 * The JSON spec to parse is provided on the event bus, and usually comes from the backend,
 * but may also come from the frontend. This component does not care in any way where the
 * JSON spec comes from.
 */
class APSGuiMgr extends Component {

    //
    // Constructors
    //

    /**
     * Creates a new GuiMgr component.
     *
     * @param props The standard properties.
     */
    constructor( props ) {
        super( props );

        this.logger = new APSLogger( "APSGuiMgr" );

        this.state = {
            gui: null,
            comps: []
        };

        this.uuid = uuidv4();
        this.listenAddress = "aps:guimgr:" + this.uuid;

        this.localEventBus = new LocalEventBus();

        /**
         * Event bus subscriber. We need to keep the instance of this so that we can unsubscribe later.
         *
         * @param {string} message - A received message in JSON format.
         */
        this.compSubscriber = ( message ) => {

            this.logger.debug( ">>>>>>>: message: {}",  message  );

            if ( message['msgType'] === "gui" ) {

                this.updateGui( message['msgData'] );
            }
        };
    }

    //
    // Component Interactions
    //

    /**
     * React callback for when component is available.
     */
    componentDidMount() {

        this.localEventBus.addBusRouter( new LocalBusRouter() );
        this.localEventBus.addBusRouter( new VertxEventBusRouter() );

        // Subscribe to eventbus for content events.
        this.localEventBus.subscribe( this.listenAddress, { routing: "local,external" }, ( message ) => {
            // noinspection JSCheckFunctionSignatures
            this.compSubscriber( message );
        } );

        // Inform someone that there is a new client available and provide clients unique address.
        this.localEventBus.send( ADDR_NEW_CLIENT, { op: "init", client: this.listenAddress }, { routing: "external" } );

        // For testing ...
        //this.fakeContentForTestDebugAndPOC();
    }

    /**
     * React is telling us the component will be removed.
     */
    componentWillUnmount() {

        // Since we are going away, stop listening for events.
        this.localEventBus.unsubscribe( this.listenAddress, { routing: "local,external" }, this.compSubscriber );
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
    updateGui( gui ) {

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
    parseGui( gui, arrKeyCon ) {

        let content = [];
        let childContent = [];

        let children = gui['children'];

        if ( children != null ) {

            for ( let child of children ) {

                childContent.push( this.parseGui( child, arrKeyCon ) )
            }
        }

        let type = gui['type'];

        // noinspection JSUnresolvedVariable
        switch ( type ) {

            case 'layout':

                // noinspection JSUnresolvedVariable
                if ( gui['layout'] === "horizontal" ) {

                    content.push(
                        <APSLayout key={++arrKeyCon.key} orientation="horiz">
                            {childContent}
                        </APSLayout>
                    )
                }
                else {

                    content.push(
                        <APSLayout key={++arrKeyCon.key} orientation="vert">
                            {childContent}
                        </APSLayout>
                    )
                }

                break;

            case 'div':

                content.push(
                    <APSDiv key={++arrKeyCon.key}>
                        {childContent}
                    </APSDiv>
                );

                break;

            case 'button':

                content.push(
                    <APSButton key={++arrKeyCon.key} eventBus={this.localEventBus} mgrId={this.uuid} guiProps={gui}/>
                );

                break;

            case 'textField':

                content.push(
                    <APSTextField key={++arrKeyCon.key} eventBus={this.localEventBus} mgrId={this.uuid} guiProps={gui}/>
                );

                break;

            case 'textArea':

                content.push(
                    <APSTextArea key={++arrKeyCon.key} eventBus={this.localEventBus} mgrId={this.uuid} guiProps={gui}/>
                );

                break;

            case 'number':

                break;

            case 'date':

                break;

            case 'header':

                break;

            case 'text':

                break;

            case 'markdown':

                break;

            default:
                console.error( "Bad 'type': " + type );
            //     throw "Bad type '" + type + "' in GUI specification JSON!"
        }

        return content
    }

    /**
     * This produces real browser output.
     */
    fakeContentForTestDebugAndPOC() {
        let testData = {
            id: "top",
            name: "top",
            type: "layout",
            layout: "horizontal",
            children: [
                {
                    id: "name",
                    name: "name-field",
                    group: "gpoc",
                    type: "textField",
                    width: 20,
                    value: "",
                    class: "form-control",
                    listenTo: "aps:test-gui",
                    publishTo: "aps:test-gui",
                    headers: { routing: "local" }
                },
                {
                    id: "description",
                    name: "descriptionField",
                    group: "gpoc",
                    type: "textArea",
                    cols: 30,
                    rows: 4,
                    value: "",
                    class: "form-control",
                    listenTo: "aps:test-gui",
                    publishTo: "aps:test-gui",
                    headers: { routing: "local" }
                },
                {
                    id: "submit",
                    name: "submitButton",
                    group: "gpoc",
                    type: "button",
                    label: "Save",
                    class: "btn btn-success",
                    disabled: true,
                    collectGroups: "gpoc",
                    enabled: "groupNotEmpty:gpoc",
                    listenTo: "aps:test-gui",
                    publishTo: "aps:test-gui",
                    headers: { routing: "external" }
                }
            ]
        };

        // This would normally come from elsewhere.
        this.localEventBus.send( this.listenAddress, { routing: "local" }, { msgType: "gui", msgData: testData } );
    }
}


// noinspection JSUnusedGlobalSymbols
export default APSGuiMgr
