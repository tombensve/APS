import React from 'react'
import APSComponent from "./APSComponent"
import { ControlLabel, FormControl } from 'react-bootstrap'

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
 * ### guiProps.rows
 *
 * The number of rows for the text area.
 *
 * ### guiProps.cols
 *
 * The number of columns in the text area.
 *
 * ### guiProps.placeholder
 *
 * A greyed out placeholder for the component.
 */
export default class APSTextArea extends APSComponent {

    constructor( props: {} ) {

        super( props );

        this.defaultValue = this.props.guiProps.value != null ? this.props.guiProps.value : "";

        this.state = {

            disabled: false,
            value: this.defaultValue
        };

        this.empty = true;
    }

    componentType(): string {
        return "aps-text-area";
    }

    set disabled( state: boolean ) {

        let _state = this.state;
        _state.disabled = state;
        this.setState( _state );
    }

    handleEvent( event: {} ) {

        this.setState( {
            disabled: this.state.disabled,
            value: event.target.value
        } );

        // Handle emptiness.
        this.empty = ( event.target.value === "" );

        this.message(
            this.changeEvent(
                {
                    componentType: this.componentType(),
                    value: event.target.value
                }
            )
        );
    }

    componentDidMount() {

    }

    doRender( comps ) {
        let placeHolder = "";
        if ( this.props.guiProps.placeholder != null ) {
            placeHolder = this.props.guiProps.placeholder;
        }

        if ( this.props.guiProps.label ) {
            comps.push( <ControlLabel>{this.props.guiProps.label}</ControlLabel> );
        }

        // noinspection HtmlUnknownAttribute
        comps.push( <FormControl componentClass="textarea"
                                 type={"textarea"}
                                 value={this.state.value}
                                 id={this.props.guiProps.id}
                                 rows={this.props.guiProps.rows}
                                 cols={this.props.guiProps.cols}
                                 placeHolder={placeHolder}
                                 onChange={this.handleEvent.bind( this )}
                                 disabled={this.state.disabled}
        /> );
    }
}

