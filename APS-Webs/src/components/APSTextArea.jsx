import React from 'react'
import APSComponent from "./APSComponent"
import { FormControl } from 'react-bootstrap'

class APSTextArea extends APSComponent {

    constructor( props ) {

        super( props );

        this.state = {

            disabled: false,
            value: this.props.guiProps.value != null ? this.props.guiProps.value : ""
        };

    }

    componentId() {
        return "APSTextArea";
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

        // Handle emptiness.
        this.empty = ( event.target.value === "" );

        this.send(
            this.changeEvent(
                {
                    componentType: "textArea",
                    value: event.target.value
                }
            )
        );
    }

    render() {

        // noinspection HtmlUnknownAttribute
        return <FormControl componentClass="textarea"
                            value={this.state.value}
                            id={this.props.guiProps.id}
                            rows={this.props.guiProps.textArea.rows}
                            cols={this.props.guiProps.textArea.cols}
                            onChange={this.handleEvent.bind( this )}
                            disabled={this.state.disabled}
        />
    }
}

// noinspection JSUnusedGlobalSymbols
export default APSTextArea;
