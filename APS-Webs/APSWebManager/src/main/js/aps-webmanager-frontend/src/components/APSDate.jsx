// React
import React from 'react'
// External
import DayPickerInput from 'react-day-picker/DayPickerInput'
import 'react-day-picker/lib/style.css'
// Utils
import { isMobileDevice } from "../Utils";
// Components
import APSComponent from './APSComponent'
import { ControlLabel } from "react-bootstrap";

/**
 * Date component based on DayPickerInput: http://react-day-picker.js.org/api/DayPickerInput/.
 *
 * I tried several date pickers that did not work! DayPickerInput works perfectly and was
 * possible to adapt to Bootstrap style due to a well thought through API.
 */
export default class APSDate extends APSComponent {

    constructor( props: { guiProps: { startValue: *, disabled: boolean } } ) {
        super( props );

        this.defaultValue = props.guiProps.startValue ? props.guiProps.startValue : "";

        this.state = {
            value: this.defaultValue,
            disabled: props.guiProps.disabled != null ? props.guiProps.disabled : false
        };
        this.setState( this.state );

        this.placeHolder = "YYYY-MM-DD";
        if ( typeof this.props.guiProps.placeHolder !== "undefined" && this.props.guiProps.placeHolder != null ) {
            this.placeHolder = this.props.guiProps.placeHolder;
        }

        this.empty = true;
        this.hasValue = true;
    }

    componentType(): string {
        return "aps-date";
    }

    valueChangedHandlerComputer( date: string ) {

        if ( date ) {
            this.empty = false;
            this.setState( {
                value: date,
                disabled: this.state.disabled
                // showPopover: false
            } );

            this.message(
                this.changeEvent(
                    {
                        componentType: this.componentType(),
                        value: date
                    }
                )
            );
        }
        else {
            this.empty = true;
        }
    }

    valueChangedHandlerMobile( event: {} ) {

        if ( event ) {

            this.empty = false;
            this.setState( {
                value: event.target.value,
                disabled: this.state.disabled
                // showPopover: false
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
        else {
            this.empty = true;
        }
    }

    doRender( comps ) {
        if ( this.props.guiProps.label ) {
            comps.push( <ControlLabel>{this.props.guiProps.label}</ControlLabel> );
        }

        if ( isMobileDevice().any() ) {
            comps.push( <input id={this.props.guiProps.id} type={"date"} className={"form-control"}
                               placeholder={"YYYY-MM-DD"}
                               onChange={this.valueChangedHandlerMobile.bind( this )}/> );
        }
        else {
            comps.push( <DayPickerInput
                id={this.props.guiProps.id}
                classNames={{
                    container: 'form-group',
                    overlayWrapper: 'DayPickerInput-OverlayWrapper',
                    overlay: 'DayPickerInput-Overlay'
                }}
                dayPickerProps={{
                    showWeekNumbers: true,
                    todayButton: 'Today',
                }}

                placeholder={this.placeHolder}

                // This is needed to play nice with the Bootstrap L&F.
                component={props => <input className="form-control" {...props} />}

                onDayChange={this.valueChangedHandlerComputer.bind( this )}

                disabled={this.state.disabled}
            /> );
        }
    }
}