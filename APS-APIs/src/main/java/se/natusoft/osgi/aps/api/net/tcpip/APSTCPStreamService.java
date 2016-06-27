package se.natusoft.osgi.aps.api.net.tcpip;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Defines a very simple TCP API with named, configured destinations.
 */
public interface APSTCPStreamService {

    /**
     * Provides the streams for writing and reading a TCP connection.
     */
    interface TCPConnection {
        /**
         * @return For writing to the TCP socket.
         */
        OutputStream getOutputStream() throws IOException;

        /**
         * @return For reading the TCP socket.
         */
        InputStream getInputStream() throws IOException;

        void close() throws IOException;
    }

    /**
     * Creates a TCP connection that can be written and read from. The destination of the connection is provided
     * by a named configuration.
     *
     * @param name The name of the TCP destination to connect to.
     *
     * @return A new TCPConnection if successful.
     *
     * @throws IOException if unsuccessful.
     */
    TCPConnection connect(String name) throws IOException;
}
