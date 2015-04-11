package se.natusoft.osgi.aps.api.net.tcpip;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This represents a TCP client request.
 */
public interface TCPRequest {

    /**
     * This is a callback that is used to perform a TCP request.
     *
     * @param requestStream The stream to write request data on.
     * @param responseStream The stream to read response data from.
     *
     * @throws IOException The one and only!
     */
    void tcpRequest(OutputStream requestStream, InputStream responseStream) throws IOException;
}
