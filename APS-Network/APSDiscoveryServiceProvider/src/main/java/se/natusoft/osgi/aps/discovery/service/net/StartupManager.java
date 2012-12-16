package se.natusoft.osgi.aps.discovery.service.net;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import se.natusoft.osgi.aps.api.core.config.event.APSConfigChangedEvent;
import se.natusoft.osgi.aps.api.core.config.event.APSConfigChangedListener;
import se.natusoft.osgi.aps.api.core.platform.model.PlatformDescription;
import se.natusoft.osgi.aps.api.core.platform.service.APSPlatformService;
import se.natusoft.osgi.aps.api.net.discovery.service.APSSimpleDiscoveryService;
import se.natusoft.osgi.aps.discovery.config.APSDiscoveryServiceConfig;
import se.natusoft.osgi.aps.discovery.model.ServiceDescriptions;
import se.natusoft.osgi.aps.discovery.service.provider.APSSimpleDiscoveryServiceProvider;
import se.natusoft.osgi.aps.exceptions.APSException;
import se.natusoft.osgi.aps.tools.APSLogger;
import se.natusoft.osgi.aps.tools.APSServiceTracker;
import se.natusoft.osgi.aps.tools.tracker.OnServiceAvailable;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * This manages the different threads of this bundle. It also acts on configuration changes.
 */
public class StartupManager extends Thread implements APSConfigChangedListener {
    //
    // Private Members
    //

    /** The discovery service registration. */
    private ServiceRegistration simpleDiscoveryService = null;

    /** Storage of locally published services. */
    private ServiceDescriptions locallyPublishedServices = null;

    /** Storage of remotely published services. */
    private ServiceDescriptions remotelyPublishedServices = null;

    /** A tracker for the platform service. */
    private APSServiceTracker<APSPlatformService> platformServiceTracker = null;

    /** The service providing instance. */
    APSSimpleDiscoveryServiceProvider simpleDiscoveryServiceProvider = null;

    /** Our discoveryServiceLogger. */
    private APSLogger discoveryServiceLogger = null;

    /** Listens to multicast announcements of services from other instances on the local net. */
    private APSMulticastDiscoveryListenerThread multicastDiscoveryListenerThread = null;

    /** Listens to udp announcements of services from other instances on the local net. */
    private APSUDPDiscoveryListenerThread udpDiscoveryListenerThread = null;

    /** This thread sporadically refreshes other remote instances about its local services. */
    private APSAutoRefreshRemoteInstancesThread autoRefreshRemoteInstancesThread = null;

    /** Sends multicast announcements of services on the local net. */
    private APSMulticastDiscoveryAnnouncer multicastDiscoveryAnnouncer = null;

    /** Sends udp anouncements of services to specific udp discovery listener in other instance on the local net. */
    private Map<String, APSUDPDiscoveryAnnouncer> udpDiscoveryAnnouncers = new HashMap<String, APSUDPDiscoveryAnnouncer>();

    /** The bundle context. */
    private BundleContext context = null;

    /** The description of the platform. */
    private ObjectContainer<PlatformDescription> platformDescription = new ObjectContainer<PlatformDescription>();

    //
    // Constructors
    //

    /**
     * Creates a new StartupManager.
     *
     * @param context The bundle context.
     */
    public StartupManager(BundleContext context) {
        setName("APSDiscoveryService Startup Manager");
        this.context = context;
    }

    //
    // Methods
    //

    public void startup() {
        super.start();
    }

    public synchronized void shutdown() {
        stopManagedThreads();
    }

