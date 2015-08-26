package se.natusoft.osgi.aps.net.messaging

import com.rabbitmq.client.Connection
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.osgi.framework.BundleContext
import org.osgi.framework.ServiceRegistration
import se.natusoft.osgi.aps.api.core.config.event.APSConfigChangedEvent
import se.natusoft.osgi.aps.api.core.config.event.APSConfigChangedListener
import se.natusoft.osgi.aps.api.misc.json.service.APSJSONService
import se.natusoft.osgi.aps.api.net.messaging.service.APSMessageService
import se.natusoft.osgi.aps.codedoc.Issue
import se.natusoft.osgi.aps.net.messaging.apis.ConnectionProvider
import se.natusoft.osgi.aps.net.messaging.config.RabbitMQMessageServiceConfig
import se.natusoft.osgi.aps.net.messaging.rabbitmq.PeskyWabbitConnectionManager
import se.natusoft.osgi.aps.net.messaging.service.APSRabbitMQMessageServiceProvider
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.annotation.activator.BundleStart
import se.natusoft.osgi.aps.tools.annotation.activator.BundleStop
import se.natusoft.osgi.aps.tools.annotation.activator.Managed
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiService

/**
 * Manages starting and stopping of this bundle.
 */
@CompileStatic
@TypeChecked
class BundleManagement {

    //
    // Private Members
    //

    /** Our logger. */
    @Managed(loggingFor = "aps-rabbitmq-message-service-provider")
    private APSLogger logger

    @Managed
    private BundleContext bundleContext

    /** Listens to configuration changes. */
    private APSConfigChangedListener configChangedListener

    /** For connecting to RabbitMQ. */
    private PeskyWabbitConnectionManager rabbitMQConnectionManager

    /** The defined instances. */
    private Map<String, APSRabbitMQMessageServiceProvider> instances = new HashMap<>()
    private Map<String, ServiceRegistration> serviceRegistrations = new HashMap<>()

    //
    // Methods
    //

    /**
     * This method is run on bundle start.
     *
     * It will register a configuration listener and then start all configured instances. The configuration listener
     * will reconnect to the RabbitMQ message bus in case connection config has changed, and then take down deleted
     * instances and start newly defined instances.
     */
    @BundleStart(thread = true)
    public void startup() {
        // Since this is called on bundle startup the whole startup process will halt until this returns,
        // so we do what we need to do in a thread instead and then return immediately. The catch is that
        // the bundle start will look successful even if it wasn't. Any failure are however logged so it
        // is possible to see that it failed in the log.
        try {
            this.rabbitMQConnectionManager = new PeskyWabbitConnectionManager()

            this.logger.info(this.rabbitMQConnectionManager.ensureConnection())

            this.configChangedListener = new APSConfigChangedListener() {
                @Override
                public synchronized void apsConfigChanged(APSConfigChangedEvent event) {
                    try {
                        BundleManagement.this.rabbitMQConnectionManager.reconnect()
                        refreshInstances()
                    }
                    catch (IOException ioe) {
                        BundleManagement.this.logger.error("Failed reconnecting to RabbitMQ!", ioe)
                    }
                }
            }

            RabbitMQMessageServiceConfig.managed.get().addConfigChangedListener(this.configChangedListener)

            startAllInstances()
        }
        catch (IOException ioe) {
            this.logger.error("Failed to connect to RabbitMQ!", ioe)
        }
    }

    /**
     * This method is run on bundle stop.
     *
     * It will take down all instances.
     */
    @BundleStop
    public void shutdown() {
        if (this.configChangedListener != null) {
            RabbitMQMessageServiceConfig.managed.get().removeConfigChangedListener(this.configChangedListener)
        }

        stopAllInstances()

        try {
            this.logger.info(this.rabbitMQConnectionManager.ensureConnectionClosed())
        }
        catch (IOException ioe) {
            this.logger.error("Failed to stop RabbitMQ connection!", ioe)
        }
    }

    private void startAllInstances() {
        try {
            RabbitMQMessageServiceConfig.managed.get().instances.each { RabbitMQMessageServiceConfig.RMQInstance instance ->
                startInstance(instance)
            }
        }
        catch (Throwable t) {
            this.logger.debug("Cluster setup failure: " + t.message, t)
        }
    }

    private void stopAllInstances() {
        this.instances.each { String name, APSRabbitMQMessageServiceProvider msp ->
            stopInstance(msp)
        }
    }

    private void startInstance(RabbitMQMessageServiceConfig.RMQInstance instance) {

        APSRabbitMQMessageServiceProvider messageService = new APSRabbitMQMessageServiceProvider(
                logger: this.logger,
                name: instance.name.string,
                connectionProvider: new ConnectionProvider() {
                    @Override
                    Connection getConnection() throws IOException {
                        return BundleManagement.this.rabbitMQConnectionManager.connection
                    }
                },
                instanceConfig: instance
        )
        messageService.start()

        this.instances.put(instance.name.string, messageService);

        Properties props = new Properties()
        props.setProperty(APSMessageService.APS_MESSAGE_SERVICE_PROVIDER, "rabbitmq")
        props.setProperty(APSMessageService.APS_MESSAGE_SERVICE_INSTANCE_NAME, instance.name.string)
        ServiceRegistration reg = this.bundleContext.registerService(APSMessageService.class.name, messageService, props)
        this.serviceRegistrations.put(instance.name.string, reg)
    }

    private void stopInstance(APSRabbitMQMessageServiceProvider instance) {
        try {
            ServiceRegistration reg = this.serviceRegistrations.get(instance.name)
            reg.unregister()
            instance.stop()
        }
        catch (IOException ioe) {
            this.logger.error("Failed to stop APSCluster instance! [" + ioe.getMessage() + "]", ioe)
        }
    }

    /**
     * This gets called on configuration change.
     */
    private void refreshInstances() {
        closeRemovedInstances()
        startNewInstances()
    }

    private void closeRemovedInstances() {
        this.instances.findAll { String name, APSRabbitMQMessageServiceProvider instance ->
            !RabbitMQMessageServiceConfig.managed.get().instances.any {
                RabbitMQMessageServiceConfig.RMQInstance instanceConfig -> instanceConfig.name.string.equals(name)
            }
        }.each { String name, APSRabbitMQMessageServiceProvider instance -> stopInstance(instance) }
    }

    @Issue(
            target = "IntelliJ IDEA", targetVersion = "14.0.2",
            id ="IDEA-134831",
            description = "'Object cluster ->' is incorrectly marked as an error!",
            url = "https://youtrack.jetbrains.com/issue/IDEA-134831"
    )
    private void startNewInstances() {
        RabbitMQMessageServiceConfig.managed.get().instances.findAll { RabbitMQMessageServiceConfig.RMQInstance instance ->
            !this.instances.containsKey(instance.name.string)
        }.each { Object instance ->
            startInstance((RabbitMQMessageServiceConfig.RMQInstance)instance)
        }
    }

}
