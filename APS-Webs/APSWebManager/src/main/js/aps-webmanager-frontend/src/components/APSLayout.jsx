import React, { Component } from 'react'
import Box from 'react-layout-components'

/**
 * ## Properties
 *
 * ### guiProps.orientation
 *
 * horizontal or vertical.
 *
 */
export default class APSLayout extends Component {

    render() {
        let result = null;

        if ( this.props.guiProps.orientation === null || this.props.guiProps.orientation.startsWith( "horiz" ) ) {

            result = (
                <Box alignContent="flex-start" alignItems="space-between">
                    {this.props.children}
                </Box>
            )
        }
        else {

            result = (
                <Box alignContent="flex-start" alignItems="space-between" column>
                    {this.props.children}
                </Box>
            )
        }

        return result
    }
}
