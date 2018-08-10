import React, { Component } from 'react'
import { Row } from 'react-bootstrap'

export default class APSLayout extends Component {
    render() {
        let result = null;

        if ( this.props.guiProps.layout.orientation === null || this.props.guiProps.layout.orientation.startsWith( "horiz" ) ) {

            result = (
                    <Row id={this.props.id} className="form-inline">
                        {this.props.children}
                    </Row>
            )
        }
        else {

            result = (
                <div id={this.props.id} className="form-group">
                    {this.props.children}
                </div>
            )
        }

        return result
    }
}
