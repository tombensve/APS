package se.natusoft.osgi.aps.web.adminweb

/**
 * This class only holds internal constants
 */
interface Constants {

    interface BusEvent {
        String Type = "type"

        String Address = "address"
        interface _Address {
            String PUBLIC = "aps.admin.web.event"
        }

        String Classifier = "classifier"
        interface _Classifier {
            String PUBLIC = "public"
            String PRIVATE = "private"
        }

        String Action = "action"
    }
}

interface Event {

    Map<String, Object> struct = [
            header: [
                    type      : "service",
                    address   : "aps.admin.web",
                    _classifier: "/public|private/"
            ],
            body  : [
                    action: "get-webs"
            ],
            reply: [
                    webs: [
                            [
                                    _empty_: "true",
                                    name: "/.*/",
                                    url: "/^https?://.*/"
                            ]
                    ]
            ]
    ]

    interface type {
        String name = "type"
        enum values {
            service
        }
    }

    interface address {
        String name = "address"
        enum values {
            aps_admin_web
        }
    }

    interface classifier {
        String name = "classifier"
        enum values {
            external,
            local
        }
    }

    interface action {
        String name = "action"
        enum values {
            getWebs
        }
    }

    interface content {
        String name = "content"

    }
}

