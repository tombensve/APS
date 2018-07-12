import React, { Component } from 'react'
import './APSLayout.css'

class APSLayout extends Component {
    render() {
        let result = null;

        // noinspection JSUnresolvedVariable
        if (this.props.orientation === null || this.props.orientation.startsWith( "horiz" ) ) {

            result = (
                <div className="layout-horiz">
                    {this.props.children}
                </div>
            )
        }
        else {

            result = (
                <div className="layout-vert">
                    {this.props.children}
                </div>
            )
        }

        return result
    }
}

// noinspection JSUnusedGlobalSymbols
export default APSLayout
