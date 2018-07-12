import React from 'react'
import './APSTextArea.css'
import APSComponent from "./APSComponent"

class APSTextArea extends APSComponent {

    constructor( props ) {

        super( props );

        this.state = {

            disabled: false,
            text: this.props.guiProps.text
        };

    }

    componentId() {
        return "APSTextArea";
    }

    set disabled( state ) {

        let _state = this.state;
        _state.disabled = state;
        this.setState(_state);
    }

    handleEvent( event ) {

        this.setState( {
            disabled: this.state.disabled,
            text: event.target.value
        } );

        // Handle emptiness.
        this.empty = ( event.target.value === "" );

        this.send( this.eventMsg( {
            componentType: "textArea",
            value: this.state.text,
            action: "changed"
        } ) );
    }

    render() {

        return <textarea rows={this.props.guiProps.rows} cols={this.props.guiProps.cols}
                         onChange={this.handleEvent.bind( this )} disabled={this.state.disabled}>
            {this.state.text}
        </textarea>
    }
}

// noinspection JSUnusedGlobalSymbols
export default APSTextArea;
