package se.natusoft.osgi.aps.net.messaging.service

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.osgi.aps.api.pubsub.APSPubSubService
import se.natusoft.osgi.aps.api.pubsub.APSPublisher
import se.natusoft.osgi.aps.api.pubsub.APSSender
import se.natusoft.osgi.aps.api.pubsub.APSSubscriber
import se.natusoft.osgi.aps.constants.APS
import se.natusoft.osgi.aps.net.messaging.config.Config
import se.natusoft.osgi.aps.net.messaging.rabbitmq.PeskyWabbitConnectionManager
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.annotation.activator.*

/**
 * Provides and manages this service.
 *
 * __NOTE:__ This implementation does not support "TypedData.contentType"! It will ignore what is passed and
 * always deliver "UNKNOWN".
 */
@SuppressWarnings("GroovyUnusedDeclaration")
@CompileStatic
@TypeChecked
@OSGiServiceProvider(
        properties = [
                @OSGiProperty(name = APS.Service.Provider, value = "aps-rabbitmq-simple-message-provider"),
                @OSGiProperty(name = APS.Service.Category, value = APS.Value.Messaging.Service.Category),
                @OSGiProperty(name = APS.Service.Function, value = APS.Value.Messaging.Service.Function),
                @OSGiProperty(name = APS.Messaging.Persistent, value = APS.TRUE),
                @OSGiProperty(name = APS.Messaging.MultipleReceivers, value = APS.TRUE)
        ]
)
class APSRabbitMQSimpleMessageServiceProvider implements APSPubSubService<byte[]> {

    //
    // Private Members
    //

    /** Our logger. */
    @Managed(loggingFor = "aps-rabbitmq-simple-message-service-provider")
    private APSLogger logger

    /** For connecting to RabbitMQ. */
    private PeskyWabbitConnectionManager rabbitMQConnectionManager

    /** The defined instances. */
    private Map<String, APSRabbitMQMessageProvider> instances = new HashMap<>()

    //
    // Service Methods
    //
    /**
     * Returns a publisher to publish with.
     *
     * @param meta Meta data for the publisher.
     */
    APSPublisher<byte[]> publisher( Map<String, String> meta ) {
        String topic = meta[ "topic" ]
        APSRabbitMQMessageProvider messageProvider = this.instances.get( topic )
        if ( messageProvider == null ) {
            throw new IllegalArgumentException( "sendMessage(): No such topic: '${topic}'!" )
        }
        return new Publisher<>( meta: meta, messageProvider: messageProvider )
    }

    /**
     * Returns a sender to send with. Depending on implementation the APSSender instance returned can possibly
     * be an APSReplyableSender that allows for providing a subscriber for a reply to the sent message.
     *
     * @param meta Meta data for the sender.
     */
    APSSender<byte[]> sender( Map<String, String> meta ) {
        throw new UnsupportedOperationException("This RabbitMQ implementation only provides a publisher!")
    }

    /**
     * Adds a subscriber.
     *
     * @param subscriber The subscriber to add.
     * @param meta Meta data. This depends on the implementation. Can possibly be null when not used. For example
     *                   if there is a need for an address or topic put it in the meta data.
     */
    void subscribe( APSSubscriber<byte[]> subscriber, Map<String, String> meta ) {
        String topic = meta[ "topic" ]
        APSRabbitMQMessageProvider messageProvider = this.instances.get( topic )
        if ( messageProvider == null ) {
            throw new IllegalArgumentException( "addMessageListener(): No such topic: '${topic}'!" )
        }
        messageProvider.addMessageSubscriber( subscriber )

    }

    /**
     * Removes a subscriber.
     *
     * @param subscriber The consumer to remove.
     * @param meta Meta data. Key "topic" provides the topic.
     */
    void unsubscribe( APSSubscriber<byte[]> subscriber, Map<String, String> meta ) {
        String topic = meta[ "topic" ]
        APSRabbitMQMessageProvider messageProvider = this.instances.get( topic )
        if ( messageProvider == null ) {
            throw new IllegalArgumentException( "removeMessageListener(): No such topic: '${topic}'!" )
        }
        messageProvider.removeMessageSubscriber( subscriber )
    }

