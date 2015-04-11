package se.natusoft.osgi.aps.api.net.tcpip;

import java.net.DatagramPacket;
import java.net.InetAddress;

/**
 * Implemented by multicast data listeners.
 */
public interface UDPListener {

    /**
     * Received multicast data.
     *
     * @param name The name of a configuration specifying address and port or multicast and port.
     * @param dataGramPacket The received datagram.
     */
    void udpDataReceived(String name, DatagramPacket dataGramPacket);
}
