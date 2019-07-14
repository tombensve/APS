package se.natusoft.osgi.aps.net.vertx

import io.vertx.core.eventbus.EventBus
import se.natusoft.docutations.NotNull
import se.natusoft.docutations.Nullable
import se.natusoft.docutations.Optional
import se.natusoft.osgi.aps.api.messaging.APSBusRouter
import se.natusoft.osgi.aps.exceptions.APSValidationException
import se.natusoft.osgi.aps.types.APSHandler
import se.natusoft.osgi.aps.types.APSResult
import se.natusoft.osgi.aps.types.ID

class APSVertxBusRouter implements APSBusRouter {

    private static final FILTER = " cluster:"

    //
    // Groovy JB Properties
    //

    EventBus eventBus

    //
    // Methods
    //

    private boolean validTarget( String target, APSHandler<APSResult> resultHandler, Closure go) {
        if ( target.startsWith( FILTER ) ) {
            target = target.substring( FILTER.length(  ) )

            go.call( target )
        }
        else {
            resultHandler.handle( APSResult.failure( new APSValidationException( "'target' must start with ${ FILTER } !" ) ) )
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

        validTarget( target, resultHandler ) { String realTarget ->

            this.eventBus.sender(realTarget).send(  )

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
