import React, { Component } from 'react'
import Box from 'react-layout-components'
import APSLogger from "../APSLogger"

/**
 * ## Properties
 *
 * ### guiProps.orientation
 *
 * horizontal or vertical.
 *
 */
export default class APSLayout extends Component {

    constructor( props: {} ) {
        super( props );

        this.logger = new APSLogger( "APSLayout" );
    }

    render() {

        let borderStyle = {};

        // This works!
        // let borderStyle = { border: "1px solid black" };

        // In this case this.props.guiProps.borderStyle do contain "1px solid black", but this fails!
        // The style is not applied!
        // let borderStyle = { border: this.props.guiProps.borderStyle };

        // For now I only support border or no border.
        if ( this.props.guiProps.border === true) {
            borderStyle = { border: "1px solid black" };
        }

        let result = null;

        if ( this.props.guiProps.orientation === null || this.props.guiProps.orientation.startsWith( "horiz" ) ) {

            result = (
                <Box alignContent="flex-start" alignItems="space-between" style={borderStyle}>
                    {this.props.children}
                </Box>
            )
        } else {

            result = (
                <Box alignContent="flex-start" alignItems="space-between" style={borderStyle} column>
                    {this.props.children}
                </Box>
            )
        }

        return result
    }
}
