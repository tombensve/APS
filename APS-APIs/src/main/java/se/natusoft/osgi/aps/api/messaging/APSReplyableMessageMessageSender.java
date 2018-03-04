package se.natusoft.osgi.aps.api.messaging;

import se.natusoft.osgi.aps.model.APSHandler;

/**
 * This extension of APSSender is to support messaging providers that allows for a
 * reply to a specific message. The only such I know of is Vertx, but there might be
 * more.
 *
 * Since APS at the moment makes heavy use of Vertx and that I still want to be able
 * to encapsulate it with APS generic APIs I decided to handle this feature of Vertx
 * like this.
 *
 * Note that I have allowed for 2 different types of send and reply message. Since all
 * messages in APS will be JSON or a Map&lt;String, Object&gt; representation of JSON
 * the send and reply types will be the same. I dit not however want to force it in
 * the API.
 *
 * @param <Message> The message type to send.
 * @param <ReplyMessage> The expected typeof reply message.
 */
public interface APSReplyableMessageMessageSender<Message, ReplyMessage> extends APSMessageSender<Message> {

    /**
     * This must be called before send(...). send will use the last supplied reply subscriber.
     *
     * Note that this uses a fluent API that returns this. This allows for just adding
     * ".send(...)" after the call to this.
     *
     * @param handler the handler of the reply.
     */
    @SuppressWarnings("unused")
    APSMessageSender<Message> replyTo(APSHandler<APSMessage<ReplyMessage>> handler);

}
