import React from 'react'
import APSComponent from "./APSComponent"
import { Radio } from 'react-bootstrap'
import uuid from "../APSUUID"
import { apsObject } from "../Utils"

/**
 * ## Properties
 *
 * ### guiProps.id
 *
 * The id of the component.
 *
 * ### guiProps.value
 *
 * The starting value.
 *
 * ### gioProps.radios
 *
 * [
 *   { id:"id", label:"label" },
 *   ...
 * ]
 *
 * The value will be the id of the selected radio button.
 */
export default class APSRadioSet extends APSComponent {

    constructor( props: {} ) {
        super( props );

        this.radioGroup = uuid();

        this.defaultValue = this.props.guiProps.value;

        this.state = {
            disabled: false,
            value: this.defaultValue
        };

        this.empty = false;

    }

    componentType(): string {
        return "aps-radio-set";
    }

    set disabled( state: boolean ) {
        let _state = this.state;
        _state.disabled = state;
        this.setState( _state );
    }

    handleEvent( event: {} ) {
        event = apsObject( event );

        this.setState( {
            disabled: this.state.disabled,
            value: event.target.value
        } );

        this.message(
            this.changeEvent(
                {
                    componentType: this.componentType(),
                    value: event.target.value
                }
            )
        );
    }

    componentDidMount() {
    }

    render() {

        let comps = [];

        for ( let comp of this.props.guiProps.radios ) {

            if ( comp.id === this.state.value ) {
                comps.push(
                    <Radio value={comp.id} name={this.radioGroup} onChange={this.handleEvent.bind( this )} checked inline>
                        {comp.label}
                    </Radio>
                );

            }
            else {
                comps.push(
                    <Radio value={comp.id} name={this.radioGroup} onChange={this.handleEvent.bind( this )} inline>
                        {comp.label}
                    </Radio>
                );
            }
        }

        return comps;
    }
}


