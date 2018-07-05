import React, { Component } from 'react';
import './GuiMgr.css';
import LocalEventBus from "./LocalEventBus";

/*
 * Strange that JS does not have build in support for UUID!
 *
 * Found this on: https://stackoverflow.com/questions/105034/create-guid-uuid-in-javascript
 */
function uuidv4() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace( /[xy]/g, function ( c ) {
        let r = Math.random() * 16 | 0, v = c === 'x' ? r : ( r & 0x3 | 0x8 );
        return v.toString( 16 );
    } );
}

class GuiMgr extends Component {

    constructor( props ) {
        super( props );

        this.state = { changed: false };// Unsure if I need this!

        let uuid = uuidv4();
        let listenAddress = "guimgr:" + uuid;

        this.eventBus = LocalEventBus.INSTANCE;
        this.eventBus.subscribe( listenAddress, ( address, message ) => {
            if ( address === listenAddress ) {
                let msg = JSON.parse( message );

                this.gui = msg['gui'];
            }
        } );
        this.eventBus.send( "aps:web", { op: "init", client: listenAddress }, true );
    }

    render() {
        let result = null;

        // noinspection JSUnresolvedVariable
        switch ( gui['type'] ) {

            case 'layout':
                // noinspection JSUnresolvedVariable
                if ( gui['layout'] === "horizontal") {

                    result = (
                        <div className="layout-horiz">
                            {this.props.children}
                        </div>
                    );
                }
                else {

                    result = (
                        <div className="layout-vert">
                            {this.props.children}
                        </div>
                    );
                }

                break;

            default:
                throw "Bad type '" + type + "'"
        }

        return result;
    }
}

// noinspection JSUnusedGlobalSymbols
export default GuiMgr;
