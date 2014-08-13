package se.natusoft.osgi.aps;

import junit.framework.TestCase;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigEnvironment;
import se.natusoft.osgi.aps.core.config.model.APSConfigInstanceMemoryStoreImpl;
import se.natusoft.osgi.aps.core.config.model.APSConfigObjectFactory;
import se.natusoft.osgi.aps.core.config.model.ConfigEnvironmentProvider;
import se.natusoft.osgi.aps.core.config.model.admin.APSConfigEditModelImpl;
import se.natusoft.osgi.aps.core.config.model.admin.APSConfigEnvironmentImpl;
import se.natusoft.osgi.aps.hazelcast.config.APSHazelCastConfig;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class ValidateConfig extends TestCase {

    private APSConfigEnvironment testConfigEnvironment = new APSConfigEnvironmentImpl("testenv", "For test.", 0);

    public void testConfig() throws Exception {
        // This code will throw an exception if the config class passed on the last line is bad.
        Properties props = new Properties();
        APSConfigInstanceMemoryStoreImpl configValueStore = new APSConfigInstanceMemoryStoreImpl(props);
        APSConfigObjectFactory configObjectFactory = new APSConfigObjectFactory(new ConfigEnvironmentProvider() {
            @Override
            public APSConfigEnvironment getActiveConfigEnvironment() {
                return testConfigEnvironment;
            }

            @Override
            public List<APSConfigEnvironment> getConfigEnvironments() {
                List<APSConfigEnvironment> envs = new LinkedList<>();
                envs.add(testConfigEnvironment);
                return envs;
            }

            @Override
            public APSConfigEnvironment getConfigEnvironmentByName(String name) {
                return getActiveConfigEnvironment();
            }
        }, configValueStore);

        new APSConfigEditModelImpl(APSHazelCastConfig.class, configObjectFactory);
    }
}
