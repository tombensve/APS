package se.natusoft.osgi.aps.json.tools;

import se.natusoft.osgi.aps.json.JSONErrorHandler;
import se.natusoft.osgi.aps.exceptions.APSIOException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

/**
 * Utility implementation of JSONErrorHandler.
 */
public class CollectingErrorHandler implements JSONErrorHandler {
    //
    // Private Members
    //

    private List<String> messages = new LinkedList<>();

    private boolean printWarnings = false;

    //
    // Constructors
    //

    public CollectingErrorHandler() {}

    /**
     * @param printWarnings If true warnings will be printed to stderr.
     */
    public CollectingErrorHandler(boolean printWarnings) {
        this.printWarnings = printWarnings;
    }

    //
    // Methods
    //

    /**
     * Warns about something.
     *
     * @param message The warning message.
     */
    @Override
    public void warning(String message) {
        this.messages.add(message);
        if (this.printWarnings) {
            System.err.println(message);
        }
    }

    /**
     * Indicate failure.
     *
     * @param message The failure message.
     * @param cause   The cause of the failure. Can be null!
     * @throws RuntimeException This method must throw a RuntimeException.
     */
    @Override
    public void fail(String message, Throwable cause) throws RuntimeException {
        StringBuilder sb = new StringBuilder();
        sb.append("Message:\n");
        sb.append(message);
        sb.append("\nException:\n");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        cause.printStackTrace(pw);
        pw.flush();
        sb.append(sw.toString());
        this.messages.add(sb.toString());

        throw new APSIOException(message, cause);
    }

    /**
     * @return true if there are any messages.
     */
    public boolean hasMessages() {
        return !this.messages.isEmpty();
    }

    /**
     * @return All messages as one string.
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String message : messages) {
            sb.append(message);
            sb.append("\n");
        }
        return sb.toString();
    }
}
