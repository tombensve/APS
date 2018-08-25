import React, { Component } from 'react'
import { Row } from 'react-bootstrap'

/**
 * ## Properties
 *
 * ### guiProps.orientation
 *
 * horizontal or vertical.
 */
export default class APSLayout extends Component {
    render() {
        let result = null;

        if ( this.props.guiProps.orientation === null || this.props.guiProps.orientation.startsWith( "horiz" ) ) {

            result = (
                    <Row className="form-inline">
                        {this.props.children}
                    </Row>
            )
        }
        else {

            result = (
                <div className="form-group">
                    {this.props.children}
                </div>
            )
        }

        return result
    }
}
