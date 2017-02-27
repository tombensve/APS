package se.natusoft.osgi.aps.web.adminweb

/**
 * This class only holds internal constants
 */
interface Constants {

    interface EVENT {
        String ADDRESS = "aps.admin.web"
        String ACTION = "action"

        interface TYPE {
            String SERVER = "service"
        }

        interface CLASSIFIER {
            String PUBLIC = "public"
            String PRIVATE = "private"
        }

        interface FIELD {
            String BODY = "body"
            String ADDRESS = "address"
            String CLASSIFIER = "classifier"
            String ACTION = "action"
        }

        interface ACTION {
            String GET_WEBS = "get-webs"
        }
    }
}

