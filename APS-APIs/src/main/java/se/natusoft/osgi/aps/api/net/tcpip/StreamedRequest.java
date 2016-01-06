package se.natusoft.osgi.aps.api.net.tcpip;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

/**
 * This is used to make a streamed request.
 */
public interface StreamedRequest {

    /**
     * This must be implemented by clients that want to make a request.
     *
     * @param sendPoint This is only received as information. It is the send-point that will be sent to.
     * @param requestStream All data to send in the request should be written to this stream. DO NOT CLOSE THE STREAM!
     * @param responseStream If the send-point is async then this will be null otherwise a response to the request can
     *                       be read from this stream.
     */
    void sendRequest(URI sendPoint, OutputStream requestStream, InputStream responseStream) throws IOException;
}
