package se.natusoft.osgi.aps.tools.services;

import se.natusoft.osgi.aps.tools.annotation.activator.ExecutorSvc;
import se.natusoft.osgi.aps.tools.annotation.activator.Managed;
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiServiceProvider;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

@SuppressWarnings("ConstantConditions")
@OSGiServiceProvider
public class APSActivatorExecutorServiceTestSvc implements TestService {

    @Managed
    @ExecutorSvc(parallelism = 5, type = ExecutorSvc.ExecutorType.Cached, unConfigurable = true)
    private ExecutorService fixed;

    @Managed
    @ExecutorSvc(parallelism = 15, type = ExecutorSvc.ExecutorType.Scheduled)
    private ScheduledExecutorService sched;


    @Override
    public String getServiceInstanceInfo() {
        String status = "OK";

        if (this.fixed == null || this.sched == null) {
            status = "BAD";
        }

        // Verify that we have a delegate for fixed.
        if (!this.fixed.getClass().getName().equals("java.util.concurrent.Executors$DelegatedExecutorService")) {
            status = this.fixed.getClass().getName();
        }

        // Verify that we have a scheduled executor service for sched.
        if (!this.sched.getClass().getName().equals("java.util.concurrent.ScheduledThreadPoolExecutor")) {
            status = this.sched.getClass().getName();
        }

        return status;
    }
}
