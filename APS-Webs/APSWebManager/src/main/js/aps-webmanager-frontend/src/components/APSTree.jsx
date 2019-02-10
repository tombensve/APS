import React from "react";
import { Glyphicon } from "react-bootstrap";
import APSComponent from "./APSComponent"

/**
 * ## Data
 *
 *  +--------------------------------------------------+
 *  |                                                  |
 *  +->{                                               |
 *         label: label,                               |
 *         id: id                                      |
 *         type: "leaf"/"branch",                      |
 *         open: true/false (only valid if branch),    |
 *         children: [...]                             |
 *     }               |                               |
 *                     +-------------------------------+
 *
 * Leaf:s will trigger events containing the actual node via on LeafClick().
 */
export default class APSTree extends APSComponent {

    constructor( props ) {
        super( props );

        this.node = this.props.node ? this.props.node : this.props.guiProps.node;

        this.state = {
            open: this.node.open ? this.node.open : false
        };
    }

    // Override
    componentType(): string {

        return "aps-tree";
    }

    handleEvent( event: {} ) {
        if ( this.node.type === "branch" ) {

            this.setState( {
                open: !this.state.open
            } );
        } else {

            this.logger.debug( `Node clicked: ${JSON.stringify( this.node )}` );

            if ( this.props.onLeafClick ) {

                this.props.onLeafClick( this.node );
            } else {
                this.message(
                    this.actionEvent( {

                        componentType: this.componentType(),
                        node: this.node
                    }, "aps-tree-node-selected" )
                );
            }
        }
    }

    render() {
        let rend = [];

        let nodeStyle = {
            marginLeft: 20
        };

        let divClass = this.props.child ? "" : "formGroup";

        if ( this.node.type === "branch" ) {

            if ( this.state.open ) {

                let nodeContent = [];

                rend.push(
                    <div>
                        <Glyphicon glyph="glyphicon glyphicon-triangle-bottom" onClick={this.handleEvent.bind( this )}/>
                        <span onClick={this.handleEvent.bind( this )}>{' '}{this.node.label}</span>
                    </div>
                );

                for ( let cnode of this.node.children ) {
                    nodeContent.push( <APSTree eventBus={this.props.eventBus} mgrId={this.props.mgrId}
                                               origin={this.props.origin} node={cnode}
                                               guiProps={this.props.guiProps}
                                               child={true} // This will avoid a new 'formGroup'.
                                               onLeafClick={this.props.onLeafClick}/> );
                }
                rend.push( <div style={nodeStyle} className={divClass}>{nodeContent}</div> );
            } else {

                rend.push(
                    <div>
                        <Glyphicon glyph="glyphicon glyphicon-triangle-right" onClick={this.handleEvent.bind( this )}/>
                        <span onClick={this.handleEvent.bind( this )}>{' '}{this.node.label}</span>
                    </div>
                );
            }
        } else {
            rend.push(
                <div>
                    <Glyphicon glyph="glyphicon glyphicon-leaf" onClick={this.handleEvent.bind( this )}/>
                    <span onClick={this.handleEvent.bind( this )}>{' '}{this.node.label}</span>
                </div> );
        }

        return rend;
    }
}