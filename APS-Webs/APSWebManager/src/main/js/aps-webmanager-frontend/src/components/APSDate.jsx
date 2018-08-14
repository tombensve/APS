import React from 'react'
import APSComponent from './APSComponent'
import DayPickerInput from 'react-day-picker/DayPickerInput'
import 'react-day-picker/lib/style.css'
import APSLogger from '../APSLogger'

export default class APSDate extends APSComponent {

    constructor( props ) {
        super( props );

        this.logger = new APSLogger( "APSDate" );

        this.state = {
            value: props.guiProps.startValue,
            disabled: props.guiProps.disabled != null ? props.guiProps.disabled : false
            // showPopover: true
        };
        this.setState( this.state );

        this.placeHolder = "YYYY-MM-DD";
        if (typeof this.props.guiProps.placeHolder !== "undefined" &&  this.props.guiProps.placeHolder != null) {
            this.placeHolder = this.props.guiProps.placeHolder;
        }
    }

    valueChangedHandler( date ) {
        this.logger.debug( "Selected date: " + date );

        if ( typeof date !== "undefined" ) {
            this.setState( {
                value: date,
                disabled: this.state.disabled
                // showPopover: false
            } );

            this.message(
                this.changeEvent(
                    {
                        componentType: "date",
                        value: date
                    }
                )
            );
        }
    }

    render() {
        return <DayPickerInput
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
        />
    }
}