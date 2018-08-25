import React from 'react'
import { ControlLabel, FormControl, FormGroup } from 'react-bootstrap'
import APSComponent from "./APSComponent";
import APSTextField from "./APSTextField"
import APSTextArea from "./APSTextArea"
import APSNumber from "./APSNumber"

/**
 * This is a component that takes a value and some meta data about the value. The meta data is used
 * to determine how to render & edit the value.
 *
 * ## Properties
 *
 * ### guiProps.value
 *
 * A value of types: string, number, boolean, date
 *
 * ### guiProps.meta
 *
 * An object looking like this:
 *
 *     {
 *         type: "text"/"text-block"/"number"/"boolean"/"date",
 *         validValues: [ {id:n, value: value}, ...],
 *         selectable: "one-checkbox"/"one-dropdown"/"many-checkbox",
 *         label: "text",
 *     }
 *
 */
export class APSValueEditor extends APSComponent {

    constructor( props ) {
        super( props );

        this.state = {
            value: "",
            meta: {},
            disabled: props.guiProps.disabled != null ? props.guiProps.disabled : false
        };
        this.setState( this.state );

        this.hasValue = true;
    }

    componentType() {
        return "aps-value-editor";
    }

    render() {
        let toRender = [];
        let mgrId = this.props.mgrId != null ? this.props.mgrId : "";

        if ( this.props.guiProps.meta.label != null ) {
            toRender.push( <ControlLabel>{this.props.guiProps.meta.label}</ControlLabel> )
        }

        switch ( this.props.guiProps.meta.type ) {
            case "text":
                toRender.push(
                    <APSTextField eventBus={this.props.eventBus} guiProps={this.props.guiProps} mgrId={mgrId} />
                );
                break;

            case "text-block":
                toRender.push(
                    <APSTextArea eventBus={this.props.eventBus} guiProps={this.props.guiProps} mgrId={mgrId} />
                );
                break;

            case "number":
                toRender.push(
                    <APSNumber eventBus={this.props.eventBus} guiProps={this.props.guiProps} mgrId={mgrId} />
                );

                break;

            case "boolean":

                break;

            case "date":


        }

        return <FormGroup>{toRender}</FormGroup>;
    }

}