    public void run() {
        startLogger();

        // The platform description is not essential for the service to perform so we get it when and if we can.
        PlatformDescription pfd = new PlatformDescription("?", "<unknown>", "<Not available!>");
        this.platformDescription.set(pfd);
        this.platformServiceTracker =
                new APSServiceTracker<APSPlatformService>(this.context, APSPlatformService.class, APSServiceTracker.LARGE_TIMEOUT);
        this.platformServiceTracker.start();
        this.platformServiceTracker.onActiveServiceAvailable(new OnServiceAvailable<APSPlatformService>() {
            @Override
            public void onServiceAvailable(APSPlatformService service, ServiceReference serviceReference) throws Exception {
                StartupManager.this.platformDescription.set(service.getPlatformDescription());
            }
        });

        // We do need our configuration so lets ensure it is managed before we access it. We can do this because we
        // are in a separate thread. This cannot be done directly in an activator!
        APSDiscoveryServiceConfig.mc.waitUtilManaged();

        // OKIDOKI, lets start things upp!
        try {
            startManagedThreads();
        }
        catch (Exception e) {
            this.discoveryServiceLogger.error(e.getMessage(), e);
        }
    }

    private void startManagedThreads() throws Exception {
        this.locallyPublishedServices = new ServiceDescriptions();
        this.remotelyPublishedServices = new ServiceDescriptions();

        createServiceProvider();

        startAutoRefreshRemoteInstancesThread();

        startOrUpdateMulticastDiscoveryListener();
        startOrUpdateMulticastDiscoveryAnnouncer();

        startOrUpdateUDPDiscoveryListener();
        startOrUpdateUDPDiscoveryAnnouncers();

        startServiceProvider();

        APSDiscoveryServiceConfig.mc.get().addConfigChangedListener(this);

    }

    private void stopManagedThreads() {
        APSDiscoveryServiceConfig.mc.get().removeConfigChangedListener(this);

        stopMulticastDiscoveryListener();
        stopMulticastDiscoveryAnnouncer();

        stopUDPDiscoveryListener();
        stopUDPDiscoveryAnnouncers();

        stopAutoRefreshRemoteInstancesThread();

        stopServiceProvider();

    }

    /**
     * Event listener callback when event occurs.
     *
     * @param event information about the event.
     */
    @Override
    public void apsConfigChanged(APSConfigChangedEvent event) {
        try {
            startAutoRefreshRemoteInstancesThread();

            startOrUpdateMulticastDiscoveryListener();
            startOrUpdateMulticastDiscoveryAnnouncer();

            startOrUpdateUDPDiscoveryListener();
            startOrUpdateUDPDiscoveryAnnouncers();
        }
        catch (Exception e) {
            this.discoveryServiceLogger.error(e.getMessage(), e);
        }
    }

    //
    // Logger
    //

    private void startLogger() {
        // Create a logger for this service. It will connect to the standard OSGi log service on start.
        // If no logservice is available it will log to stdout.
        this.discoveryServiceLogger = new APSLogger(System.out);
        this.discoveryServiceLogger.setLoggingFor("APSDiscoveryService");
        this.discoveryServiceLogger.start(this.context);
    }

    private void stopLogger() {
        this.discoveryServiceLogger.stop(this.context);
    }

    //
    // ServiceProvider
    //

    private void createServiceProvider() {
        this.simpleDiscoveryServiceProvider = new APSSimpleDiscoveryServiceProvider(this.platformDescription);
        this.simpleDiscoveryServiceProvider.setLocallyPublishedServices(locallyPublishedServices);
        this.simpleDiscoveryServiceProvider.setRemotelyPublishedServices(remotelyPublishedServices);
    }

    private void startServiceProvider() {
        Dictionary serviceProps = new Properties();
        serviceProps.put(Constants.SERVICE_PID, APSSimpleDiscoveryServiceProvider.class.getName());
        this.simpleDiscoveryService = this.context.registerService(APSSimpleDiscoveryService.class.getName(), simpleDiscoveryServiceProvider, serviceProps);

        // Update logger with service information and start it.
        this.discoveryServiceLogger.setServiceReference(this.simpleDiscoveryService.getReference());
    }

    private void stopServiceProvider() {
        if (this.simpleDiscoveryService != null) {
            this.simpleDiscoveryService.unregister();
        }
    }

    //
    // AutoRefreshRemoteInstances
    //

