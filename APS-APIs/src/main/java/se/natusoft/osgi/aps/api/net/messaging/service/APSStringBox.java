package se.natusoft.osgi.aps.api.net.messaging.service;

/**
 * This extends APSBox with a String box.
 */
public interface APSStringBox extends APSBox {

    /**
     * Set content as a String.
     *
     * @param content The string to set.
     */
    void setStringContent(String content);

    /**
     * Gets the content as a String.
     */
    String getStringContent();

    //
    // Inner Classes
    //

    /**
     * Factory API for creating APSStringBox instances.
     */
    interface APSStringBoxFactory extends APSBoxFactory<APSStringBox> {

        /**
         * Creates a new APSStringBox with a content String.
         *
         * @param content The content string to set.
         */
        APSStringBox createStringBox(String content);
    }

    /**
     * An APSStringBoxFactory implementation creating APSStringBoxDefaultProvider instances.
     */
    class APSStringBoxDefaultProviderFactory implements APSStringBoxFactory {

        /**
         * Creates a new APSStringBox with a content String.
         *
         * @param content The content string to set.
         */
        @Override
        public APSStringBox createStringBox(String content) {
            APSStringBox box = createBox();
            box.setStringContent(content);
            return box;
        }

        /**
         * Returns a new APSBox without content.
         */
        @Override
        public APSStringBox createBox() {
            return new APSStringBoxDefaultProvider();
        }

        /**
         * Returns a new APSBox with content.
         *
         * @param content The content of the box to create.
         */
        @Override
        public APSStringBox createBox(byte[] content) {
            APSStringBox box = createBox();
            box.setContent(content);
            return box;
        }
    }

    /**
     * Provides a default implementation of APSStringBox.
     */
    class APSStringBoxDefaultProvider extends APSBoxDefaultProvider implements APSStringBox {

        /**
         * Set content as a String.
         *
         * @param content The string to set.
         */
        @Override
        public void setStringContent(String content) {
            setContent(content.getBytes());
        }

        /**
         * Gets the content as a String.
         */
        @Override
        public String getStringContent() {
            return new String(getContent());
        }
    }
}
