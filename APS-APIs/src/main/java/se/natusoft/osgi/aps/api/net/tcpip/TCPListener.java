package se.natusoft.osgi.aps.api.net.tcpip;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;

/**
 * A Listener for TCP traffic.
 */
public interface TCPListener {

    /**
     * Receives a TCP request stream.
     *
     * @param name The name of a configuration specifying address and port that the request comes from.
     * @param address The address of the request.
     * @param tcpReqStream The request stream.
     * @param tcpRespStream The response stream.
     *
     * @throws IOException
     */
    void tcpRequestReceived(String name, InetAddress address, InputStream tcpReqStream, OutputStream tcpRespStream) throws IOException;
}
