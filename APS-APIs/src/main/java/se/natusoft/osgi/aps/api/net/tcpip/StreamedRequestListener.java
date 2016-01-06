package se.natusoft.osgi.aps.api.net.tcpip;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

/**
 * This defines a listener of streamed requests.
 */
public interface StreamedRequestListener {

    /**
     * Listeners of requests should implement this.
     *
     * @param receivePoint The receive-point the listener was registered with.
     * @param requestStream This contains the request data. DO NOT CLOSE THIS STREAM!
     * @param responseStream If receive-point is marked as async then this will be null, otherwise a
     *                       response should be written to this. DO NOT CLOSE THIS STREAM.
     */
    void requestReceived(URI receivePoint, InputStream requestStream, OutputStream responseStream) throws IOException;
}
