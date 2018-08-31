import React from 'react'
import { Alert } from 'react-bootstrap'
import APSMarkdown from "./APSMarkdown"
import APSComponent from "./APSComponent"

type Message = { aps: { type: string }, content: { targetId: string, markdown: string } }

export default class APSAlert extends APSComponent {

    constructor( props ) {
        super( props );

        this.state = { show: false, value: this.props.guiProps.value };

        if ( !this.props.guiProps.bsType ) {
            this.props.guiProps.bsType = "warning";
        }

        this.subscribe((message : Message) => {
            if (message.aps.type === "aps-alert" && message.content.targetId === this.props.guiProps.id) {
                if ( !message.content.hide === true ) {
                    // this.logger.debug("About to show alert!");
                    this.setState( {
                        show: true,
                        value: message.content.markdown
                    } );
                }
            }
            else {
                // this.logger.debug("About to close alert!");
                this.setState({
                    show: false,
                    value: this.state.value
                });
            }
        });
    }

    componentType() {
        return "aps-alert";
    }

    render() {
        if ( this.state.show ) {

            // noinspection JSUnresolvedVariable
            if ( this.props.guiProps.hideIn ) {
                // noinspection JSUnresolvedVariable
                setTimeout( () => {
                    this.setState( {
                        show: false,
                        value: this.state.value
                    } );
                }, this.props.guiProps.hideIn );
            }

            // Note that APSMarkdown listens on the bus itself and thus makes use of routing headers.
            // Setting incoming to 'none' means it will not subscribe to anything. Since it doesn't send
            // anything we set outgoing to 'none' also.
            return <Alert bsStyle={this.props.guiProps.bsType}>
                <APSMarkdown guiProps={{
                    id: this.props.guiProps.id + "_md",
                    value: this.state.value,
                    headers: {
                        routing: {
                            outgoing: "none",
                            incoming: "none"
                        }
                    }
                }} eventBus={this.props.eventBus} mgrId={this.props.mgrId} origin={this.props.origin}/>
            </Alert>;
        }
        else {
            return <div id="alertPlaceholder"/>;
        }
    }
}