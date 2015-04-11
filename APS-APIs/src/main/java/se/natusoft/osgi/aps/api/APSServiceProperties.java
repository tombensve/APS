package se.natusoft.osgi.aps.api;

import java.util.Properties;

/**
 * These are "properties" for use in service registrations.
 */
public interface APSServiceProperties { // This interface is just a container. Put no functionality directly in it!

    abstract class Security {

        public static final String Key = "aps.props.security";

        public static final String Secure = "secure";
        public static final String NonSecure = "nonsecure";

        public static void setSecure(Properties properties) { properties.setProperty(Key, Secure); }
        public static void setNonsecure(Properties properties) { properties.setProperty(Key, NonSecure); }
        public static String getSecureLookupCriteria() { return "(" + Key + "=" + Secure + ")"; }
        public static String getNonsecureLookupCriteria() { return "(" + Key + "=" + NonSecure + ")"; }
    }

    interface Instance {
        String Name = "aps.svc.props.instance.name";
    }
}
