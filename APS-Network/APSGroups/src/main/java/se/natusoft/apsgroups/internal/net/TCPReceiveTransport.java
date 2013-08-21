package se.natusoft.apsgroups.internal.net;

import se.natusoft.apsgroups.config.APSGroupsConfig;
import se.natusoft.apsgroups.logging.APSGroupsLogger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * A transport for receiving over TCP.
 */
public class TCPReceiveTransport implements Transport {
    //
    // Private Members
    //

    private APSGroupsConfig.TransportConfig config;

    private ServerSocket svcSocket;

    private APSGroupsLogger logger;

    //
    // Constructors
    //

    /**
     * Creates a new TCPReceiveTransport.
     *
     * @param logger The logger to use.
     * @param config The config for the transport.
     */
    public TCPReceiveTransport(APSGroupsLogger logger, APSGroupsConfig.TransportConfig config) {
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
        this.logger.info("Opening TCPReceiveTransport on port " + config.getPort() + "!");
        this.svcSocket = new ServerSocket(this.config.getPort());
    }

    /**
     * Closes this transport.
     *
     * @throws java.io.IOException
     */
    @Override
    public void close() throws IOException {
        this.logger.info("Closing TCPReceiveTranport on port " + config.getPort() + "!");
        if (this.svcSocket != null) {
            this.svcSocket.close();
        }
    }

    /**
     * Sends the byte of data to the destination(s).
     *
     * @param data The data to send.
     * @throws java.io.IOException on failure to send.
     */
    @Override
    public void send(byte[] data) throws IOException {
        throw new UnsupportedOperationException("This is a receiving transport! send() is thus not allowed!");
    }

    /**
     * Receives data.
     *
     * @throws java.io.IOException
     */
    @Override
    public Packet receive() throws IOException {
        Socket socket = this.svcSocket.accept();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        InputStream recvStream = socket.getInputStream();
        int b = recvStream.read();
        while (b != -1) {
            bytes.write(b);
            b = recvStream.read();
        }
        bytes.close();
        Packet packet = new TCPReceiveTransportPacket(bytes.toByteArray(), socket.getInetAddress());
        socket.close();

        return packet;
    }

    //
    // Inner Classes
    //

    /**
     * Provides an implementation of Transport.Packet.
     */
    private static class TCPReceiveTransportPacket implements Packet {
        //
        // Private Members
        //

        private byte[] bytes;

        private InetAddress address;

        //
        // Constructors
        //

        /**
         * Creates a new TPCReceiveTransportPacket.
         *
         * @param bytes The bytes of the packet.
         * @param address The origin address of the data.
         */
        public TCPReceiveTransportPacket(byte[] bytes, InetAddress address) {
            this.bytes = bytes;
            this.address = address;
        }

        //
        // Methods
        //

        /**
         * @return The packet bytes. This will return only the received amount of bytes.
         */
        @Override
        public byte[] getBytes() {
            return this.bytes;
        }

        /**
         * @return The address the packet came from.
         */
        @Override
        public InetAddress getAddress() {
            return this.address;
        }
    }
}
