package se.natusoft.osgi.aps.api.pubcon;

import java.util.Map;

/**
 * This is a pure utility to wrap some content and provide meta data with it.
 *
 * @param <Content> The content type.
 */
public class APSMessage<Content> {

    //
    // Private Members
    //

    private Map<String, String> meta;
    private Content content;

    //
    // Constructors
    //

    /**
     * Default constructor, requiring setters to provide content.
     */
    public APSMessage() {}

    /**
     * Creates a new APSMessage instance.
     *
     * @param content The message content.
     * @param meta Message meta data.
     */
    public APSMessage(Content content, Map<String, String> meta) {
        this.setContent(content);
        this.setMeta(meta);
    }

    /**
     * Creates a new APSMessage instance.
     *
     * @param content The message content.
     */
    public APSMessage(Content content) {
        this.setContent(content);
    }

    //
    // Methods
    //

    /**
     * @return The meta data.
     */
    public Map<String, String> getMeta() {
        return meta;
    }

    /**
     * Sets meta data.
     *
     * @param meta The meta data to set.
     */
    public void setMeta(Map<String, String> meta) {
        this.meta = meta;
    }

    /**
     * @return The message content.
     */
    public Content getContent() {
        return content;
    }

    /**
     * Sets the content.
     *
     * @param content The content to set.
     */
    public void setContent(Content content) {
        this.content = content;
    }

}
