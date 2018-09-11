import React from 'react'
import APSComponent from "./APSComponent"
import { ControlLabel, FormControl } from 'react-bootstrap'

/**
 * ## props
 *
 * ### guiProps.label
 *
 * A label before the component.
 *
 * ### guiProps.options
 *
 * The options to make available in the select. It has the following format:
 *
 *     [
 *         { value: "value", label: "label" },
 *         ...
 *     ]
 */
export default class APSSelect extends APSComponent {

    constructor(props) {
        super(props);

        this.defaultValue = this.props.guiProps.value;

        this.state = {
            value: this.defaultValue
        };

    }

    componentType(): string {
        return "aps-select";
    }

    handleEvent(event: {}) {

        this.setState({
            value: event.target.value
        });

        this.message(
            this.changeEvent(
                {
                    componentType: this.componentType(),
                    value: event.target.value
                }
            )
        );
    }

    doRender( comps: [] ) {

        if (this.props.guiProps.label) {
            comps.push(<ControlLabel>{this.props.guiProps.label}</ControlLabel>);
        }

        let opts = [];

        for (let opt: {value:string, label:string} of this.props.guiProps.options) {
            if (opt.value === this.state.value) {
                opts.push( <option selected value={opt.value}>{opt.label}</option> );
            }
            else {
                opts.push( <option value={opt.value}>{opt.label}</option> );
            }
        }

        comps.push(
            <FormControl componentClass={"select"} onChange={this.handleEvent.bind( this )}>
                {opts}
            </FormControl>
        );
    }
}