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

    //
    // Message keys
    //

    static final String _body_ = "body"
    static final String _address_ = "address"
    static final String _classifier_ = "classifier"
    static final String _action_ = "action"
    static final String _reply_ = "reply"
    static final String _error_ = "error"

    //
    // Values
    //

    static final String GLOBAL_BUS_ADDRESS = "aps.adminweb"
    static final String LOCAL_BUS_ADDRESS = "adminweb.general"

    static final String TYPE_SERVICE = "service"

    static final String CLASSIFIER_PUBLIC = "public"
    static final String CLASSIFIER_PRIVATE = "private"

    static final String ACTION_GET_WEBS = "get-webs"
}
