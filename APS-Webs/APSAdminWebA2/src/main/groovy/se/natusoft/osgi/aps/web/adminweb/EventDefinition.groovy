package se.natusoft.osgi.aps.web.adminweb

import se.natusoft.osgi.aps.tools.groovy.lib.MapJsonDocVerifier

/**
 * This contains the definition of the events handled.
 */
class EventDefinition {

    private static final Map<String, Object> EVENT_SCHEMA = [
            header_1: [
                    type_1      : "service",
                    address_1   : "aps.admin.web",
                    classifier_1: "?public|private"
            ],
            body_1  : [
                    action_1: "get-webs"
            ],
            reply_0 : [
                    webs_1: [
                            [
                                    name_1: "?.*",
                                    url_1 : "?^https?://.*"
                            ]
                    ]
            ],
            error_0 : [
                    code_1   : "#1-2",
                    message_1: "?.*"
            ]
    ] as Map<String, Object>

    static final MapJsonDocVerifier EVENT_VERIFIER = new MapJsonDocVerifier(validStructure: EVENT_SCHEMA)

    static void validate(Map<String, Object> event) {
        EVENT_VERIFIER.validate(event)
    }

    /**
     * Creates an error.
     * @param error The error to create.
     */
    static Map<String, Object> createError(Map<String, Object> error) {
        createBasicPublicEvent(null, error)
    }

    /**
     * Creates a reply.
     * @param reply The reply to create.
     */
    static Map<String, Object> createReply(Map<String, Object> reply) {
        createBasicPublicEvent(reply, null)
    }

    /**
     * Factory method for creating an event.
     *
     * @param reply Reply data.
     * @param error Error data.
     */
    private static Map<String, Object> createBasicPublicEvent(Map<String, Object> reply, Map<String, Object> error) {
        Map<String, Object> event = [
                header: [
                        type      : "service",
                        address   : "aps.admin.web",
                        classifier: "public"
                ],
                body  : {
                    action: "get-webs"
                }
        ] as Map<String, Object>

        if (reply != null) {
            event["reply"] = reply
        }

        if (error != null) {
            event["error"] = error
        }

        event
    }
}
