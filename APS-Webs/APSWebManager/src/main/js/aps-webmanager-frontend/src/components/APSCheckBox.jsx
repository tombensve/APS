import React from 'react'
import APSComponent from "./APSComponent"
import { Checkbox } from 'react-bootstrap'

/**
 * ## Properties
 *
 * ### guiProps.id
 *
 * The id of the component.
 *
 * ### guiProps.value
 *
 * The initial value of the component.
 *
 * ### guiProps.placeholder
 *
 * A greyed out placeholder for the component.
 *
 */
export default class APSCheckBox extends APSComponent {

    constructor( props: {} ) {
        super( props );


        let checked: boolean = false;

        if (this.props.guiProps.value && this.props.guiProps.value === "checked") {
            checked = true;
        }

        this.defaultValue = checked;

        this.state = {
            disabled: false,
            // Note: The name of the state value has to match the component value name to be a "controlled" component!
            value: checked
        };

        this.empty = false;

    }

    componentType(): string {
        return "aps-check-box";
    }

    set disabled( state: boolean ) {
        let _state = this.state;
        _state.disabled = state;
        this.setState( _state );
    }

    handleEvent( event: {} ) {

        this.setState( {
            disabled: this.state.disabled,
            value: !this.state.value
        } );

        this.message(
            this.changeEvent(
                {
                    componentType: this.componentType(),
                    // Our this.state.value is not yet updated here even though we have
                    // called setState(...).
                    value: !this.state.value
                }
            )
        );
    }

    componentDidMount() {
        this.sendDefaultValueLocalOnly();
    }

    render() {

        if (this.state.value === true) {
            return <Checkbox id={this.props.guiProps.id} onChange={this.handleEvent.bind( this )} checked>
                {' '}{this.props.guiProps.label}
            </Checkbox>;
        }
        else {
            return <Checkbox id={this.props.guiProps.id} onChange={this.handleEvent.bind( this )}>
                {' '}{this.props.guiProps.label}
            </Checkbox>;
        }
    }
}


