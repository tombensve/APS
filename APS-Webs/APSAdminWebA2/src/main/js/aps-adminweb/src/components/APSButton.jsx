import React from 'react'
import './APSButton.css'
import APSComponent from "./APSComponent"

class APSButton extends APSComponent {

    constructor( props ) {
        super( props );

        this.state = {
            disabled: props.guiProps.disabled != null ? props.guiProps.disabled : false
        };
        this.setState( this.state );

        this.hasValue = false;
    }

    componentId() {
        return "APSButton";
    }

    set disabled( disabled ) {
        this.state.disabled = disabled;
        // Note that just changing the this.state state does not affect anything. We must pass the
        // updated state to setState( state ) to have an effect.
        this.setState( this.state );
    }

    handleEvent( event ) {
        console.log( this, event );

        this.send( this.eventMsg( {
            componentType: "button"
        } ) );
    }

    render() {

        // noinspection HtmlUnknownAttribute
        return <button className={this.props.guiProps.class + " apsButton"}
                       id={this.props.guiProps.id}
                       onClick={this.handleEvent.bind( this )}
                       disabled={this.state.disabled}>
            {this.props.guiProps.label}
        </button>
    }
}

// noinspection JSUnusedGlobalSymbols
export default APSButton;
