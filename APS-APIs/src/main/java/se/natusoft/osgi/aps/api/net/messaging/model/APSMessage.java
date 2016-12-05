package se.natusoft.osgi.aps.api.net.messaging.model;

import se.natusoft.docutations.NotNull;
import se.natusoft.docutations.Nullable;
import se.natusoft.osgi.aps.exceptions.APSIOException;

import java.io.*;
import java.util.UUID;

/**
 * This represents a basic message containing bytes.
 */
public interface APSMessage {

    /**
     * @return The message id.
     */
    @NotNull
    UUID getId();

    /**
     * @return true if this message is a reply.
     */
    boolean isReply();

    /**
     * @return The message id this message is a reply to if isReply() is true. Otherwise null is returned.
     */
    @Nullable
    UUID getReplyToId();

    /**
     * Sets the message bytes.
     *
     * @param bytes The bytes to set.
     */
    void setMessage(@NotNull byte[] bytes);

    /**
     * @return The message bytes.
     */
    @Nullable
    byte[] getMessage();

    /**
     * Provides a hint about the content of the message. This can be anything and is between message produces and message consumer
     * to agree on.
     *
     * Note that depending on transport, protocol, etc it is not a given that this information can be passed on at all!
     *
     * @param contentHint The hint to set.
     */
    void setContentHint(@Nullable String contentHint);

    /**
     * @return The content hint if any or null.
     */
    @Nullable
    String getContentHint();

    /**
     * Returns a stream to which bytes can be written. Closing the stream will set the contents as bytes.
     */
    @NotNull
    OutputStream getMessageWriteStream();

    /**
     * @return a stream from which the byte content of the message can be read.
     */
    @NotNull
    InputStream getMessageReadStream();

    /**
     * @return The source of the message if available. Can be null.
     */
    @Nullable
    String getSource();

    /**
     * Sets the source of the message.
     *
     * @param source The source to set.
     */
    void setSource(@Nullable String source);

    //
    // Inner Classes
    //

    /**
     * Provides a default implementation of Message.
     */
    class Provider implements APSMessage {

        //
        // Private Members
        //

        /** The id of this message. */
        private UUID id = UUID.randomUUID();

        /** If set the id of the message this is a reply to. */
        private UUID replyToId = null;

        /** The current message. */
        private byte[] message;

        /** The source of the message. */
        private String source = "<Unknown>";

        /** For providing message bytes via a stream. */
        private ByteArrayOutputStream messageWriteStream = null;

        /** A possible hint about the content of the message. */
        private String contentHint;

        //
        // Constructors
        //

        /**
         * Creates a new Provider instance.
         */
        public Provider() {}

        /**
         * Creates a new Provider instance.
         *
         * @param message The message data.
         */
        public Provider(byte[] message) {
            this.message = message;
        }

        /**
         * Creates a new Provider instance.
         *
         * @param message The message data.
         * @param replyToId The id of the message this message is a reply to.
         */
        public Provider(byte[] message, UUID replyToId) {
            this.message = message;
            this.replyToId = replyToId;
        }

        //
        // Methods
        //

        /**
         * @return The message id.
         */
        public UUID getId() {
            return this.id;
        }

        /**
         * @return true if this message is a reply.
         */
        public boolean isReply() {
            return this.replyToId != null;
        }

        /**
         * Sets the id of the message this message is a reply to.
         *
         * @param replyToId The id to set.
         */
        public void setReplyToId(UUID replyToId) {
            this.replyToId = replyToId;
        }

        /**
         * @return The message id this message is a reply to if isReply() is true. Otherwise null is returned.
         */
        @Nullable
        public UUID getReplyToId() {
            return this.replyToId;
        }


        /**
         * Sets the message bytes.
         *
         * @param bytes The bytes to set.
         */
        @Override
        public void setMessage(@NotNull byte[] bytes) {
            this.message = bytes;
            this.messageWriteStream = null;
        }

        /**
         * @return The message bytes.
         */
        @Override
        @NotNull
        public byte[] getMessage() {
            if (this.messageWriteStream != null) {
                try {
                    this.messageWriteStream.close();
                    this.message = this.messageWriteStream.toByteArray();
                }
                catch (IOException ioe) {
                    throw new APSIOException(ioe.getMessage(), ioe);
                }
            }
            return this.message;
        }

        /**
         * Provides a hint about the content of the message. This can be anything and is between message produces and message consumer
         * to agree on.
         *
         * Note that depending on transport, protocol, etc it is not a given that this information can be passed on at all!
         *
         * @param contentHint The hint to set.
         */
        @Override
        public void setContentHint(@Nullable String contentHint) {
            this.contentHint = contentHint;
        }

        /**
         * @return The content hint if any or null.
         */
        @Nullable
        public String getContentHint() {
            return this.contentHint;
        }

        /**
         * Returns a stream to which bytes can be written. Closing the stream will set the contents as bytes.
         */
        @Override
        public OutputStream getMessageWriteStream() {
            this.messageWriteStream = new ByteArrayOutputStream() {
                public void close() throws IOException {
                    super.close();
                    Provider.this.message = toByteArray();
                }
            };

            return this.messageWriteStream;
        }

        /**
         * @return a stream from which the byte content of the message can be read.
         */
        @Override
        public InputStream getMessageReadStream() {
            return new ByteArrayInputStream(this.message);
        }

        /**
         * @return The source of the message if available. Can be null.
         */
        @Override
        public String getSource() {
            return this.source;
        }

        /**
         * Sets the source of the message.
         *
         * @param source The source to set.
         */
        @Override
        public void setSource(@Nullable String source) {
            this.source = source;
        }

        /**
         * @return the message as a String.
         */
        @Override
        public String toString() {
            return this.message != null && this.message.length > 0 ? new String(this.message) : "";
        }
    }
}
