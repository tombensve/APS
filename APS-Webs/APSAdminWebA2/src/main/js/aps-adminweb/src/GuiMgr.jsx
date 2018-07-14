import React, { Component } from 'react'
import './GuiMgr.css'
import LocalEventBus from "./LocalEventBus"
import LocalBusRouter from "./LocalBusRouter"
import APSLayout from "./components/APSLayout"
import APSButton from "./components/APSButton"
import APSTextField from "./components/APSTextField"
import APSTextArea from "./components/APSTextArea"
import { ROUTE_EXTERNAL, ROUTE_LOCAL } from "./Consts"
import { uuidv4 } from "./UUID"

/**
 * A component reading a JSON spec of components to render. The GuiMgr also creates a local
 * event bus passed on to components. The JSON spec provides messages for components to send
 * on the bus. Some will be forwarded on a global cluster bus also reaching the backend.
 *
 * The JSON spec to parse is provided on the event bus, and usually comes from the backend,
 * but may also come from the frontend. This component does not care in any way where the
 * JSON spec comes from.
 */
class GuiMgr extends Component {

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

        this.state = {
            gui: null,
            comps: []
        };

        this.uuid = uuidv4();
        this.listenAddress = "guimgr:" + this.uuid;

        this.eventBus = new LocalEventBus();
        this.eventBus.addBusRouter( new LocalBusRouter() );
        //this.eventBus.addBusRouter( new VertxEventBusRouter() );

        /**
         * Event bus subscriber. We need to keep the instance of this so that we can unsubscribe later.
         *
         * @param {String} address - The address the message is to. Always received if you use same function
         *                           for multiple subscriptions.
         * @param {String} message - A received message in JSON format.
         */
        this.compSubscriber = ( address, message ) => {

            if ( address === this.listenAddress ) {

                let msg = JSON.parse( message );

                if ( msg['type'] === "gui" ) {

                    this.updateGui( msg['gui'] );
                }
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

        // Subscribe to eventbus for content events.
        this.eventBus.subscribe( this.listenAddress, this.compSubscriber, ROUTE_LOCAL );

        // Inform someone that there is a new client available and provide clients unique address.
        this.eventBus.send( "aps:web", JSON.stringify( { op: "init", client: this.listenAddress } ), ROUTE_EXTERNAL );

        // For testing ...
        this.fakeContentForTestDebugAndPOC();
    }

    /**
     * React is telling us the component will be removed.
     */
    componentWillUnmount() {

        // Since we are going away, stop listening for events.
        this.eventBus.unsubscribe( this.listenAddress, this.compSubscriber, ROUTE_LOCAL );
    }

    /**
     * Provides component rendering.
     *
     * @returns {Array} Since this component is very dynamic we provide an array of components, and what
     *                  those are, are up to the input to the component.
     */
    render() {
        return this.state.gui != null ? this.state.comps : <h1>No gui available!</h1>;
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

        // Should hopefully trigger a render ...
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

            case 'button':

                content.push(
                    <APSButton key={++arrKeyCon.key} eventBus={this.eventBus} mgrId={this.uuid} guiProps={gui}/>
                );

                break;

            case 'textField':

                content.push(
                    <APSTextField key={++arrKeyCon.key} eventBus={this.eventBus} mgrId={this.uuid} guiProps={gui}/>
                );

                break;

            case 'textArea':

                content.push(
                    <APSTextArea key={++arrKeyCon.key} eventBus={this.eventBus} mgrId={this.uuid} guiProps={gui}/>
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
                console.log( "ERROR: Bad 'type': " + type );
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
                    class: "",
                    listenTo: "test-gui",
                    publishTo: "test-gui",
                    routing: "local",
                },
                {
                    id: "description",
                    name: "descriptionField",
                    group: "gpoc",
                    type: "textArea",
                    cols: 30,
                    rows: 4,
                    value: "",
                    class: "",
                    listenTo: "test-gui",
                    publishTo: "test-gui",
                    routing: "local"
                },
                {
                    id: "submit",
                    name: "submitButton",
                    group: "gpoc",
                    type: "button",
                    label: "Save",
                    class: "",
                    disabled: true,
                    collectGroups: "gpoc",
                    enabled: "groupNotEmpty:gpoc",
                    listenTo: "test-gui",
                    publishTo: "test-gui",
                    routing: "local,external"
                }
            ]
        };

        // This would normally come from elsewhere.
        this.eventBus.send( this.listenAddress, JSON.stringify( { type: "gui", gui: testData } ), ROUTE_LOCAL );
    }
}


// noinspection JSUnusedGlobalSymbols
export default GuiMgr
