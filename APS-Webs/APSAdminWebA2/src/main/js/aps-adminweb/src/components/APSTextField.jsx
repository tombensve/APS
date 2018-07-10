import React from 'react'
import './APSTextField.css'
import APSComponent from "./APSComponent"

class APSTextField extends APSComponent {

    constructor( props ) {
        super( props );

        this.state = {
            disabled: false,
            text: this.props.guiProps.text
        };

    }

    componentId() { return "APSTextField"; }

    set disabled( state ) {
        this.state.disabled = state;
    }

    handleEvent( event ) {
        //console.log( this.componentId() + " : " + JSON.stringify(event) );
        console.log( this.componentId() + " : " + event.type)
        event.preventDefault();

        this.send( JSON.stringify( {
            type: "gui-event",
            componentType: "textField",
            value: this.state.text,
            action: "changed",
            managerId: this.props.mgrId,
            componentId: this.props.guiProps.id
        } ) );
    }

    render() {
        return <input value={this.state.text} type="text" onChange={this.handleEvent.bind( this )}
                      disabled={this.state.disabled}/>
    }
}

// noinspection JSUnusedGlobalSymbols
export default APSTextField;
