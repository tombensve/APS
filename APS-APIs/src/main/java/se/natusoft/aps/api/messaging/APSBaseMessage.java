package se.natusoft.aps.api.messaging;

import se.natusoft.docutations.Optional;
import se.natusoft.aps.exceptions.APSValidationException;

import java.util.Map;

/**
 * This represents a common base for all messages. aps-core-lib provides an implementation of this.
 *
 * A message structure will as a minimum look like this:
 *
 *     {
 *         aps: {
 *             type: "type"  // Identifies what is in the message under 'content'.
 *             version: n.n, // The version of the message. For forwards/backwards compatibility.
 *         },
 *         content: {
 *             ...
 *         }
 *     }
 */
public interface APSBaseMessage extends Map<String, Object> {

    /**
     * Validates the content of the message.
     */
    void validate() throws APSValidationException;

    /**
     * @return the 'aps.version' value.
     */
    float getApsVersion();

    /**
     * Sets the 'aps.version' value.
     *
     * @param version The version value to set.
     */
    void setApsVersion(float version);

    /**
     * @return The 'aps.from' value.
     */
    @Optional
    String getApsFrom();

    /**
     * Sets the 'aps.from' value.
     *
     * @param from The from value to set.
     */
    @Optional
    void setApsFrom(String from);

    /**
     * @return the 'aps.type' id.
     */
    String getApsType();

    /**
     * Sets the 'aps.type' id
     *
     * @param type The type to set.
     */
    void setApsType(String type);

    /**
     * @return the 'aps' object.
     */
    Map<String, Object> getAps();

    /**
     * @return the 'content' object.
     */
    Map<String, Object> getContent();
}
