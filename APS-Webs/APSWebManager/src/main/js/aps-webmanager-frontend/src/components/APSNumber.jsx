import APSComponent from "./APSComponent"
import { ControlLabel, FormControl, FormGroup } from "react-bootstrap";
import React from "react";

/**
 * # Component
 *
 * A simple numeric input. Takes an optional min and a max. Floating point numbers and integers allowed.
 *
 * There is no GUI fanciness here. Just an input field. However '<' or '-' will decrease value by 1 and
 * '>' or '+' will increase value by one.
 *
 * ## Properties
 *
 * ### guiProps.min
 *
 * The minimum value.
 *
 * ### guiProps.max
 *
 * The max value.
 *
 * ### guiProps.value
 *
 * The starting value.
 */
export default class APSNumber extends APSComponent {

    constructor( props: {} ) {
        super( props );

        this.props.guiProps.placeholder = "0";

        this.defaultValue = this.props.guiProps.value ? this.props.guiProps.value : "0";

        this.state = {
            value: this.defaultValue,
            disabled: props.guiProps.disabled != null ? props.guiProps.disabled : false,
        };
        this.setState( this.state );

        this.defaultValue = this.props.guiProps.value;
        this.min = Number( this.props.guiProps.min );
        this.max = Number( this.props.guiProps.max );
        this.empty = false;
        this.hasValue = true;
    }

    /**
     * @returns {string} The name/type of this component.
     */
    componentType(): string {
        return "aps-number";
    }

    set disabled( state: boolean ) {
        let _state = this.state;
        _state.disabled = state;
        this.setState( _state );
    }

    /**
     * Event handler
     *
     * @param event
     */
    handleEvent( event: {} ) {

        let value: string = event.target.value;

        // Exclude any characters not part of a number.
        let iterator = value[Symbol.iterator]();
        let currChar = iterator.next();
        let filteredValue = "";

        if ( currChar.value === '-' ) {
            filteredValue = filteredValue + currChar.value;
        }
        while ( !currChar.done ) {

            if ( "0123456789.".indexOf( currChar.value ) >= 0 ) {
                filteredValue = filteredValue + currChar.value;
            }

            currChar = iterator.next();
        }

        // Conversion to number will fail if only '-'. Since we get called on every character
        // we have to check for this.
        if ( filteredValue !== "-" ) {
            // Convert string to number and validate min and max.
            let validationNumber = null;
            if ( filteredValue.indexOf( '.' ) >= 0 ) {
                validationNumber = Number.parseFloat( filteredValue );
            }
            else {
                validationNumber = Number.parseInt( filteredValue, 10 );
            }

            if ( !Number.isNaN( validationNumber ) ) {

                // Allows >/+ to increase number and </- to decrease number.
                if ( value.endsWith( ">" ) || value.endsWith( "+" ) ) {
                    validationNumber = validationNumber + 1;
                    filteredValue = validationNumber.toString( 10 );
                }
                else if ( value.endsWith( "<" ) || value.endsWith( "-" ) ) {
                    validationNumber = validationNumber - 1;
                    filteredValue = validationNumber.toString( 10 );
                }

                // Limit value between max and min if provided.
                if ( !Number.isNaN( this.min ) && validationNumber < this.min ) {
                    filteredValue = "" + this.min;
                }

                if ( !Number.isNaN( this.max ) && ( validationNumber > this.max ) ) {
                    filteredValue = "" + this.max;
                }
            }
        }

        // Only update value and send event if value actually changed after filtering.
        if ( this.state.value !== filteredValue ) {
            this.setState( {
                value: filteredValue,
                disabled: this.state.disabled
            } );


            this.message(
                this.changeEvent(
                    {
                        componentType: this.componentType(),
                        value: filteredValue
                    }
                )
            );
        }
    }

    doRender( comps ) {

        let placeHolder = "";
        if ( this.props.guiProps.placeholder != null ) {
            placeHolder = this.props.guiProps.placeholder;
        }

        if ( this.props.guiProps.label ) {
            comps.push( <ControlLabel>{this.props.guiProps.label}</ControlLabel> );
        }

        // Note that if type="number" is added then things turn to shit!
        // 1) The filtering above no longer have any effect. It seem to keep its own value and ignore the set value.
        // 2) > to increase and < to decrease no longer works due to same reason.
        // 3) On iOS and Android the number field does not work at all. You can enter any number, min and max has
        //   no effect.
        //
        // But when I treat this as a text field and do my own filtering on accepted input and check the min and max
        // limits myself, then this works fine both on my Mac, my iPhone and my virtual android. The '>' and '<' to
        // change value does not work on the mobiles.
        comps.push( <FormControl componentClass="input"
                                 value={this.state.value}
                                 id={this.props.guiProps.id}
                                 placeholder={placeHolder}
                                 onChange={this.handleEvent.bind( this )}
                                 disabled={this.state.disabled}/> );
    }

}