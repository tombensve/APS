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

    interface Service {
        String Provider = SERVICE_PROVIDER;
        String Category = SERVICE_CATEGORY;
        String Function = SERVICE_FUNCTION;

        interface Production {
            String Ready = SERVICE_PRODUCTION_READY;
        }
    }

    interface Uses {
        String Network = "uses.network";
        String Discovery = "uses.discovery";
    }

    interface Provides {
        String Discovery = "provides.discovery";
    }

    interface Messaging {
        String SERVICE_CATEGORY = "network";
        String SERVICE_FUNCTION = "messaging";

        String PROVIDER = "messaging-provider";
        String PERSISTENT = "messaging-persistent";
        String MULTIPLE_RECEIVERS = "messaging-multiple-receivers";

    }

    interface Value {
        interface Service {
            interface Category {
                String Network = "network";
                String Web = "web";
                String Storage = "storage";
                String Security = "security";
                String Authentication = "authentication";
                String Transform = "transform";
                String DataFormats = "data.formats";
            }

            interface Function {
                String Discovery = "discovery";
                String JSON = "json";
                String RemoteService = "remote-service";
                String Configuration = "configuration";
                String Database = "database";
                String Filesystem = "filesystem";
            }
        }
    }
}
