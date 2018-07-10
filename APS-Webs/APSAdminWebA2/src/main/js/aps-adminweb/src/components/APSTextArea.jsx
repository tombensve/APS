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

    componentId() { return "APSTextArea"; }

    set disabled( state ) {

        this.state.disabled = state;
    }

    handleEvent( event ) {

        console.log( this, event );

        this.send( JSON.stringify( {
            type: "gui-event",
            componentType: "textArea",
            value: this.state.text,
            action: "changed",
            managerId: this.props.mgrId,
            componentId: this.props.guiProps.id
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
