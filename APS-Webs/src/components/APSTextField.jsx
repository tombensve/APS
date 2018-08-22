import React from 'react'
import APSComponent from "./APSComponent"
import { FormControl } from 'react-bootstrap'

class APSTextField extends APSComponent {

    constructor( props ) {
        super( props );

        this.state = {
            disabled: false,
            // Note: The name of the state value has to match the component value name to be a "controlled" component!
            value: this.props.guiProps.value
        };

    }

    componentId() {
        return "APSTextField";
    }

    set disabled( state ) {
        let _state = this.state;
        _state.disabled = state;
        this.setState( _state );
    }

    handleEvent( event ) {

        this.setState( {
            disabled: this.state.disabled,
            value: event.target.value
        } );

        this.empty = ( event.target.value === "" );

        // event.stopPropagation();

        this.send(
            this.changeEvent(
                {
                    componentType: "textField",
                    value: event.target.value
                }
            )
        );

        console.log( this.name + " : " + event.type + " : " + event.target.value );
    }

    render() {

        let placeHolder = "";
        if (this.props.guiProps.textField != null && this.props.guiProps.textField.placeholder != null) {
            placeHolder = this.props.guiProps.textField.placeholder;
        }
        return <FormControl componentClass="input"
                            value={this.state.value}
                            id={this.props.guiProps.id}
                            placeholder={placeHolder}
                            onChange={this.handleEvent.bind( this )}
                            disabled={this.state.disabled}/>
    }
}

// noinspection JSUnusedGlobalSymbols
export default APSTextField;
