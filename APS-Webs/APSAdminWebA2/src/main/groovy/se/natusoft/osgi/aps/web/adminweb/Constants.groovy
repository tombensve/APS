package se.natusoft.osgi.aps.web.adminweb

/**
 * This defines message value names.
 */
interface Constants {

    //
    // Message keys
    //

    String _body_ = "body"
    String _address_ = "address"
    String _classifier_ = "classifier"
    String _action_ = "action"
    String _reply_ = "reply"
    String _error_ = "error"

    //
    // Values
    //

    String BUS_ADDRESS = "aps.admin.web"

    String TYPE_SERVICE = "service"

    String CLASSIFIER_PUBLIC = "public"
    String CLASSIFIER_PRIVATE = "private"

    String ACTION_GET_WEBS = "get-webs"
}
