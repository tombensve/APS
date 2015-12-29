package se.natusoft.osgi.aps.api.net.util;

/**
 * Defines data with content type.
 */
public interface TypedData {

    /** Use this for unknown content type. */
    String UNKNOWN_CONTENT_TYPE = "UNKNOWN";

    /**
     * Sets the data content.
     *
     * @param content The value content to set.
     */
    void setContent(byte[] content);

    /**
     * Returns the data content.
     */
    byte[] getContent();

    /**
     * Sets the type of the content.
     *
     * @param contentType The type to set. Preferably use mime types like "application/json", etc.
     */
    void setContentType(String contentType);

    /**
     * Returns the type of the content.
     */
    String getContentType();

    /**
     * Utility implementation.
     */
    class Provider implements TypedData {
        //
        // Private Members
        //

        private byte[] content;
        private String contentType;

        //
        // Constructors
        //

        /**
         * Creates a new Provider.
         */
        public Provider() {}

        /**
         * Creates a new Provider.
         *
         * @param contentType The data type.
         */
        public Provider(String contentType) {
            this.contentType = contentType;
        }

        /**
         * Creates a new Provider.
         *
         * @param content The data content.
         * @param contentType The data type.
         */
        public Provider(byte[] content, String contentType) {
            this.content = content;
            this.contentType = contentType;
        }

        //
        // Methods
        //

        /**
         * Sets the data content.
         *
         * @param content The data content to set.
         */
        @Override
        public void setContent(byte[] content) {
            this.content = content;
        }

        /**
         * Returns the data content.
         */
        @Override
        public byte[] getContent() {
            return this.content;
        }

        /**
         * Sets the type of the data content.
         *
         * @param contentType The type to set. Preferably use mime types.
         */
        @Override
        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        /**
         * Returns the type of the data content.
         */
        @Override
        public String getContentType() {
            return this.contentType;
        }
    }
}
