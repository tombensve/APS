package se.natusoft.osgi.aps.net.messaging.service

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.docutations.NotNull
import se.natusoft.docutations.Nullable
import se.natusoft.osgi.aps.api.messaging.APSMessagePublisher
import se.natusoft.osgi.aps.api.messaging.APSMessageSubscriber
import se.natusoft.osgi.aps.api.messaging.APSMessagingException
import se.natusoft.osgi.aps.api.messaging.APSMessage
import se.natusoft.osgi.aps.constants.APS
import se.natusoft.osgi.aps.exceptions.APSValidationException
import se.natusoft.osgi.aps.model.APSHandler
import se.natusoft.osgi.aps.model.APSResult
import se.natusoft.osgi.aps.model.ID
import se.natusoft.osgi.aps.net.messaging.config.Config
import se.natusoft.osgi.aps.net.messaging.rabbitmq.PeskyWabbitConnectionManager
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.annotation.activator.*

/**
 * Provides and manages this service.*/
@SuppressWarnings( "GroovyUnusedDeclaration" )
@CompileStatic
@TypeChecked
// @formatter:off
@OSGiServiceProvider(
        serviceAPIs = [APSMessagePublisher.class, APSMessageSubscriber.class],
        properties = [
                @OSGiProperty(name = APS.Service.Provider, value = "aps-rabbitmq-message-provider"),
                @OSGiProperty(name = APS.Service.Category, value = APS.Value.Messaging.Service.Category),
                @OSGiProperty(name = APS.Service.Function, value = APS.Value.Messaging.Service.Function),
                @OSGiProperty(name = APS.Messaging.Persistent, value = APS.TRUE),
                @OSGiProperty(name = APS.Messaging.MultipleReceivers, value = APS.TRUE)
        ]
)
// @formatter:on
class APSRabbitMQSimpleMessageServiceProvider implements APSMessagePublisher<byte[]>, APSMessageSubscriber<byte[]> {

    //
    // Private Members
    //

    /** Our logger. */
    @Managed( loggingFor = "aps-rabbitmq-simple-message-service-provider" )
    private APSLogger logger

    /** For connecting to RabbitMQ. */
    private PeskyWabbitConnectionManager rabbitMQConnectionManager

    /** The defined instances. */
    private Map<String, APSRabbitMQMessageProvider> instances = [ : ]

    /** Maps subscriber id to topic. */
    private Map<ID, String> idToTopic = [ : ]

    //
    // Startup / Shutdown Methods
    //

    /**
     * This method is run on bundle start.
     *
     * It will register a configuration listener and then start all configured instances. The configuration listener
     * will reconnect to the RabbitMQ message bus in case connection configold has changed, and then take down deleted
     * instances and start newly defined instances.*/
    @SuppressWarnings( "GroovyUnusedDeclaration" )
    @BundleStart( thread = true )
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
     * It will take down all instances.*/
    @SuppressWarnings( "GroovyUnusedDeclaration" )
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
            Config.config[ 'instances' ].each { String key, Map<String, Serializable> instance -> startInstance( instance ) }
        }
        catch ( Throwable t ) {
            this.logger.debug( "Cluster setup failure: " + t.message, t )
        }
    }

    private void stopAllInstances() {
        this.instances.each { String name, APSRabbitMQMessageProvider msp -> stopInstance( msp ) }
    }

    private void startInstance( Map<String, Serializable> instance ) {

        APSRabbitMQMessageProvider messageService = new APSRabbitMQMessageProvider( logger: this.logger,
                name: instance.name as String,
                connectionProvider: { return this.rabbitMQConnectionManager.connection },
                instanceConfig: instance )
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

            !Config.config.instances.any { Map.Entry<String, LinkedHashMap<String, String>> entry -> entry.value.name == name
            }
        }.each { String name, APSRabbitMQMessageProvider instance ->

            stopInstance( instance )
        }
    }

    private void startNewInstances() {
        Config.config.instances.findAll { Map.Entry<String, LinkedHashMap<String, String>> entry ->

            !this.instances.containsKey( entry.value.name )
        }.each { Object e ->

            startInstance( ( (Map.Entry<String, LinkedHashMap<String, String>>) e ).value as Map<String, Serializable> )
        }
    }

    //
    // Service Methods
    //

    /**
     * Publishes a message.
     *
     * @param destination The destination of the message. Preferably this is something that the
     *                    service looks up to get a real destination, rather than an absolute
     *                    destination.
     * @param message The message to publish.
     *
     * @throws APSMessagingException on any failure. Note that this is a RuntimeException!
     */
    @Override
    void publish( String destination, byte[] message ) throws APSMessagingException {

        APSRabbitMQMessageProvider messageProvider = this.instances.get( destination )

        if ( messageProvider == null ) {

            throw new IllegalArgumentException( "sendMessage(): No such topic: '${destination}'!" )
        }

        messageProvider.sendMessage( message )
    }

    /**
     * Publishes a message receiving a result of success or failure. On Success there
     * can be a result value and on failure there is an Exception describing the failure
     * available. This variant never throws an Exception.
     *
     * @param destination The destination of the message. Preferably this is something that the
     *                    service looks up to get a real destination, rather than an absolute
     *                    destination.
     * @param message The message to publish.
     * @param result Callback providing the success or failure of the call.
     */
    @Override
    void publish( String destination, byte[] message, APSHandler result ) {

        try {
            publish( destination, message )

            result.handle( APSResult.success( null ) )
        }
        catch ( APSMessagingException me ) {

            result.handle( APSResult.failure( me ) )
        }

    }

    /**
     * Adds a subscriber.
     *
     * @param destination The destination to subscribe to.
     *                       This is up to the implementation, but it is strongly recommended that
     *                       this is a name that will be looked up in some configuration for the real
     *                       destination, by the service rather than have the client pass a value from
     *                       its configuration.
     * @param subscriptionId A unique ID used to later cancel the subscription. Use UUID or some other ID
     *                       implementation that is always unique.
     * @param handler The subscription handler.
     */
    @Override
    void subscribe( @NotNull String destination, @NotNull ID subscriptionId, @Nullable APSHandler<APSResult> result,
                    @NotNull APSHandler<APSMessage<byte[]>> handler ) {

        APSRabbitMQMessageProvider messageProvider = this.instances.get( destination )

        if ( messageProvider == null ) {

            APSResult.failureResult( result,
                    new APSValidationException( "addMessageListener(): No such topic: '${destination}'!" ) )
        }

        messageProvider.addMessageSubscriber( subscriptionId, handler )
        this.idToTopic[ subscriptionId ] = destination

    }

    /**
     * Cancel a subscription.
     *
     * @param subscriptionId The same id as passed to subscribe.
     */
    @Override
    void unsubscribe( @NotNull ID subscriptionId, @Nullable APSHandler result ) {

        String topic = this.idToTopic[ subscriptionId ]

        APSRabbitMQMessageProvider messageProvider = this.instances.get( topic )

        if ( messageProvider == null ) {
            throw new IllegalArgumentException( "removeMessageListener(): No such topic: '${topic}'!" )
        }

        messageProvider.removeMessageSubscriber( subscriptionId )

    }
}
