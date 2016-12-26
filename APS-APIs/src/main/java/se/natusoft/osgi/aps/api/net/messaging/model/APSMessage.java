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
     * Returns a stream to which bytes can be written. Closing the stream will set the contents as bytes.
     */
    @NotNull
    OutputStream getMessageWriteStream();

    /**
     * @return a stream from which the byte content of the message can be read.
     */
    @NotNull
    InputStream getMessageReadStream();

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

        /** The current message. */
        private byte[] message;

        /** For providing message bytes via a stream. */
        private ByteArrayOutputStream messageWriteStream = null;

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

        //
        // Methods
        //

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
         * @return the message as a String.
         */
        @Override
        public String toString() {
            return this.message != null && this.message.length > 0 ? new String(this.message) : "";
        }
    }
}
