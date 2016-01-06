package se.natusoft.osgi.aps.api.net.tcpip;

import java.net.DatagramPacket;
import java.net.URI;

/**
 * This is a listener of a block of data.
 */
public interface DatagramPacketListener {

    /**
     * This is called whenever a new data block is received.
     *
     * @param receivePoint The receive-point this data came from.
     * @param packet The actual data received.
     */
    void dataBlockReceived(URI receivePoint, DatagramPacket packet);
}
