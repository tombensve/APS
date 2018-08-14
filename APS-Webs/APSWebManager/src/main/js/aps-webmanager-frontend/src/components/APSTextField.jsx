import React from 'react'
import APSComponent from "./APSComponent"
import { FormControl } from 'react-bootstrap'

/**
 * ## Properties
 *
 * ### guiProps.id
 *
 * The id of the component.
 *
 * ### guiProps.value
 *
 * The initial value of the component.
 *
 * ### guiProps.placeholder
 *
 * A greyed out placeholder for the component.
 *
 */
class APSTextField extends APSComponent {

    constructor( props ) {
        super( props );

        this.state = {
            disabled: false,
            // Note: The name of the state value has to match the component value name to be a "controlled" component!
            value: this.props.guiProps.value
        };

    }

    componentType() {
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

        this.message(
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
        if (this.props.guiProps.placeholder != null) {
            placeHolder = this.props.guiProps.placeholder;
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
