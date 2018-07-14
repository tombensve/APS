import React from 'react'
import './APSTextArea.css'
import APSComponent from "./APSComponent"

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
            text: event.target.value
        } );

        // Handle emptiness.
        this.empty = ( event.target.value === "" );

        this.send( this.eventMsg( {
            componentType: "textArea",
            value: event.target.value,
            action: "changed"
        } ) );
    }

    render() {

        // noinspection HtmlUnknownAttribute
        return <textarea className={this.props.guiProps.class + " apsTextArea"}
                         id={this.props.guiProps.id}
                         rows={this.props.guiProps.rows} cols={this.props.guiProps.cols}
                         onChange={this.handleEvent.bind( this )} disabled={this.state.disabled}
                         defaultValue={this.state.value}/>
    }
}

// noinspection JSUnusedGlobalSymbols
export default APSTextArea;
