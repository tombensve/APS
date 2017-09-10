package se.natusoft.osgi.aps.web.adminweb

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

/**
 * This defines message value names.
 */
@CompileStatic
@TypeChecked
interface Constants {

    static final String APP_NAME = "aps-admin-web-a2"
    //static final String NODE_ADDRESS = APP_NAME + "." + InetAddress.localHost.hostName
    static final String NODE_ADDRESS = "APSAdminWeb"

    //
    // Values
    //

    static final String GLOBAL_BUS_ADDRESS = "aps.admin.web"
    static final String LOCAL_BUS_ADDRESS = "aps.admin.web"

    static final String TYPE_SERVICE = "service"

    static final String CLASSIFIER_PUBLIC = "public"
    static final String CLASSIFIER_PRIVATE = "private"

    static final String EVENT_ID_CLIENT_AVAILABLE = "clientAvailable"
}
