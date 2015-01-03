package se.natusoft.osgi.aps.tools.apis;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * Provides dynamic setup of each service instance to create.
 */
public interface APSActivatorServiceSetupProvider {

    static class Setup {
        /** The service APIs implemented by the service. Can be null in which case the first implemented interface will be used. */
        public List<String> serviceAPIs = new LinkedList<>();

        /** The properties for a registered service instance. This can be empty, but that defeats the purpose of using this. */
        public Properties props = new Properties();

        /** The instance of the service. */
        public Object serviceInstance;
    }

    /**
     * Provides setup for each instance to create and register.
     *
     * Each returned Setup instance will result in one registered service instance.
     *
     * Each Properties instance should contain a common property with different values
     * that can be searched for during service lookup/tracking.
     */
    List<Setup> provideServiceInstancesSetup();

}
