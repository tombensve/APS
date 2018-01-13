package se.natusoft.osgi.aps.api.pubsub;

import se.natusoft.osgi.aps.api.reactive.APSValue;
import se.natusoft.osgi.aps.api.reactive.APSHandler;

/**
 * This extension of APSSender is to support messaging providers that allows for a
 * reply to a specific message. The only such I know of is Vertx, but there might be
 * more.
 *
 * Since APS at the moment makes heavy use of Vertx and that I still want to be able
 * to encapsulate it with APS generic APIs I decided to handle this feature of Vertx
 * like this.
 *
 * Note that I have allows for 2 different types of send and reply message. Since all
 * messages in APS will be JSON or a Map&lt;String, Object&gt; representation of JSON
 * the send and reply types will be the same. I dit not however want to force it in
 * the API.
 *
 * @param <Message> The message type to send.
 * @param <ReplyMessage> The expected typeof reply message.
 */
public interface APSReplyableSender<Message, ReplyMessage> extends APSSender<Message> {

    /**
     * This must be called before send(...). send will use the last supplied reply subscriber.
     *
     * @param handler the handler of the reply.
     */
    @SuppressWarnings("unused")
    APSSender<Message> replyTo(APSHandler<APSValue<ReplyMessage>> handler);

}
