import React from 'react'
import { ControlLabel, FormGroup } from 'react-bootstrap'
import APSComponent from "./APSComponent";
import APSTextField from "./APSTextField"
import APSTextArea from "./APSTextArea"
import APSNumber from "./APSNumber"
import APSCheckBox from "./APSCheckBox"
import APSDate from "./APSDate"
import APSSelect from "./APSSelect"

/**
 * This is a component that takes a value and some meta data about the value. The meta data is used
 * to determine how to render & edit the value.
 *
 * ## Properties
 *
 * ### guiProps.id
 *
 * The id of the component.
 *
 * ### guiProps.valueSort
 *
 * The sort of value being edited: "text"/"text-block"/"number"/"boolean"/"date"/"enum".
 *
 * ### guiProps.numRange
 *
 * Object containing a 'min' and a 'max' numeric value to set a valid range.
 *
 * ### guiProps.value
 *
 * The value being edited.
 *
 */
export class APSValueEditor extends APSComponent {

    constructor( props: APSProps ) {
        super( props );

        this.state = {
            value: this.props.guiProps.value,
            disabled: props.guiProps.disabled != null ? props.guiProps.disabled : false
        };

        this.hasValue = true;

    }

    componentType(): string {
        return "aps-value-editor";
    }

    render() {
        let toRender = [];
        let mgrId = this.props.mgrId != null ? this.props.mgrId : "";

        if ( this.props.guiProps.label != null ) {
            toRender.push( <ControlLabel>{this.props.guiProps.label}</ControlLabel> )
        }

        let guiProps;

        switch ( this.props.guiProps.valueSort ) {
            case "text":
                guiProps = {
                    id: this.props.guiProps.id,
                    value: this.state.value
                };
                toRender.push(
                    <APSTextField eventBus={this.props.eventBus} guiProps={guiProps} mgrId={mgrId} />
                );
                break;

            case "text-block":
                guiProps = {
                    id: this.props.guiProps.id,
                    value: this.state.value
                };
                toRender.push(
                    <APSTextArea eventBus={this.props.eventBus} guiProps={guiProps} mgrId={mgrId} />
                );
                break;

            case "number":
                guiProps = {
                    id: this.props.guiProps.id,
                    value: this.state.value,
                    min: this.props.guiProps.numRange.min,
                    max: this.props.guiProps.numRange.max
                };
                toRender.push(
                    <APSNumber eventBus={this.props.eventBus} guiProps={guiProps} mgrId={mgrId} />
                );

                break;

            case "boolean":
                guiProps = {
                    id: this.props.guiProps.id,
                    value: this.state.value
                };
                toRender.push(
                    <APSCheckBox eventBus={this.props.eventBus} guiProps={guiProps} mgrId={mgrId} />
                );
                break;

            case "date":
                guiProps = {
                    id: this.props.guiProps.id,
                    value: this.state.value
                };
                toRender.push(
                    <APSDate eventBus={this.props.eventBus} guiProps={guiProps} mgrId={mgrId} />
                );
                break;

            case "enum":
                guiProps = {
                    id: this.props.guiProps.id,
                    value: this.state.value,
                    options: this.props.guiProps.options
                };
                toRender.push(
                    <APSSelect eventBus={this.props.eventBus} guiProps={guiProps} mgrId={mgrId} />
                );
        }

        return <FormGroup>{toRender}</FormGroup>;
    }
}
