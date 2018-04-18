package se.natusoft.osgi.aps.api.messaging;

import se.natusoft.docutations.NotNull;
import se.natusoft.docutations.Nullable;
import se.natusoft.osgi.aps.model.APSValue;

import java.util.Map;

/**
 * This is a message container to wrap actual messages, which makes it easier to be compatible
 * with different messaging solutions. Only received messages are wrapped with this rather than
 * APSValue.
 *
 * @param <Message> The type of the content.
 */
@SuppressWarnings("unused")
public interface APSMessage<Message> extends APSValue<Message> {

    /**
     * @return the message content.
     */
    @NotNull
    @Override
    Message content();

    /**
     * @return true if the message is replyable.
     */
    boolean isReplyable();

    /**
     * Replies to message. Will throw APSUnsupportedException if isReplyable() returns false.
     *
     * @param reply The message to reply with.
     */
    void reply(@NotNull Message reply);

    /**
     * Provides a simple default implementation. This should probably be extended.
     *
     * @param <Message> The type of the content.
     */
    class Provider<Message> implements APSMessage<Message> {

        private Message content;
        private boolean replyable = false;

        public Provider(Message content) {
            this.content = content;
        }

        public Provider(Message content, boolean replyable) {
            this.content = content;
            this.replyable = replyable;
        }

        /**
         * @return the message content.
         */
        @Override
        public Message content() {
            return this.content;
        }

        /**
         * @return true if the message is replyable.
         */
        @Override
        public boolean isReplyable() {
            return this.replyable;
        }

        /**
         * Replies to message. This must be overridden to use.
         *
         * @param reply The message to reply with.
         */
        @Override
        public void reply(Message reply) {
            throw new UnsupportedOperationException("reply(Message reply) is not implemented by this provider!");
        }
    }
}
