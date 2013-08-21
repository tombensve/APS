package se.natusoft.apsgroups.internal.net;

import java.io.IOException;

/**
 * A transport that works over TCP.
 */
public class TCPTransport implements Transport {

    /**
     * Sends the byte of data to the destination(s).
     *
     * @param data The data to send.
     * @throws java.io.IOException on failure to send.
     */
    @Override
    public void send(byte[] data) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Receives data.
     *
     * @throws java.io.IOException
     */
    @Override
    public Packet receive() throws IOException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Sets up the transport.
     *
     * @throws java.net.UnknownHostException
     * @throws java.net.SocketException
     * @throws java.io.IOException
     */
    @Override
    public void open() throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Closes this transport.
     *
     * @throws java.io.IOException
     */
    @Override
    public void close() throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
