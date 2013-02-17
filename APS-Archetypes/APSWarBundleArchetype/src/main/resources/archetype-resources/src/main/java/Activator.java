package ${package};

import java.util.Dictionary;
import java.util.Properties;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;
import se.natusoft.osgi.aps.api.core.tools.APSLogger;

public class Activator implements BundleActivator {
    //
    // Private Members
    //
    
    // Required Services
    
    /** The OSGi Log service. */
    private ServiceTracker logServiceTracker = null;
    
    
    // Provided Services
    
    /** The platform service. */
    private ServiceRegistration myServiceReg = null;

    // Other Members
    
    /** Our logger. */
    private APSLogger logger = null;
    
    //
    // Bundle Start.
    //
    
    @Override
    public void start(BundleContext context) throws Exception {
        this.logServiceTracker = new ServiceTracker(context, LogService.class.getName(), null);
        this.logServiceTracker.open();
        this.logger = new APSLogger(this.logServiceTracker);
        
        
        Dictionary platformServiceProps = new Properties();
        platformServiceProps.put(Constants.SERVICE_PID, MyServiceProvider.class.getName());                        
        MyServiceProvider myServiceProvider = new MyServiceProvider();
        this.myServiceReg = context.registerService(MyService.class.getName(), myServiceProvider, platformServiceProps);
    }

    //
    // Bundle Stop.
    //
    
    @Override
    public void stop(BundleContext context) throws Exception {
        this.myServiceReg.unregister();
                
        this.logServiceTracker.close();
        
        this.myServiceReg = null;
        this.logServiceTracker = null;
        this.logger = null;
    }

}
