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
