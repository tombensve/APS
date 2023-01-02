import APSEventBus from "./APSEventBus"

/**
 * Utility that sends alert messages on the bus to APSAlert components.
 */
export default class APSAlerter {

    /** A map of all alerters by name. */
    static alerters = {};

    /**
     * Returns a named alerter or undefined.
     *
     * @param {string} name The name of the alerter to get.
     *
     * @returns {APSAlerter} The named alerter or undefined if it does not exist.
     */
    static getAlerter(name: string) {
        return APSAlerter.alerters[name];
    }

    /**
     * Creates a new alerter.
     *
     * @param name The name of the alerter to create.
     * @param eventBus The event bus to use by the alerter.
     *
     * @returns {APSAlerter} The created alerter.
     */
    static createAlerter(name: string, eventBus: APSEventBus): APSAlerter {
        let alterer = new APSAlerter(eventBus);
        APSAlerter.alerters[name] = alterer;
        return alterer;
    }

    /**
     * Convenience method that tries getting existing, and creates a new if there is no existing.
     *
     * @param name The name of the alerter to get or create.
     * @param eventBus The event bus to use by the alerter if it needs to be created.
     *
     * @returns {APSAlerter} Old or new alerter.
     */
    static getOrCreateAlerter(name: string, eventBus: APSEventBus): APSAlerter {
        let alerter = APSAlerter.getAlerter(name);
        if (!alerter) {
            alerter = APSAlerter.createAlerter(name, eventBus);
        }
        return alerter;
    }

    /**
     * Creates a new Alerter. Don't call this directly, use the static get or create methods.
     *
     * @param eventBus The event bus to use by the alerter.
     */
    constructor( eventBus: APSEventBus ) {
        this.eventBus = eventBus;
    }

    /**
     * Triggers an alert by sending an alert message on the bus. This will only have an effect if
     * there is an APSAlert component listening on the bus.
     *
     * @param alertId The id of the APSAlert component to send alert message to.
     * @param text The alert text. Can be markdown.
     */
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

    /**
     * Hides a previously opened alert.
     *
     * @param alertId Same id as used with alert(...).
     */
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