    /**
     * Provides a callback that gets called when service is ready to work.
     *
     * @param onReady The callback to call when service is ready to work.
     */
    @Override
    void onReady( Runnable onReady ) {
        onReady.run()
    }

    /**
     * Provides a callback that gets called when service is no longer in a ready state.
     *
     * @param onNotReady The callback to call when service is no longer in ready state.
     */
    @Override
    void onNotReady( Runnable onNotReady ) {}

    //
    // Startup / Shutdown Methods
    //

    /**
     * This method is run on bundle start.
     *
     * It will register a configuration listener and then start all configured instances. The configuration listener
     * will reconnect to the RabbitMQ message bus in case connection configold has changed, and then take down deleted
     * instances and start newly defined instances.
     */
    @SuppressWarnings("GroovyUnusedDeclaration")
    @BundleStart(thread = true)
    void startup() {
        // Since this is called on bundle startup the whole startup process will halt until this returns,
        // so we do what we need to do in a thread instead and then return immediately. The catch is that
        // the bundle start will look successful even if it wasn't. Any failure are however logged so it
        // is possible to see that it failed in the log.
        try {
            this.rabbitMQConnectionManager = new PeskyWabbitConnectionManager()

            this.logger.info( this.rabbitMQConnectionManager.ensureConnection() )

            startAllInstances()
        }
        catch ( IOException ioe ) {
            this.logger.error( "Failed to connect to RabbitMQ!", ioe )
        }
    }

    /**
     * This method is run on bundle stop.
     *
     * It will take down all instances.
     */
    @SuppressWarnings("GroovyUnusedDeclaration")
    @BundleStop
    void shutdown() {
        stopAllInstances()

        try {
            this.logger.info( this.rabbitMQConnectionManager.ensureConnectionClosed() )
        }
        catch ( IOException ioe ) {
            this.logger.error( "Failed to stop RabbitMQ connection!", ioe )
        }
    }

    private void startAllInstances() {
        try {
            Config.config[ 'instances' ].each { String key, Map<String, Serializable> instance ->
                startInstance( instance )
            }
        }
        catch ( Throwable t ) {
            this.logger.debug( "Cluster setup failure: " + t.message, t )
        }
    }

    private void stopAllInstances() {
        this.instances.each { String name, APSRabbitMQMessageProvider msp ->
            stopInstance( msp )
        }
    }

    private void startInstance( Map<String, Serializable> instance ) {

        APSRabbitMQMessageProvider messageService = new APSRabbitMQMessageProvider(
                logger: this.logger,
                name: instance.name as String,
                connectionProvider: { return this.rabbitMQConnectionManager.connection },
                instanceConfig: instance
        )
        messageService.start()

        this.instances.put( instance.name as String, messageService )
    }

    private void stopInstance( APSRabbitMQMessageProvider instance ) {
        try {
            instance.stop()
        }
        catch ( IOException ioe ) {
            this.logger.error( "Failed to stop APSCluster instance! [" + ioe.getMessage() + "]", ioe )
        }
    }

    private void refreshInstances() {
        closeRemovedInstances()
        startNewInstances()
    }

    private void closeRemovedInstances() {
        this.instances.findAll { String name, APSRabbitMQMessageProvider instance ->
            !Config.config.instances.any { Map.Entry<String, LinkedHashMap<String, String>> entry -> entry.value.name == name }
        }.each { String name, APSRabbitMQMessageProvider instance -> stopInstance( instance ) }
    }

    private void startNewInstances() {
        Config.config.instances.findAll { Map.Entry<String, LinkedHashMap<String, String>> entry ->
            !this.instances.containsKey( entry.value.name )
        }.each { Object e  -> startInstance(((Map.Entry<String, LinkedHashMap<String, String>>)e).value as Map<String, Serializable>) }
    }
}
