import React from 'react'
import APSComponent from "./APSComponent"
import { Button } from 'react-bootstrap'

/**
 * ### Button specific properties
 *
 * #### guiProps.button.style
 *
 * Takes one of the bootstrap style names. Ex: "warning", "error", "info", ...
 *
 * #### guiProps.button.label
 *
 * A label for the button.
 */
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

        this.send(
            this.submitActionEvent(
                {
                    componentType: "button"
                }
            )
        );
    }

    render() {

        // noinspection HtmlUnknownAttribute
        return <Button bsStyle={this.props.guiProps.button.style != null ? this.props.guiProps.button.style : "success"}
                       id={this.props.guiProps.id}
                       onClick={this.handleEvent.bind( this )}
                       disabled={this.state.disabled}>
            {this.props.guiProps.button.label}
        </Button>

    }
}

// noinspection JSUnusedGlobalSymbols
export default APSButton;
