package se.natusoft.osgi.aps.api.messaging;

import se.natusoft.docutations.NotNull;
import se.natusoft.docutations.Nullable;
import se.natusoft.docutations.Optional;
import se.natusoft.docutations.Reactive;
import se.natusoft.osgi.aps.types.APSHandler;
import se.natusoft.osgi.aps.types.APSResult;
import se.natusoft.osgi.aps.types.ID;

import java.util.Map;

/**
 * This should be implemented and published as a service for different messaging
 * solutions.
 *
 * Note that there are different kind of buses with different purposes. This API tries to,
 * as generically as possible support any.  No matter what the purpose of the bus are, they
 * tend to work more or less the same, but some are persistent, and some are not. For those
 * that are persistent the request() method might not make sense. It is optional to support
 * that. If not supported, return an APSResult with success set to false and an Exception
 * with an appropriate message.
 *
 * The goal here is to have an as trivial as possible API for any bus. But this also creates
 * limitations! For example, this only allows JSON messages! Nothing else is allowed. As long
 * as you are in control of all parts communicating that should be less of a problem. If you are
 * not, well then this might not be the API to use.
 *
 * Also note that when implementing this API the target should be used to determine if the
 * message is for this implementation to handle! APSBus will pass messages to **ALL** bus
 * routers! This does not mean that **ALL** should handle / send the message! The APSLocalInMemoryBus
 * (implements this interface) for example reacts on targets starting with "local:".
 */
public interface APSBusRouter {

    /**
     * Sends a message.
     *
     * @param target The target to send to. How to interpret this is up to implementation.
     * @param message The message to send. Only JSON structures allowed and top level has to be an object.
     * @param resultHandler The handler to call with result of operation. Can be null!
     */
    @Reactive
    void send( @NotNull String target, @NotNull Map<String, Object> message,
               @Optional @Nullable APSHandler<APSResult<Void>> resultHandler );

    /**
     * Subscribes to messages to a target.
     *
     * @param id A unique ID to associate subscription with. Also used to unsubscribe.
     * @param target The target to subscribe to.
     * @param resultHandler The result of the subscription.
     * @param messageHandler The handler to call with messages sent to target.
     */
    @Reactive
    void subscribe( @NotNull ID id, @NotNull String target, @Optional @Nullable APSHandler<APSResult> resultHandler,
                    @NotNull APSHandler<Map<String, Object>> messageHandler );

    /**
     * Releases a subscription.
     *
     * @param subscriberId The ID returned by subscribe.
     */
    @Reactive
    void unsubscribe( @NotNull ID subscriberId );

}
