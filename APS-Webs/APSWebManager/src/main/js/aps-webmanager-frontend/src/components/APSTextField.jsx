import React from 'react'
import APSComponent from "./APSComponent"
import { FormControl, FormGroup } from 'react-bootstrap'

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

        this.defaultValue = "";

        this.state = {
            disabled: false,
            // Note: The name of the state value has to match the component value name to be a "controlled" component!
            value: this.props.guiProps.value,
            validationState: ""
        };

        this.empty = true;

    }

    componentType() {
        return "aps-text-field";
    }

    set disabled( state ) {
        let _state = this.state;
        _state.disabled = state;
        this.setState( _state );
    }

    set validationState( state ) {
        let _state = this.state;
        _state.validationState = state;
        this.setState( _state );
    }

    handleEvent( event ) {

        this.setState( {
            disabled: this.state.disabled,
            value: event.target.value,
            validationState: this.state.validationState
        } );

        this.empty = ( event.target.value === "" );

        // event.stopPropagation();

        this.message(
            this.changeEvent(
                {
                    componentType: this.componentType(),
                    value: event.target.value
                }
            )
        );

        console.log( this.name + " : " + event.type + " : " + event.target.value );
    }

    componentDidMount() {
        this.sendDefaultValue();
    }

    render() {

        let placeHolder = "";
        if ( this.props.guiProps.placeholder != null ) {
            placeHolder = this.props.guiProps.placeholder;
        }
        return <FormGroup id={this.props.guiProps.id + '_fg'} validationState={this.state.validationState}>
            <FormControl componentClass="input"
                         value={this.state.value}
                         id={this.props.guiProps.id}
                         placeholder={placeHolder}
                         onChange={this.handleEvent.bind( this )}
                         disabled={this.state.disabled}/>
            <FormControl.Feedback/>
        </FormGroup>
    }
}

// noinspection JSUnusedGlobalSymbols
export default APSTextField;
