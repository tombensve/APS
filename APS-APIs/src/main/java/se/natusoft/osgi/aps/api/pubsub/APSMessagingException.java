package se.natusoft.osgi.aps.api.pubsub;

/**
 * Utility if you don't think the APSPubSubException is clear enough.
 */
public class APSMessagingException extends APSPubSubException {

    public APSMessagingException(String message) {
        super(message);
    }

    public APSMessagingException(String message, Throwable cause) {
        super(message, cause);
    }
}
