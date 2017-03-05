package se.natusoft.osgi.aps.web.adminweb

import se.natusoft.osgi.aps.tools.groovy.lib.MapsonDocValidator

/**
 * This contains the definition of the events handled.
 */
class EventDefinition {

    /**
     * A schema defining valid event structure.
     */
    private static final Map<String, Object> EVENT_SCHEMA = [
            header_1: [
                    type_1      : "service",
                    address_1   : "aps.admin.web",
                    classifier_1: "?public|private"
            ],
            body_0  : [
                    action_1: "get-webs"
            ],
            reply_0 : [
                    webs_1: [
                            [
                                    name_1: "?[A-Z,a-z,0-9, ,-]*",
                                    url_1 : "?^https?://.*:?/.*"
                            ]
                    ]
            ],
            error_0 : [
                    code_1   : "#1-2",
                    message_1: "?.*"
            ]
    ] as Map<String, Object>

    /**
     * A validator instance to use for event validations. Note that it is not private! The validate(...) method on this
     * class is a convenience for calling EVENT_VALIDATOR.validate(...).
     */
    static final MapsonDocValidator EVENT_VALIDATOR = new MapsonDocValidator(validStructure: EVENT_SCHEMA)

    /**
     * Validates the specified Map structure.
     *
     * @param event The event Map structure to validate.
     */
    static void validate(Map<String, Object> event) {
        EVENT_VALIDATOR.validate(event)
    }

    /**
     * Creates an error.
     * @param error The error to create.
     */
    static Map<String, Object> createError(Map<String, Object> error) {
        createBasicPublicEvent(null, error)
    }

    /**
     * Creates an error.
     *
     * @param event The original event if any or null.
     * @param error The error to create.
     *
     * @return A new or updated event.
     */
    static Map<String, Object> createError(Map<String, Object> event, Map<String, Object> error) {
        if (event == null) {
            event = [
                    header: [
                            type: "error",
                            address: "aps.admin.web",
                            classifier: "public"
                    ]
            ] as Map<String, Object>
        }

        event['error'] = error
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