    private void startAutoRefreshRemoteInstancesThread() throws Exception{
        if (this.autoRefreshRemoteInstancesThread == null) {
            // Start a thread that will fire an event for all locally published services every 10 minutes.
            // The announcers will be listeners on this to forward the events remotely.
            this.autoRefreshRemoteInstancesThread =
                    new APSAutoRefreshRemoteInstancesThread(locallyPublishedServices, 10, this.discoveryServiceLogger);
            this.autoRefreshRemoteInstancesThread.start();
        }
    }

    private void stopAutoRefreshRemoteInstancesThread() {
        if (this.autoRefreshRemoteInstancesThread != null) {
            this.autoRefreshRemoteInstancesThread.stop();
            this.autoRefreshRemoteInstancesThread = null;
        }
    }

    //
    // MulticastDiscoveryListener
    //

    private void startOrUpdateMulticastDiscoveryListener() throws APSException {
        String multicastAddress = APSDiscoveryServiceConfig.mc.get().multicastAddress.toString().trim().toLowerCase();

        if (this.multicastDiscoveryListenerThread != null) {
            stopMulticastDiscoveryListener();
        }

        if (multicastAddress.length() > 0 && !multicastAddress.equals("disable")) {

            // Create a listener thread that will listen for service description events over a multicast group.
            this.multicastDiscoveryListenerThread = new APSMulticastDiscoveryListenerThread(
                    multicastAddress,
                    APSDiscoveryServiceConfig.mc.get().multicastPort.toInt(),
                    APSDiscoveryServiceConfig.mc.get().consecutiveReadFailureLimit.toInt(),
                    this.discoveryServiceLogger
            );
            this.multicastDiscoveryListenerThread.addDiscoveryEventListener(simpleDiscoveryServiceProvider);
            this.multicastDiscoveryListenerThread.start();
        }
    }

    private void stopMulticastDiscoveryListener() {
        if (this.multicastDiscoveryListenerThread != null) {
            this.multicastDiscoveryListenerThread.stop();
            this.multicastDiscoveryListenerThread = null;
        }
    }

    //
    // MulticastDiscoveryAnnouncer
    //

    private void startOrUpdateMulticastDiscoveryAnnouncer() throws IOException {
        String multicastAddress = APSDiscoveryServiceConfig.mc.get().multicastAddress.toString().trim().toLowerCase();

        if (this.multicastDiscoveryAnnouncer != null) {
            stopMulticastDiscoveryAnnouncer();
        }

        if (multicastAddress.length() > 0 && !multicastAddress.equals("disable")) {
            // Create an announcer that will send service description events over a multicast group.
            this.multicastDiscoveryAnnouncer = new APSMulticastDiscoveryAnnouncer(
                    multicastAddress,
                    APSDiscoveryServiceConfig.mc.get().multicastPort.toInt(),
                    this.discoveryServiceLogger
            );
            this.multicastDiscoveryAnnouncer.start();
            this.simpleDiscoveryServiceProvider.addLocalDiscoveryEventListener(this.multicastDiscoveryAnnouncer);
            this.autoRefreshRemoteInstancesThread.addDiscoveryEventListener(this.multicastDiscoveryAnnouncer);
        }
    }

    private void stopMulticastDiscoveryAnnouncer() {
        if (this.multicastDiscoveryAnnouncer != null) {
            if (this.simpleDiscoveryServiceProvider != null) {
                this.simpleDiscoveryServiceProvider.removeLocalDiscoveryEventListener(this.multicastDiscoveryAnnouncer);
            }
            if (this.autoRefreshRemoteInstancesThread != null) {
                this.autoRefreshRemoteInstancesThread.removeDiscoveryEventListener(this.multicastDiscoveryAnnouncer);
            }
            this.multicastDiscoveryAnnouncer.stop();
        }
    }

    //
    // UDPDiscoveryListener
    //

