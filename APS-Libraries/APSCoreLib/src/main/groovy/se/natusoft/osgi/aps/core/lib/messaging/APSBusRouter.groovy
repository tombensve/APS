package se.natusoft.osgi.aps.core.lib.messaging

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.docutations.NotNull
import se.natusoft.docutations.Nullable
import se.natusoft.docutations.Optional
import se.natusoft.docutations.Reactive
import se.natusoft.osgi.aps.types.APSHandler
import se.natusoft.osgi.aps.types.APSResult
import se.natusoft.osgi.aps.types.ID

/**
 * This should be implemented and published as a service for different messaging
 * solutions.
 *
 * Note that there are different kind of buses with different purposes. This API tries to,
 * as generically as possible support any.
 *
 * The goal here is to have an as trivial as possible API for any bus. But this also creates
 * limitations! For example, this only allows JSON messages! As long as you are in control of
 * all parts communicating that should be less of a problem. If you are not, well then this
 * might not be the API to use. The messaging APIs in APS-APIs are more general and more
 * flexible in its APIs than this.
 *
 * Also note that when implementing the APSBusRouter API the target should be used to determine if the
 * message is for this implementation to handle! APSBus will pass messages to **ALL** bus
 * routers! This does not mean that **ALL** should handle / send the message! The APSLocalInMemoryBus
 * (implements this interface) for example reacts on targets starting with "local:".
 *
 * The general format of target is 'id:address'. The APSTarget class supports that. The 'id'
 * part should be constant for each implementation. It identifies a specific implementation,
 * for example, the _APSLocal*InMemoryBus_ uses 'local' as id. The APSVertxBusRouter
 * (APSVertxProvider) uses 'cluster' as id. So users of APSBus can steer messages to different
 * buses by providing target specification using different target ids. This makes things very
 * easy.
 *
 * Note that it is fully legal & OK for an APSBusRouter implementation to react on multiple
 * target id:s! Even same id:s as other implementations react on. In that case one message
 * can be sent via different busses at the same time. I'm only pointing this out since it
 * is possible! In most cases it would not make sense to do so. But there might be a special
 * case where it would make sense.
 */
@CompileStatic
@TypeChecked
interface APSBusRouter {

    /**
     * Sends a message.
     *
     * @param target The target to send to. How to interpret this is up to implementation.
     * @param message The message to send. Only JSON structures allowed and top level has to
     * be an object.
     *
     * @param resultHandler The handler to call with result of operation. Can be null!
     */
    @Reactive
    void send( @NotNull String target, @NotNull Map<String, Object> message,
               @Optional @Nullable APSHandler<APSResult> resultHandler )

    /**
     * Subscribes to messages to a target.
     *
     * @param id A unique ID to associate subscription with. Also used to unsubscribe.
     * @param target The target to subscribe to.
     * @param resultHandler The result of the subscription.
     * @param messageHandler The handler to call with messages sent to target.
     */
    @Reactive
    void subscribe( @NotNull ID id, @NotNull String target,
                    @Optional @Nullable APSHandler<APSResult> resultHandler,
                    @NotNull APSHandler<Map<String, Object>> messageHandler )

    /**
     * Releases a subscription.
     *
     * @param subscriberId The ID returned by subscribe.
     */
    @Reactive
    void unsubscribe( @NotNull ID subscriberId )

}
