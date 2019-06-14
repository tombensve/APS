package se.natusoft.osgi.aps.api.messaging;

import org.osgi.framework.BundleContext;
import se.natusoft.docutations.NotNull;
import se.natusoft.docutations.Nullable;
import se.natusoft.docutations.Optional;
import se.natusoft.osgi.aps.tracker.APSServiceTracker;
import se.natusoft.osgi.aps.tracker.WithService;
import se.natusoft.osgi.aps.types.APSHandler;
import se.natusoft.osgi.aps.types.APSResult;
import se.natusoft.osgi.aps.types.ID;

import java.util.Map;

/**
 * This is a simple bus API that is used by creating an instance and passing a BundleContext.
 * <p>
 * All calls will be passed to all the APSBusRouter implementations tracked. This means that if
 * no such implementations are deployed then this will do absolutely nothing!
 */
public class APSBus {

    //
    // Private Members
    //

    /** The currently known bus routers */
    private APSServiceTracker<APSBusRouter> routerTracker = null;

    /** The bundle context of using bundle. */
    private BundleContext bundleContext = null;

    //
    // Constructors
    //

    /**
     * Creates a new APSBus tracking all deployed bus routers.
     *
     * @param bundleContext The bundle context of the caller.
     */
    public APSBus( @NotNull BundleContext bundleContext ) {
        this.bundleContext = bundleContext;
        this.routerTracker = new APSServiceTracker<>( this.bundleContext, APSBusRouter.class );
    }

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
    void send( @NotNull String target, @NotNull Map<String, Object> message, @Optional @Nullable APSHandler<APSResult<Void>> resultHandler ) {

        this.routerTracker.withAllAvailableServices( new WithService<APSBusRouter>() {
            @Override
            public void withService( APSBusRouter apsBusRouter, Object... args ) {
                apsBusRouter.send( target, message, resultHandler );
            }
        } );
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

        this.routerTracker.withAllAvailableServices( new WithService<APSBusRouter>() {
            @Override
            public void withService( APSBusRouter apsBusRouter, Object... args ) {
                apsBusRouter.subscribe( id, target, resultHandler, messageHandler );
            }
        } );
    }

    /**
     * Releases a subscription.
     *
     * @param subscriberId The ID returned by subscribe.
     */
    void unsubscribe( @NotNull ID subscriberId ) {
        this.routerTracker.withAllAvailableServices( new WithService<APSBusRouter>() {
            @Override
            public void withService( APSBusRouter apsBusRouter, Object... args ) throws Exception {
                apsBusRouter.unsubscribe( subscriberId );
            }
        } );
    }

}
