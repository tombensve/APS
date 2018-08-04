import React, { Component } from 'react'
import './APSLayout.css'

class APSLayout extends Component {
    render() {
        let result = null;

        // noinspection JSUnresolvedVariable
        if ( this.props.orientation === null || this.props.orientation.startsWith( "horiz" ) ) {

            result = (
                <div className="form-inline">
                    <div className="form-group">
                        {this.props.children}
                    </div>
                </div>
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

// noinspection JSUnusedGlobalSymbols
export default APSLayout
