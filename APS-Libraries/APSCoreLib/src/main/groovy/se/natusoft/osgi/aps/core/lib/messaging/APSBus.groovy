package se.natusoft.osgi.aps.core.lib.messaging

import se.natusoft.docutations.NotNull
import se.natusoft.docutations.Nullable
import se.natusoft.docutations.Optional
import se.natusoft.docutations.Reactive
import se.natusoft.osgi.aps.types.APSHandler
import se.natusoft.osgi.aps.types.APSResult
import se.natusoft.osgi.aps.types.ID

interface APSBus {

    /**
     * Sends a message.
     *
     * @param target The target to send to. Note that for send you can give multiple, comma separated
     *        targets to send same message to multiple places.
     * @param message The message to send. Only JSON structures allowed and top level has to be an object.
     * @param resultHandler Receives the success or failure of the call.
     */
    @Reactive
    void send( @NotNull String target, @NotNull Map<String, Object> message,
               @Optional @Nullable APSHandler<APSResult> resultHandler )

    /**
     * Subscribes to messages to a target.
     *
     * @param id A unique ID to associate subscription with. Also used to unsubscribe.
     * @param target The target to subscribe to.
     * @param messageHandler The handler to call with messages sent to target.
     */
    @Reactive
    void subscribe( @NotNull ID id, @NotNull String target, @Optional @Nullable APSHandler<APSResult> resultHandler,
                    @NotNull APSHandler<Map<String, Object>> messageHandler )

    /**
     * Releases a subscription.
     *
     * @param subscriberId The ID returned by subscribe.
     */
    @Reactive
    void unsubscribe( @NotNull ID subscriberId )

    /**
     * Sends a message and expects to get a response message back.
     *
     * This is not forwarded to a APSBusRouter! This is locally implemented
     * and does the following:
     *
     * - Generates a unique reply address.
     * - Subscribes to address.
     *   - After reply message is received and forwarded to handler, the
     *     message subscription is unsubscribed.
     * - Updates message header.replyAddress with address
     * - Sends message.
     *
     * This should theoretically work for any APSBusRouter implementation. For
     * some it might not make sense however.
     *
     * @param target The target to send to.
     * @param message The message to send.
     * @param resultHandler optional handler to receive result of send.
     * @param responseMessage A message that is a response of the sent message.
     */
    @Reactive
    void request( @NotNull String target, @NotNull Map<String, Object> message,
                  @Nullable @Optional APSHandler<APSResult> resultHandler,
                  @NotNull APSHandler<Map<String, Object>> responseMessage )
}