import React from 'react'
import APSComponent from "./APSComponent"
import { Button } from 'react-bootstrap'

/**
 * ## Properties

 * ### guiProps.button.style
 *
 * Takes one of the bootstrap style names. Ex: "warning", "error", "info", ...
 *
 * ### guiProps.button.label
 *
 * A label for the button.
 */
class APSButton extends APSComponent {

    constructor( props ) {
        super( props );

        this.disabled = props.guiProps.disabled != null ? props.guiProps.disabled : false;
        this.hasValue = false;
    }

    // Override
    componentType() {
        return "aps-button";
    }

    // Override
    set disabled( disabled ) {
        if ( !this.state ) {
            this.state = {};
        }
        this.state.disabled = disabled;
        this.setState( this.state );
    }

    handleEvent( event ) {
        console.log( this, event );

        this.message(
            this.submitActionEvent( { componentType: this.componentType() } )
        );
    }

    render() {

        // noinspection HtmlUnknownAttribute
        return <Button bsStyle={this.props.guiProps.style != null ? this.props.guiProps.style : "success"}
                       id={this.props.guiProps.id}
                       onClick={this.handleEvent.bind( this )}
                       disabled={this.state.disabled}>
            {this.props.guiProps.label}
        </Button>

    }
}

// noinspection JSUnusedGlobalSymbols
export default APSButton;
