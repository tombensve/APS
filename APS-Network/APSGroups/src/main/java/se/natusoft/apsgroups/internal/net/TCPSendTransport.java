package se.natusoft.apsgroups.internal.net;

import se.natusoft.apsgroups.config.APSGroupsConfig;
import se.natusoft.apsgroups.logging.APSGroupsLogger;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;

/**
 * A transport for sending over TCP.
 */
public class TCPSendTransport implements Transport {
    //
    // Private Members
    //

    private APSGroupsConfig.TransportConfig config;

    private APSGroupsLogger logger;

    private boolean remoteNoAvailable = false;

    //
    // Constructors
    //

    /**
     * Creates a new TCPSendTransport.
     *
     * @param logger The logger to use.
     * @param config The config for this transport.
     */
    public TCPSendTransport(APSGroupsLogger logger, APSGroupsConfig.TransportConfig config) {
        this.logger = logger;
        this.config = config;
    }

    //
    // Methods
    //

    /**
     * Sets up the transport.
     *
     * @throws java.net.UnknownHostException
     * @throws java.net.SocketException
     * @throws java.io.IOException
     */
    @Override
    public void open() throws IOException {
        this.logger.info("Opening TCPSendTransport to host '" + this.config.getHost() + "' and port " +
            this.config.getPort() + "!");
    }

    /**
     * Closes this transport.
     *
     * @throws java.io.IOException
     */
    @Override
    public void close() throws IOException {
        this.logger.info("Closing TCPSendTransport to host '" + this.config.getHost() + "' and port " +
                this.config.getPort() + "!");
    }

    /**
     * Sends the byte of data to the destination(s).
     *
     * @param data The data to send.
     * @throws java.io.IOException on failure to send.
     */
    @Override
    public void send(byte[] data) throws IOException {
        try {
            Socket socket = new Socket(this.config.getHost(), this.config.getPort());
            socket.getOutputStream().write(data);
            socket.close();
            this.remoteNoAvailable = false;
        }
        catch (ConnectException ce) {
            if (!this.remoteNoAvailable) {
                this.remoteNoAvailable = true;
                this.logger.error("Remote is not yet available! Nothing has been sent.", ce);
            }
        }
    }

    /**
     * Receives data.
     *
     * @throws java.io.IOException
     */
    @Override
    public Packet receive() throws IOException {
        throw new UnsupportedOperationException("This is a sending transport! receive() is thus not allowed!");
    }

}
