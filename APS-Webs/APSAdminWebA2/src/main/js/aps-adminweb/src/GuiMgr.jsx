import React, { Component } from 'react'
import './GuiMgr.css'
import LocalEventBus from "./LocalEventBus"
import APSLayout from "./components/APSLayout"
import APSButton from "./components/APSButton";
import APSTextField from "./components/APSTextField"
import APSTextArea from "./components/APSTextArea"

let testData = {
    id: "top",
    name: "top",
    type: "layout",
    layout: "horizontal",
    children: [
        {
            id: "name",
            name: "name-field",
            type: "textField",
            width: 20,
            value: "name",
            listenTo: "test-gui",
            publishTo: "test-gui",
            publishGlobal: false,
            subscribeGlobal: false
        },
        {
            id: "description",
            name: "descriptionField",
            type: "textArea",
            cols: 30,
            rows: 4,
            value: "description",
            listenTo: "test-gui",
            publishTo: "test-gui",
            publishGlobal: false,
            subscribeGlobal: false
        },
        {
            id: "submit",
            name: "submitButton",
            type: "button",
            label: "Save",
            disabled: true,
            track: ["name", "descriptor"],
            enabledOn: "tracked-non-empty",
            disabledOn: null,
            listenTo: "test-gui",
            publishTo: "test-gui",
            publishGlobal: true,
            subscribeGlobal: true
        }
    ]
};

/*
 * Strange that JS does not have build in support for UUID!
 *
 * Found this on: https://stackoverflow.com/questions/105034/create-guid-uuid-in-javascript
 */
function uuidv4() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace( /[xy]/g, function ( c ) {
        let r = Math.random() * 16 | 0, v = c === 'x' ? r : ( (r & 0x3) | 0x8 );
        return v.toString( 16 )
    } )
}

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

    /**
     * Creates a new GuiMgr component.
     *
     * @param props The standard properties.
     */
    constructor( props ) {
        super( props );

        this.state = { gui: testData };

        this.uuid = uuidv4();
        let listenAddress = "guimgr:" + this.uuid;

        this.eventBus = new LocalEventBus();

        /**
         * @type {string} address
         * @type {string} message
         */
        this.eventBus.subscribe( listenAddress, ( address, message ) => {

            if ( address === listenAddress ) {

                let msg = JSON.parse( message );

                if ( msg['type'] === "gui" ) {

                    this.state.gui = msg['gui']
                }
            }
        } );

        this.eventBus.send( "aps:web", JSON.stringify({ op: "init", client: listenAddress }), true );
    }

    /**
     * Creates a client specific id also containing this instances UUID.
     *
     * @param {string} id The id to append to the internal UUID.
     *
     * @returns {string} A combined id.
     */
    createClientId(id) {

        return this.uuid + ":" + id;
    }

    /**
     * Parses the GUI spec and creates components.
     *
     * @param {Object} gui
     *
     * @returns {Array} created components.
     */
    parseGui( gui ) {

        let content = [];
        let childContent = [];

        let children = gui['children'];

        if ( children != null ) {

            for ( let child of children ) {

                childContent.push( this.parseGui( child ) )
            }
        }

        let type = gui['type'];

        // noinspection JSUnresolvedVariable
        switch ( type ) {

            case 'layout':

                // noinspection JSUnresolvedVariable
                if ( gui['layout'] === "horizontal" ) {

                    content.push(
                        <APSLayout orientation="horiz">
                            {childContent}
                        </APSLayout>
                    )
                }
                else {

                    content.push(
                        <APSLayout orientation="vert">
                            {childContent}
                        </APSLayout>
                    )
                }

                break;

            case 'button':

                content.push(
                    <APSButton eventBus={this.eventBus} mgrId={this.uuid} guiProps={gui}/>
                );

                break;

            case 'textField':

                content.push(
                    <APSTextField eventBus={this.eventBus} mgrId={this.uuid} guiProps={gui}/>
                );

                break;

            case 'textArea':

                content.push(
                    <APSTextArea eventBus={this.eventBus} mgrId={this.uuid} guiProps={gui}/>
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
                console.log("ERROR: Bad 'type': " + type);
            //     throw "Bad type '" + type + "' in GUI specification JSON!"
        }

        return content
    }

    render() {
        return this.state.gui != null ? this.parseGui( this.state.gui ) : <h1>No gui available!</h1>;
    }

}

// noinspection JSUnusedGlobalSymbols
export default GuiMgr
