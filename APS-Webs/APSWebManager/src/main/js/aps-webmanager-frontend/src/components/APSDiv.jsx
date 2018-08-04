import React from 'react'
import APSComponent from "./APSComponent"

class APSDiv extends APSComponent {

    constructor( props ) {
        super( props );

        this.state = {
            disabled: props.guiProps.disabled != null ? props.guiProps.disabled : false
        };

        this.hasValue = false;
    }

    componentId() {
        return "APSDiv";
    }

    set disabled( state ) {
        let _state = this.state;
        _state.disabled = state;
        this.setState( _state );
    }

    render() {

        return <div className={this.props.guiProps.class + "aps-div"} aria-disabled={this.state.disabled}>
            ${this.props.children}
        </div>
    }
}

// noinspection JSUnusedGlobalSymbols
export default APSDiv;
