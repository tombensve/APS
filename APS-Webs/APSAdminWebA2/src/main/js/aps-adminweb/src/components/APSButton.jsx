import React from 'react'
import './APSButton.css'
import APSComponent from "./APSComponent"

class APSButton extends APSComponent {

    constructor( props ) {
        super( props );

        this.state = {
            disabled: false
        };

    }

    componentId() { return "APSButton"; }

    set disabled( state ) {
        this.state.disabled = state;
    }

    handleEvent( event ) {
        console.log( this, event );

        this.send( JSON.stringify( {
            type: "gui-event",
            componentType: "button",
            managerId: this.props.mgrId,
            componentId: this.props.guiProps.id
        } ) );
    }

    render() {
        return <button onClick={this.handleEvent.bind( this )} disabled={this.state.disabled}>
            {this.props.guiProps.label}
        </button>
    }
}

// noinspection JSUnusedGlobalSymbols
export default APSButton;
