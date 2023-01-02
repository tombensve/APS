import React from 'react'
import ReactMarkdown from 'react-markdown'
import APSComponent from "./APSComponent"

type Message = { aps: { type: string }, content: { targetId: string, markdown: string } }

export default class APSMarkdown extends APSComponent {

    constructor( props ) {
        super( props );

        this.subscribe( ( message: Message ) => {
            if ( message.aps.type === "md-text" && message.content.targetId === this.props.guiProps.id ) {
                this.setState( {
                    value: message.content.markdown
                } );
            }
        } );

        this.state = {
            value: this.props.guiProps.value
        };

        this.setState(this.state);
    }

    render() {
        let value = this.state.value;
        if ( typeof value === "string" ) {
            value = value.replace( "§", "\n\n" );
        }
        return <ReactMarkdown>
            {value}
        </ReactMarkdown>
    }
}