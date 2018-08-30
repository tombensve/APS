import APSEventBus from "./APSEventBus"

export default class APSAlerter {

    constructor( eventBus: APSEventBus ) {
        this.eventBus = eventBus;
    }

    alert( alertId, text ) {
        let alertText = null;

        if ( Array.isArray( text ) ) {
            alertText = "";
            for ( let part of text ) {
                alertText += part;
            }
        }
        else {
            alertText = text;
        }

        this.eventBus.message(
            {
                headers: {
                    routing: {
                        incoming: "none",
                        outgoing: "client"
                    }
                },
                message: {
                    aps: {
                        type: "aps-alert"
                    },
                    content: {
                        targetId: alertId,
                        markdown: alertText
                    }
                }
            }
        );
    }

    hide( alertId ) {
        this.eventBus.message(
            {
                headers: {
                    routing: {
                        incoming: "none",
                        outgoing: "client"
                    }
                },
                message: {
                    aps: {
                        type: "aps-alert"
                    },
                    content: {
                        targetId: alertId,
                        hide: true
                    }
                }
            }
        );
    }
}