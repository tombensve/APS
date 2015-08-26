package se.natusoft.osgi.aps.api.net.util;

import java.io.*;

/**
 * This represents a generic container for putting data in. This can be extended for specific types of boxes.
 * See APSJSONBox for an example.
 */
public interface APSBox {

    /**
     * Set the content bytes of the box.
     *
     * @param content The content bytes to set.
     */
    void setContent(byte[] content);

    /**
     * Returns the content bytes of this box.
     */
    byte[] getContent();

    /**
     * Returns an OutputStream on which to write to the box.
     */
    OutputStream getContentOutputStream();

    /**
     * Returns an InputStream from which to read from the box.
     */
    InputStream getContentInputStream();

    /**
     * Returns this size of the box.
     */
    int getSize();

    /**
     * Included since interfaces does not inherit toString().
     */
    String toString();

    //
    // Inner Classes
    //

    /**
     * This defines a factory for creating APSBox:es.
     */
    interface APSBoxFactory<BoxType> {

        /**
         * Returns a new APSBox without content.
         */
        BoxType createBox();

        /**
         * Returns a new APSBox with content.
         *
         * @param content The content of the box to create.
         */
        BoxType createBox(byte[] content);
    }

    /**
     * A factory for creating default APSBox implementation.
     */
    class APSBoxDefaultProviderFactory implements APSBoxFactory<APSBox> {

        /**
         * Returns a new APSBox without content.
         */
        @Override
        public APSBox createBox() {
            return new APSBoxDefaultProvider();
        }

        /**
         * Returns a new APSBox with content.
         *
         * @param content The content of the box to create.
         */
        @Override
        public APSBox createBox(byte[] content) {
            APSBox apsBox = createBox();
            apsBox.setContent(content);
            return apsBox;
        }
    }

    /**
     * This provides a usable, but non required implementation of this interface.
     */
    class APSBoxDefaultProvider implements APSBox {

        //
        // Private Members
        //

        private byte[] content = new byte[0];

        //
        // Constructors
        //

        /**
         * Default constructors.
         */
        public APSBoxDefaultProvider() {}

        //
        // Methods
        //

        /**
         * Set the content bytes of the box.
         *
         * @param content The content bytes to set.
         */
        @Override
        public void setContent(byte[] content) {
            this.content = content;
        }

        /**
         * Returns the content bytes of this box.
         */
        @Override
        public byte[] getContent() {
            return this.content;
        }

        /**
         * Returns an OutputStream on which to write to the box.
         */
        @Override
        public OutputStream getContentOutputStream() {
            return new ByteArrayOutputStream() {
                @Override
                public void close() throws IOException {
                    APSBoxDefaultProvider.this.content = getContent();
                }
            };
        }

        /**
         * Returns an InputStream from which to read from the box.
         */
        @Override
        public InputStream getContentInputStream() {
            return new ByteArrayInputStream(this.content);
        }

        /**
         * Returns this size of the box.
         */
        @Override
        public int getSize() {
            return this.content.length;
        }
    }
}
