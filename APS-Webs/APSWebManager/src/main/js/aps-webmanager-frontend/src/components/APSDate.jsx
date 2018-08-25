import React from 'react'
import APSComponent from './APSComponent'
import DayPickerInput from 'react-day-picker/DayPickerInput'
import 'react-day-picker/lib/style.css'
import APSLogger from '../APSLogger'
import { isMobileDevice } from "../Utils";

/**
 * Date component based on DayPickerInput: http://react-day-picker.js.org/api/DayPickerInput/.
 *
 * I tried several date pickers that did not work! DayPickerInput works perfectly and was
 * possible to adapt to Bootstrap style due to a well thought through API.
 */
export default class APSDate extends APSComponent {

    constructor( props: { guiProps: { startValue: *, disabled: boolean } } ) {
        super( props );

        this.logger = new APSLogger( "APSDate" );

        this.state = {
            value: props.guiProps.startValue,
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

    valueChangedHandler( date: string ) {
        this.logger.debug( "Selected date: " + date );

        if ( typeof date !== "undefined" && date !== null ) {
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

    render() {
        if ( isMobileDevice().any() ) {
            return <input id={this.props.guiProps.id} type={"date"} className={"form-control"} placeholder={"YYYY-MM-DD"}
                          onChange={this.valueChangedHandler.bind( this )}/>
        }
        else {
            return <DayPickerInput
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

                onDayChange={this.valueChangedHandler.bind( this )}

                disabled={this.state.disabled}
            />
        }
    }
}