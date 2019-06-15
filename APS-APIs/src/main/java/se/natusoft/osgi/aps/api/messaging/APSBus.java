package se.natusoft.osgi.aps.api.messaging;

import org.osgi.framework.BundleContext;
import se.natusoft.docutations.NotNull;
import se.natusoft.docutations.Nullable;
import se.natusoft.docutations.Optional;
import se.natusoft.osgi.aps.tracker.APSServiceTracker;
import se.natusoft.osgi.aps.types.APSHandler;
import se.natusoft.osgi.aps.types.APSResult;
import se.natusoft.osgi.aps.types.ID;

import java.util.Map;

/**
 * This is a simple bus API that is used by creating an instance and passing a BundleContext.
 *
 * All calls will be passed to all the APSBusRouter implementations tracked.
 *
 * Note that even if there are no APSBusRouter services published, the APSLocalInMemoryBus,
 * which is a router, is always available. It however only reacts on targets starting with "local:".
 *
 * There are 2 ways of making other buses available through APSBus:
 * 1. Implement APSBusRouter and publish as OSGi service. In this case you should probably only
 *    react on targets with a specific prefix or certain specific, configured targets. Probably
 *    never everything.
 * 2. Subscribe to "local:(mybus):(target)" or something that way and forward received messages
 *    on your bus. Example: "local:amqp:..." and forward to a RabbitMQ.
 */
@SuppressWarnings( "unused" )
public class APSBus {

    //
    // Private Members
    //

    /** The currently known bus routers */
    @Nullable private APSServiceTracker<APSBusRouter> routerTracker = null;

    //
    // Constructors
    //

    /**
     * Creates a new APSBus tracking all deployed bus routers.
     *
     * @param bundleContext The bundle context of the caller.
     */
    public APSBus( @NotNull BundleContext bundleContext ) {

        this.routerTracker = new APSServiceTracker<>( bundleContext, APSBusRouter.class );
    }

    /**
     * This constructor will not track published APSBusRouter services. It will have only
     * one rotuer, APSLocalInMemoryBus. This is intended for testing.
     */
    @SuppressWarnings( "WeakerAccess" )
    public APSBus() {}

    //
    // Methods
    //

    /**
     * Sends a message.
     *
     * @param target        The target to send to. How to interpret this is up to implementation.
     * @param message       The message to send. Only JSON structures allowed and top level has to be an object.
     * @param resultHandler Receives the success or failure of the call.
     */
    void send( @NotNull String target, @NotNull Map<String, Object> message,
               @Optional @Nullable APSHandler<APSResult<Void>> resultHandler ) {

        APSLocalInMemoryBus.ROUTER.send( target, message, resultHandler );

        if (this.routerTracker != null) {
            this.routerTracker.withAllAvailableServices( ( apsBusRouter, args ) ->
                    apsBusRouter.send( target, message, resultHandler ) );
        }
    }

    /**
     * Subscribes to messages to a target.
     *
     * @param id             A unique ID to associate subscription with. Also used to unsubscribe.
     * @param target         The target to subscribe to.
     * @param messageHandler The handler to call with messages sent to target.
     */
    void subscribe( @NotNull ID id, @NotNull String target, @Optional @Nullable APSHandler<APSResult> resultHandler,
                    @NotNull APSHandler<Map<String, Object>> messageHandler ) {

        APSLocalInMemoryBus.ROUTER.subscribe( id, target, resultHandler, messageHandler );

        if (this.routerTracker != null) {
            this.routerTracker.withAllAvailableServices( ( apsBusRouter, args ) ->
                    apsBusRouter.subscribe( id, target, resultHandler, messageHandler ) );
        }
    }

    /**
     * Releases a subscription.
     *
     * @param subscriberId The ID returned by subscribe.
     */
    void unsubscribe( @NotNull ID subscriberId ) {

        APSLocalInMemoryBus.ROUTER.unsubscribe( subscriberId );

        if (this.routerTracker != null) {
            this.routerTracker.withAllAvailableServices( ( apsBusRouter, args ) ->
                    apsBusRouter.unsubscribe( subscriberId ) );
        }
    }

}
