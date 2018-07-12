import React from 'react'
import './APSButton.css'
import APSComponent from "./APSComponent"

class APSButton extends APSComponent {

    constructor( props ) {
        super( props );

        this.state = {
            disabled: props.guiProps.disabled != null ? props.guiProps.disabled : false
        };

    }

    componentId() { return "APSButton"; }

    set disabled( state ) {
        let _state = this.state;
        _state.disabled = state;
        this.setState(_state);
    }

    handleEvent( event ) {
        console.log( this, event );

        this.send( this.eventMsg( {
            componentType: "button"
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
