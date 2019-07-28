package se.natusoft.osgi.aps.net.vertx

import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.core.eventbus.MessageConsumer
import se.natusoft.docutations.NotNull
import se.natusoft.docutations.Nullable
import se.natusoft.docutations.Optional
import se.natusoft.osgi.aps.activator.annotation.BundleStop
import se.natusoft.osgi.aps.activator.annotation.OSGiProperty
import se.natusoft.osgi.aps.activator.annotation.OSGiService
import se.natusoft.osgi.aps.activator.annotation.OSGiServiceProvider
import se.natusoft.osgi.aps.api.messaging.APSBusRouter
import se.natusoft.osgi.aps.api.messaging.APSMessagingException
import se.natusoft.osgi.aps.constants.APS
import se.natusoft.osgi.aps.net.vertx.util.RecursiveJsonObjectMap
import se.natusoft.osgi.aps.types.APSHandler
import se.natusoft.osgi.aps.types.APSResult
import se.natusoft.osgi.aps.types.ID

/**
 * Provides and APSBusRouter implementation using Vert.x EventBus for communication.
 *
 * This uses 'cluster' as id part of target, and also supports "all:".
 */
@SuppressWarnings( "unused" ) // Managed by APSActivator IDE can't see that!
@OSGiServiceProvider(
        properties = [
                @OSGiProperty( name = APS.Service.Provider, value = "aps-vertx-bus-router" ),
                @OSGiProperty( name = APS.Service.Category, value = APS.Value.Service.Category.Network ),
                @OSGiProperty( name = APS.Service.Function, value = APS.Value.Service.Function.Messaging ),
                @OSGiProperty( name = APS.Messaging.Protocol.Name, value = "vertx-eventbus" ),
                @OSGiProperty( name = APS.Messaging.Persistent, value = APS.FALSE ),
                @OSGiProperty( name = APS.Messaging.Clustered, value = APS.TRUE )
        ]
)
class APSVertxBusRouter implements APSBusRouter {

    private static final TARGET_ID = "cluster:"

    //
    // Private members
    //

    // This will work since nonBlocking is true! Even if this executes before the EventBus is available
    // calls will be cached until EventBus is available and then passed on. So we do not need to make
    // sure we have this.
    @OSGiService( nonBlocking = true )
    private EventBus eventBus

    private Map<ID, MessageConsumer> subscriptions = [:]

    //
    // Methods
    //

    /**
     * This checks if provided target is valid and if so proceeds with the operation.
     *
     * @param target Target to validate.
     * @param go Closure to call on valid target.
     */
    private static void validTarget( String target, Closure go ) {
        if ( target.startsWith( TARGET_ID ) ) {
            target = target.substring( TARGET_ID.length() )

            go.call( target )
        }
        else if (target.startsWith( "all:" )) {
            target = target.substring( 4 )

            go.call( target )
        }
    }

    /**
     * Sends a message.
     *
     * @param target The target to send to. How to interpret this is up to implementation.
     * @param message The message to send. Only JSON structures allowed and top level has to be an object.
     * @param resultHandler The handler to call with result of operation. Can be null!
     */
    @Override
    void send( @NotNull String target, @NotNull Map<String, Object> message, @Optional @Nullable APSHandler<APSResult> resultHandler ) {

        validTarget( target ) { String realTarget ->

            this.eventBus.send( realTarget, message )

            resultHandler.handle( APSResult.success( null ) )
        }
    }

    /**
     * Subscribes to messages to a target.
     *
     * @param id A unique ID to associate subscription with. Also used to unsubscribe.
     * @param target The target to subscribe to.
     * @param resultHandler The result of the subscription.
     * @param messageHandler The handler to call with messages sent to target.
     */
    @Override
    void subscribe( @NotNull ID id, @NotNull String target, @Optional @Nullable APSHandler<APSResult> resultHandler, @NotNull APSHandler<Map<String, Object>> messageHandler ) {

        validTarget( target ) { String realTarget ->

            MessageConsumer consumer = this.eventBus.consumer( target ) { Message<Map<String, Object>> msg ->

                Map<String, Object> message = new RecursiveJsonObjectMap( msg.body() )
                messageHandler.handle( message )
            }

            if ( consumer == null ) { // Not sure this can actually happen ...
                resultHandler.handle( APSResult.failure( new APSMessagingException( "Failed to get MessageConsumer!" ) ) )
            }
            else {
                this.subscriptions[ id ] = consumer
            }
        }
    }

    /**
     * Releases a subscription.
     *
     * @param subscriberId The ID returned by subscribe.
     */
    @Override
    void unsubscribe( @NotNull ID subscriberId ) {
        MessageConsumer consumer = this.subscriptions[ subscriberId ]
        consumer.unregister()
    }

    @BundleStop
    void cleanup() {
        // This is not an excuse for clients to not clean up after themselves! And this will not be done
        // until we shut down.
        this.subscriptions.keySet().each { ID key ->
            this.subscriptions[ key ].unregister()
        }
        this.subscriptions.clear(  )
    }
}
