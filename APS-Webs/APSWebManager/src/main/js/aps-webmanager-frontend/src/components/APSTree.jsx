import React from "react";
import { Glyphicon, Button } from "react-bootstrap";
import APSComponent from "./APSComponent"

/**
 * ## Data
 *
 *     {
 *     ^   label: label,
 *     |   id: id
 *     |   type: "leaf"/"branch",
 *     |   open: true/false (only valid if branch),
 *     +-- children: [...]
 *     }
 */
export default class APSTree extends APSComponent {

    constructor( props ) {
        super( props );

        this.node = this.props.node;
        if ( !this.node ) {
            this.node = this.props.guiProps.node;
        }

        let open = this.node.open;
        if ( !open ) {
            this.open = false;
        }
        this.state = {
            open: open
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
        }
        else {
            if ( this.props.onLeafClick ) {
                this.props.onLeafClick( this.node );
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

                rend.push( <div><Glyphicon glyph="glyphicon glyphicon-triangle-bottom"/><Button
                    bsStyle="link"
                    onClick={this.handleEvent.bind( this )}>{this.node.label}</Button></div> );

                for ( let cnode of this.node.children ) {
                    nodeContent.push( <APSTree eventBus={this.props.eventBus} mgrId={this.props.mgrId}
                                               origin={this.props.origin} node={cnode}
                                               guiProps={this.props.guiProps}
                                               child={true}
                                               onLeafClick={this.props.onLeafClick}/> );
                }
                rend.push( <div style={nodeStyle} className={divClass}>{nodeContent}</div> );
            }
            else {

                rend.push( <div><Glyphicon glyph="glyphicon glyphicon-triangle-right"/><Button
                    bsStyle="link"
                    onClick={this.handleEvent.bind( this )}>{this.node.label}</Button></div> );
            }
        }
        else {
            rend.push( <div style={nodeStyle}><Glyphicon glyph="glyphicon glyphicon-leaf"/><Button
                bsStyle="link"
                onClick={this.handleEvent.bind( this )}>{this.node.label}</Button></div> );
        }

        return rend;
    }
}