package se.natusoft.osgi.aps.tools.services;

import se.natusoft.osgi.aps.tools.annotation.activator.OSGiServiceProvider;
import se.natusoft.osgi.aps.tools.apis.APSActivatorServiceSetupProvider;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 *
 */
@OSGiServiceProvider(serviceSetupProvider = ServicesSetupProviderTestService.SetupProvider.class)
public class ServicesSetupProviderTestService implements TestService {

    private String instName;

    public ServicesSetupProviderTestService() {}

    public ServicesSetupProviderTestService(String instName) {
        this.instName = instName;
    }

    @Override
    public String getServiceInstanceInfo() {
        return this.instName;
    }

    public static class SetupProvider implements APSActivatorServiceSetupProvider {

        /**
         * Provides setup for each instance to create and register.
         * <p/>
         * Each returned Setup instance will result in one registered service instance.
         * <p/>
         * Each Properties instance should contain a common property with different values
         * that can be searched for during service lookup/tracking.
         */
        @Override
        public List<Setup> provideServiceInstancesSetup() {
            List<Setup> setups = new LinkedList<>();

            Setup setup = new Setup();
            setup.serviceAPIs.add("e.natusoft.osgi.aps.tools.services.TestService");
            setup.props = new Properties();
            setup.props.setProperty("instance", "first");
            setup.serviceInstance = new ServicesSetupProviderTestService("first");
            setups.add(setup);

            setup = new Setup();
            setup.serviceAPIs.add("e.natusoft.osgi.aps.tools.services.TestService");
            setup.props = new Properties();
            setup.props.setProperty("instance", "second");
            setup.serviceInstance = new ServicesSetupProviderTestService("second");
            setups.add(setup);

            setup = new Setup();
            setup.serviceAPIs.add("e.natusoft.osgi.aps.tools.services.TestService");
            setup.props = new Properties();
            setup.props.setProperty("instance", "third");
            setup.serviceInstance = new ServicesSetupProviderTestService("third");
            setups.add(setup);

            return setups;
        }
    }
}
