package se.natusoft.osgi.aps.api.pubsub;

public interface APSReplyableSender<Message, ReplyMessage> extends APSSender<Message> {

    /**
     * This must be called before send(...). send will use the last supplied reply subscriber.
     *
     * @param reply the subscriber to receive reply.
     */
    APSSender<Message> replyTo(APSSubscriber<ReplyMessage> reply);

}
