package se.natusoft.osgi.aps.tools.services;

import se.natusoft.osgi.aps.tools.APSLogger;
import se.natusoft.osgi.aps.tools.annotation.activator.Managed;
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiServiceProvider;

/**
* Created by tommy on 2015-01-03.
*/
@OSGiServiceProvider
public class OSGiService implements TestService {

    @Managed
    private APSLogger logger;

    public APSLogger getLogger() {
        return this.logger;
    }

    @Override
    public String getServiceInstanceInfo() {
        return "This is a test called OSGiService!";
    }
}
