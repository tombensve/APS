package se.natusoft.osgi.aps.api.messaging;

import se.natusoft.docutations.NotNull;
import se.natusoft.osgi.aps.model.APSValue;
import se.natusoft.osgi.aps.model.ID;

/**
 * This is a message container that wrap actual received messages. This to support reply-ability
 * per message. This in turn is due to that is how the Vert.x eventbus works. This supports that
 * and other. To support miscellaneous messaging apis the received messages must be wrapped.
 *
 * @param <Message> The type of the content.
 */
@SuppressWarnings("unused")
public interface APSMessage<Message> extends APSValue<Message> {

    /**
     * Replies to message.
     *
     * @param reply The message to reply with.
     */
    default void reply(@NotNull Message reply) {
        throw new APSMessagingException( "This message cannot be replied to!" );
    }

    /**
     * Provides a simple default implementation. This should probably be extended.
     *
     * @param <Message> The type of the content.
     */
    class Provider<Message> extends APSValue.Provider<Message> implements APSMessage<Message> {

        /**
         * Creates a new Provider instance.
         *
         * @param content The message content.
         */
        public Provider(Message content) {
            super(content);
        }

    }
}
