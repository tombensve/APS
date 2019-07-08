package se.natusoft.osgi.aps.net.vertx

import io.vertx.core.eventbus.EventBus
import se.natusoft.docutations.NotNull
import se.natusoft.docutations.Nullable
import se.natusoft.docutations.Optional
import se.natusoft.osgi.aps.api.messaging.APSBusRouter
import se.natusoft.osgi.aps.types.APSHandler
import se.natusoft.osgi.aps.types.APSResult
import se.natusoft.osgi.aps.types.ID

class APSVertxBusRouter implements APSBusRouter {

    //
    // Groovy JB Properties
    //

    EventBus eventBus

    //
    // Methods
    //

    /**
     * Sends a message.
     *
     * @param target The target to send to. How to interpret this is up to implementation.
     * @param message The message to send. Only JSON structures allowed and top level has to be an object.
     * @param resultHandler The handler to call with result of operation. Can be null!
     */
    @Override
    void send( @NotNull String target, @NotNull Map<String, Object> message, @Optional @Nullable APSHandler<APSResult<Void>> resultHandler ) {
        this.eventBus.sender(  )
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

    }

    /**
     * Releases a subscription.
     *
     * @param subscriberId The ID returned by subscribe.
     */
    @Override
    void unsubscribe( @NotNull ID subscriberId ) {

    }
}
