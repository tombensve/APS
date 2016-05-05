package se.natusoft.osgi.aps.constants;

/**
 * Hierarchy of constants.
 */
public interface APS {


    String SERVICE_PROVIDER = "service-provider";

    String SERVICE_CATEGORY = "service-category";

    String SERVICE_FUNCTION = "service-function";

    String SERVICE_PRODUCTION_READY = "service-production-ready";

    String TRUE = "true";
    String FALSE = "false";

    interface Messaging {
        String SERVICE_CATEGORY = "network";
        String SERVICE_FUNCTION = "messaging";

        String PROVIDER = "messaging-provider";
        String PERSISTENT = "messaging-persistent";
        String MULTIPLE_RECEIVERS = "messaging-multiple-receivers";

    }
}
