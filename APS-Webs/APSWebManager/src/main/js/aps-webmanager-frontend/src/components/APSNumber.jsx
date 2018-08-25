import React from 'react'
import APSComponent from "./APSComponent";
import NumericInput from 'react-numeric-input';

/**
 * # Component
 *
 * This uses the following component: https://www.npmjs.com/package/react-numeric-input.
 * It is not one of the react-bootstrap components.
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

    constructor( props ) {
        super( props );

        this.state = {
            value: this.props.guiProps.value,
            disabled: props.guiProps.disabled != null ? props.guiProps.disabled : false
        };
        this.setState( this.state );

        this.empty = false;
        this.hasValue = true;
    }

    componentType() {
        return "aps-number";
    }

    /**
     * Event handler. See https://www.npmjs.com/package/react-numeric-input#event-callbacks
     *
     * @param valueAsString
     * @param valueAsNumber
     * @param element
     */
    handleEvent( valueAsNumber, valueAsString, element ) {
        this.setState( {
            value: valueAsNumber,
            disabled: this.state.disabled
        } );

        this.message(
            this.changeEvent(
                {
                    componentType: this.componentType(),
                    value: valueAsNumber
                }
            )
        );
    }

    render() {
        // From: https://www.npmjs.com/package/react-numeric-input  License: MIT
        return <NumericInput
            min={this.props.guiProps.min}
            max={this.props.guiProps.max}
            value={this.state.value}
            onChange={this.handleEvent.bind( this )}
            style={{
                input: {
                    "border-radius": '4px'
                }
            }}
        />;
    }
}