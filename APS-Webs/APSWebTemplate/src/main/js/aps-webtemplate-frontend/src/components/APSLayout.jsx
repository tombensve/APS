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

        if ( this.props.guiProps.border === true) {
            borderStyle =
                { border: this.props.guiProps.borderStyle !== null ? this.props.guiProps.borderStyle : "1px solid black" };
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
