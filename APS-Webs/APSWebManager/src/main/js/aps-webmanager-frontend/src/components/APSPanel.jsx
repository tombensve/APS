import React from 'react'
import APSComponent from "./APSComponent"
import { Panel } from 'react-bootstrap'

export default class APSPanel extends APSComponent {

    constructor( props ) {
        super( props );

        this.state = {
            disabled: props.guiProps.disabled != null ? props.guiProps.disabled : false
        };

        this.hasValue = false;
        this.busMember = false;
    }

    componentId() {
        return "APSPanel";
    }

    set disabled( state ) {
        let _state = this.state;
        _state.disabled = state;
        this.setState( _state );
    }

    render() {
        let panelHeading = [];

        if (this.props.guiProps.heading != null) {
            panelHeading.push(<Panel.Heading>{this.props.guiProps.heading}</Panel.Heading>)
        }

        let panelFooter = [];
        if (this.props.guiProps.footer != null) {
            panelFooter.push(<Panel.Footer>{this.props.guiProps.footer}</Panel.Footer>)
        }

        let style = "primary";
        if (this.props.guiProps.bsStyle != null) style = this.props.guiProps.bsStyle;

        return <Panel id={this.props.guiProps.id} aria-disabled={this.state.disabled} bsStyle={style}>
            {panelHeading}
            <Panel.Body>
                {this.props.children}
            </Panel.Body>
            {panelFooter}
        </Panel>
    }
}
