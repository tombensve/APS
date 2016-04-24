package se.natusoft.osgi.aps.net.messaging.service

import com.rabbitmq.client.Connection
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.osgi.framework.BundleContext
import se.natusoft.osgi.aps.api.core.config.event.APSConfigChangedEvent
import se.natusoft.osgi.aps.api.core.config.event.APSConfigChangedListener
import se.natusoft.osgi.aps.api.net.messaging.exception.APSMessagingException

import se.natusoft.osgi.aps.api.net.messaging.service.APSSimpleMessageService
import se.natusoft.osgi.aps.api.net.util.TypedData
import se.natusoft.osgi.aps.codedoc.Issue
import se.natusoft.osgi.aps.net.messaging.apis.ConnectionProvider
import se.natusoft.osgi.aps.net.messaging.config.RabbitMQMessageServiceConfig
import se.natusoft.osgi.aps.net.messaging.rabbitmq.PeskyWabbitConnectionManager
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.annotation.activator.BundleStart
import se.natusoft.osgi.aps.tools.annotation.activator.BundleStop
import se.natusoft.osgi.aps.tools.annotation.activator.Managed
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiProperty
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiServiceProvider

/**
 * Provides and manages this service.
 *
 * __NOTE:__ This implementation does not support "contentType"! It will ignore what is passed and always deliver "UNKNOWN".
 */
@CompileStatic
@TypeChecked
@OSGiServiceProvider(
        properties = @OSGiProperty(
                // Since this class implements APSSimpleMessageService it would make sense for this constant
                // to not require full qualification. The Groovy compiler (2.4.5) is however of a different
                // opinion. Maybe its because this is referenced in an annotation outside of the class.
                // IDEA however complains loudly about the full qualification here, incorrectly (well, the
                // compiler wins this argument), saying it is "unnecessary".
                name = APSSimpleMessageService.APS_MESSAGE_SERVICE_PROVIDER,
                value = "aps-rabbitmq-simple-message-provider"
        )
)
class APSRabbitMQSimpleMessageServiceProvider implements APSSimpleMessageService {

    //
    // Private Members
    //

    /** Our logger. */
    @Managed(loggingFor = "aps-rabbitmq-simple-message-service-provider")
    private APSLogger logger

    @Managed
    private BundleContext bundleContext

    /** Listens to configuration changes. */
    private APSConfigChangedListener configChangedListener

    /** For connecting to RabbitMQ. */
    private PeskyWabbitConnectionManager rabbitMQConnectionManager

    /** The defined instances. */
    private Map<String, APSRabbitMQMessageProvider> instances = new HashMap<>()

    //
    // Service Methods
    //

    /**
     * Adds a listener for types.
     *
     * @param topic The topic to listen to.
     * @param listener The listener to add.
     */
    @Override
    void addMessageListener(String topic, APSSimpleMessageService.MessageListener listener) {
        APSRabbitMQMessageProvider messageProvider = this.instances.get(topic)
        if (messageProvider == null) {
            throw new IllegalArgumentException("addMessageListener(): No such topic: '${topic}'!")
        }
        messageProvider.addMessageListener(listener)
    }

    /**
     * Removes a messaging listener.
     *
     * @param topic The topic to stop listening to.
     * @param listener The listener to remove.
     */
    @Override
    void removeMessageListener(String topic, APSSimpleMessageService.MessageListener listener) {
        APSRabbitMQMessageProvider messageProvider = this.instances.get(topic)
        if (messageProvider == null) {
            throw new IllegalArgumentException("removeMessageListener(): No such topic: '${topic}'!")
        }
        messageProvider.removeMessageListener(listener)
    }

    /**
     * Sends a message.
     *
     * @param topic The topic of the message.
     * @param message The message to send.
     *
     * @throws se.natusoft.osgi.aps.api.net.messaging.exception.APSMessagingException on failure.
     */
    @Override
    void sendMessage(String topic, TypedData message) throws APSMessagingException{
        APSRabbitMQMessageProvider messageProvider = this.instances.get(topic)
        if (messageProvider == null) {
            throw new IllegalArgumentException("sendMessage(): No such topic: '${topic}'!")
        }
        messageProvider.sendMessage(message.content)
    }


    //
    // Startup / Shutdown Methods
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
                        APSRabbitMQSimpleMessageServiceProvider.this.rabbitMQConnectionManager.reconnect()
                        refreshInstances()
                    }
                    catch (IOException ioe) {
                        APSRabbitMQSimpleMessageServiceProvider.this.logger.error("Failed reconnecting to RabbitMQ!", ioe)
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
        this.instances.each { String name, APSRabbitMQMessageProvider msp ->
            stopInstance(msp)
        }
    }

    private void startInstance(RabbitMQMessageServiceConfig.RMQInstance instance) {

        APSRabbitMQMessageProvider messageService = new APSRabbitMQMessageProvider(
                logger: this.logger,
                name: instance.name.string,
                connectionProvider: new ConnectionProvider() {
                    @Override
                    Connection getConnection() throws IOException {
                        return APSRabbitMQSimpleMessageServiceProvider.this.rabbitMQConnectionManager.connection
                    }
                },
                instanceConfig: instance
        )
        messageService.start()

        this.instances.put(instance.name.string, messageService);
    }

    private void stopInstance(APSRabbitMQMessageProvider instance) {
        try {
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
        this.instances.findAll { String name, APSRabbitMQMessageProvider instance ->
            !RabbitMQMessageServiceConfig.managed.get().instances.any {
                RabbitMQMessageServiceConfig.RMQInstance instanceConfig -> instanceConfig.name.string.equals(name)
            }
        }.each { String name, APSRabbitMQMessageProvider instance -> stopInstance(instance) }
    }

    @Issue(
            target = "IntelliJ IDEA", targetVersion = "14.0.2-2016.1",
            id ="IDEA-134831",
            description = [
                    "'Object instance ->' is incorrectly marked as an error!",
                    "This has priority 'Major' and nothing has been done yet!"
                    ],
            url = "https://youtrack.jetbrains.com/issue/IDEA-134831",
            dateReported = "28 Dec 2014"
    )
    private void startNewInstances() {
        RabbitMQMessageServiceConfig.managed.get().instances.findAll { RabbitMQMessageServiceConfig.RMQInstance instance ->
            !this.instances.containsKey(instance.name.string)
        }.each { Object instance ->
            startInstance((RabbitMQMessageServiceConfig.RMQInstance)instance)
        }
    }

}