    private void startOrUpdateUDPDiscoveryListener() throws UnknownHostException, APSException {
        String udpLocalListenAddress = APSDiscoveryServiceConfig.mc.get().udpLocalListenAddress.toString().trim().toLowerCase();

        if (this.udpDiscoveryListenerThread != null) {
            stopUDPDiscoveryListener();
        }

        if (udpLocalListenAddress != null && udpLocalListenAddress.length() > 0 && !udpLocalListenAddress.equals("disable")) {
            // Setup an address for udp listener.
            String address = null;
            if (udpLocalListenAddress.equals("auto")) {
                address = InetAddress.getLocalHost().getHostAddress();
            }
            else {
                address = udpLocalListenAddress;
            }

            // Create a listener thread that will listen for service description events over UDP.
            this.udpDiscoveryListenerThread = new APSUDPDiscoveryListenerThread(
                    address,
                    APSDiscoveryServiceConfig.mc.get().udpLocalListenPort.toInt(),
                    APSDiscoveryServiceConfig.mc.get().consecutiveReadFailureLimit.toInt(),
                    this.discoveryServiceLogger
            );
            this.udpDiscoveryListenerThread.addDiscoveryEventListener(this.simpleDiscoveryServiceProvider);
            this.udpDiscoveryListenerThread.start();
        }
    }

    private void stopUDPDiscoveryListener() {
        if (this.udpDiscoveryListenerThread != null) {
            this.udpDiscoveryListenerThread.stop();
            this.udpDiscoveryListenerThread = null;
        }
    }

    //
    // UDPDiscoveryAnnouncers
    //

    private void startOrUpdateUDPDiscoveryAnnouncers() throws IOException {
        // Remove announcers no longer wanted.
        for (String key : this.udpDiscoveryAnnouncers.keySet()) {
            boolean isKeyStillInConfig = false;
            for (
                    APSDiscoveryServiceConfig.APSUDPRemoteDestinationDiscoveryServiceConfig staticDiscoveryServiceConfig :
                    APSDiscoveryServiceConfig.mc.get().udpTargetDiscoveryServices
            ) {
                String confKey =
                        staticDiscoveryServiceConfig.targetHost.toString() + staticDiscoveryServiceConfig.targetPort.toString();
                if (key.equals(confKey)) {
                    isKeyStillInConfig = true;
                    break;
                }
            }

            if (!isKeyStillInConfig) {
                APSUDPDiscoveryAnnouncer announcer = this.udpDiscoveryAnnouncers.remove(key);
                this.simpleDiscoveryServiceProvider.removeLocalDiscoveryEventListener(announcer);
                this.autoRefreshRemoteInstancesThread.removeDiscoveryEventListener(announcer);
                announcer.stop();
            }
        }

        // Create an announcer that will send service description events over UDP.
        for (
            APSDiscoveryServiceConfig.APSUDPRemoteDestinationDiscoveryServiceConfig staticDiscoveryServiceConfig :
            APSDiscoveryServiceConfig.mc.get().udpTargetDiscoveryServices
        ) {
            String key = staticDiscoveryServiceConfig.targetHost.toString() + staticDiscoveryServiceConfig.targetPort.toString();

            if (this.udpDiscoveryAnnouncers.get(key) == null) {
                APSUDPDiscoveryAnnouncer udpDiscoveryAnnouncer = new APSUDPDiscoveryAnnouncer(
                        staticDiscoveryServiceConfig.targetHost.toString(),
                        staticDiscoveryServiceConfig.targetPort.toInt(),
                        this.discoveryServiceLogger
                );
                udpDiscoveryAnnouncer.start();
                this.simpleDiscoveryServiceProvider.addLocalDiscoveryEventListener(udpDiscoveryAnnouncer);
                this.autoRefreshRemoteInstancesThread.addDiscoveryEventListener(udpDiscoveryAnnouncer);
                this.udpDiscoveryAnnouncers.put(key, udpDiscoveryAnnouncer);
            }
        }
    }

    private void stopUDPDiscoveryAnnouncers() {
        for (String key : this.udpDiscoveryAnnouncers.keySet()) {
            APSUDPDiscoveryAnnouncer announcer = this.udpDiscoveryAnnouncers.remove(key);
            this.simpleDiscoveryServiceProvider.removeLocalDiscoveryEventListener(announcer);
            this.autoRefreshRemoteInstancesThread.removeDiscoveryEventListener(announcer);
            announcer.stop();
        }
    }
}
