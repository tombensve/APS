import React from 'react'
import APSComponent from "./APSComponent"
import { Panel } from 'react-bootstrap'

/**
 * ## Properties
 *
 * ### guiProps.disabled
 *
 * Disables or enables the component.
 *
 * ### guiProps.heading
 *
 * A heading for the panel. Don't specify to avoid a heading.
 *
 * ### guiProps.footer
 *
 * A footer for the panel. Don't specify to avoid footer.
 *
 * ### guiProps.bsStyle
 *
 * Changes the default bootstrap style of "primary" to the specified style.
 */
export default class APSPanel extends APSComponent {

    constructor( props ) {
        super( props );

        this.state = {
            disabled: props.guiProps.disabled != null ? props.guiProps.disabled : false
        };

        this.hasValue = false;
        this.busMember = false;
    }

    componentType() {
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
