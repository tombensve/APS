package se.natusoft.osgi.aps.tools.debug;

import java.io.IOException;
import java.net.Socket;

/**
 * This class connects to an IP address and a  port and sends all output to that destination.
 *
 * This is intended to be used in conjunction with nc (net cat) like: nc -k -l localhost 10999
 */
public class NCLogger {

    //
    // Private Members
    //

    private String host;

    private int port;

    //
    // Constructors
    //

    /**
     * Creates a new NCLogger instance.
     *
     * @param host The host to write to.
     * @param port The port to write to.
     */
    public NCLogger(String host, int port) {
        this.host = host;
        this.port = port;
    }

    //
    // Methods
    //

    /**
     * Logs a line of text.
     *
     * @param text The text to log.
     */
    public void log(String text) {
        try {
            Socket socket = new Socket(this.host, this.port);
            socket.setSoTimeout(2000);
            socket.getOutputStream().write(text.getBytes());
            socket.getOutputStream().write("\n".getBytes());
            socket.close();
        }
        catch (IOException ignore) {}
    }

}
