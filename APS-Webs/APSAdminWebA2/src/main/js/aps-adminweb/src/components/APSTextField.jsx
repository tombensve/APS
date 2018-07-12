import React from 'react'
import './APSTextField.css'
import APSComponent from "./APSComponent"

class APSTextField extends APSComponent {

    constructor( props ) {
        super( props );

        this.state = {
            disabled: false,
            // Note: The name of the state value has to match the component value name to be a "controlled" component!
            value: this.props.guiProps.text
        };

    }

    componentId() { return "APSTextField"; }

    set disabled( state ) {
        let _state = this.state;
        _state.disabled = state;
        this.setState(_state);
    }

    handleEvent( event ) {

        this.setState({
            disabled: this.state.disabled,
            text: event.target.value
        });

        this.empty = (event.target.value === "");

        // event.stopPropagation();

        this.send( this.eventMsg( {
            componentType: "textField",
            value: event.target.value,
            action: "changed"
        } ) );

        console.log( this.componentId() + " : " + event.type + " : " + event.target.value);
    }

    render() {
        return <input value={this.state.value} type="text" onChange={this.handleEvent.bind( this )}
                      disabled={this.state.disabled}/>
    }
}

// noinspection JSUnusedGlobalSymbols
export default APSTextField;
