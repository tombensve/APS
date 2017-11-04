package se.natusoft.osgi.aps.api.pubcon;

/**
 * Utility if you don't think the APSPubConException is clear enough.
 */
public class APSMessagingException extends APSPubConException {

    public APSMessagingException(String message) {
        super(message);
    }

    public APSMessagingException(String message, Throwable cause) {
        super(message, cause);
    }
}
