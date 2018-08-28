import APSTextField from "./APSTextField"

/**
 * # Component
 *
 * A simple numeric input. Takes an optional min and a max. Floating point numbers and integers allowed.
 *
 * There is no GUI fanciness here. Just an input field. However '<' will decrease value by 1 and '>' will
 * increase value by one.
 *
 * ## Properties
 *
 * ### guiProps.min
 *
 * The minimum value.
 *
 * ### guiProps.max
 *
 * The max value.
 *
 * ### guiProps.value
 *
 * The starting value.
 */
export default class APSNumber extends APSTextField {

    constructor( props: {} ) {
        super( props );

        this.props.guiProps.placeholder = "0";

        this.state = {
            value: this.props.guiProps.value,
            disabled: props.guiProps.disabled != null ? props.guiProps.disabled : false,
        };
        this.setState( this.state );

        this.min = Number( this.props.guiProps.min );
        this.max = Number( this.props.guiProps.max );
        this.empty = false;
        this.hasValue = true;
    }

    componentType(): string {
        return "aps-number";
    }

    /**
     * Event handler
     *
     * @param event
     */
    handleEvent( event: {} ) {

        let value: string = event.target.value;

        // Exclude any characters not part of a number.
        let iterator = value[Symbol.iterator]();
        let currChar = iterator.next();
        let filteredValue = "";
        let first = true;

        while ( !currChar.done && "0123456789.,-".indexOf( currChar.value ) >= 0 ) {

            if ( currChar === '-') {

                if (first) {

                    filteredValue = filteredValue + currChar.value;
                }
            }
            else {

                filteredValue = filteredValue + currChar.value;
            }

            currChar = iterator.next();
            first = false;
        }

        // Convert string to number and validate min and max.
        let validationNumber = null;
        if ( filteredValue.indexOf( '.' ) >= 0 || filteredValue.indexOf( ',' ) >= 0 ) {
            validationNumber = Number.parseFloat( filteredValue );
        }
        else {
            validationNumber = Number.parseInt( filteredValue, 10 );
        }

        if ( !Number.isNaN( validationNumber ) ) {

            // Allows > to increase number and < to decrease number.
            if (value.endsWith(">")) {
                validationNumber = validationNumber + 1;
                filteredValue = validationNumber.toString(10);
            }
            else if (value.endsWith("<")) {
                validationNumber = validationNumber - 1;
                filteredValue = validationNumber.toString(10);
            }

            // Limit value between max and min if provided.
            if ( !Number.isNaN(this.min) && validationNumber < this.min ) {
                filteredValue = "" + this.min;
            }

            if ( !Number.isNaN(this.max) && (validationNumber > this.max) ) {
                filteredValue = "" + this.max;
            }
        }

        // Only update value and send event if value actually changed after filtering.
        if (this.state.value !== filteredValue) {
            this.setState( {
                value: filteredValue,
                disabled: this.state.disabled
            } );


            this.message(
                this.changeEvent(
                    {
                        componentType: this.componentType(),
                        value: filteredValue
                    }
                )
            );
        }
    }
}