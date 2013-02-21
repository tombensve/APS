package ${package};

import java.util.Dictionary;
import java.util.Properties;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogService;
import se.natusoft.osgi.aps.tools.APSServiceTracker;
import se.natusoft.osgi.aps.tools.APSLogger;

public class Activator implements BundleActivator {
    //
    // Private Members
    //
    
    // Required Services
    
    /** The  service. */
    private APSServiceTracker<Svc> svcServiceTracker = null;
    
    
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
        this.logger = new APSLogger(System.out);
        this.logger.start(context);
        
        this.svcServiceTracker = new APSServiceTracker<Svc>(
                context,
                Svc.class,
                APSServiceTracker.LARGE_TIMEOUT
        );
        this.svcServiceTracker.start();
        
        Dictionary platformServiceProps = new Properties();
        platformServiceProps.put(Constants.SERVICE_PID, MyServiceProvider.class.getName());                        
        MyServiceProvider myServiceProvider = new MyServiceProvider(this.logger);
        this.myServiceReg = context.registerService(MyService.class.getName(), myServiceProvider, platformServiceProps);
    }

    //
    // Bundle Stop.
    //
    
    @Override
    public void stop(BundleContext context) throws Exception {
        if (this.myServiceReg != null) {
            try {
                this.myServiceReg.unregister();
                this.myServiceReg = null;
            }
            catch (IllegalStateException ise) { /* This is OK! */ }
        }

        if (this.logger != null) {
            this.logger.stop(context);
            this.logger = null;
        }
    }

}
