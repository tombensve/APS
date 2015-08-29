package se.natusoft.osgi.aps.api.net.tcpip;

import se.natusoft.osgi.aps.api.APSServiceProperties;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.List;

/**
 * This service provides the following functions:
 *
 * * TCP, UDP & Multicast network traffic.
 *
 * * External configurations of network connections.
 *
 * * Decoupling from java.net classes allowing for test implementations where network traffic can be
 *   simulated in unit-tests without causing problems when run concurrently in a CI server.
 *
 * The users of this service should also have a config that specifies which config of this service
 * to use.
 *
 * If a service both sends TCP request and receives them there need to be 2 different config entries
 * for this.
 */
@SuppressWarnings("unused")
public interface APSTCPIPService extends APSServiceProperties {

    /** This can be used for remove UDPListener. */
    UDPListener ALL_LISTENERS = null;

    /**
     * Sends UDP data.
     *
     * @param name The name of a configuration specifying address and port or multicast and port.
     * @param data The data to send.
     *
     * @throws IOException The one and only!
     * @throws IllegalArgumentException on unconfigured name.
     */
    void sendUDP(String name, byte[] data) throws IOException;

    /**
     * Reads UDP data.
     *
     * @param name The name of a configuration specifying address and port or multicast and port.
     * @param data The buffer to receive into.
     *
     * @return the data buffer.
     *
     * @throws IOException
     * @throws IllegalArgumentException on unconfigured name.
     */
    DatagramPacket readUDP(String name, byte[] data) throws IOException;

    /**
     * Adds a listener for received udp data.
     *
     * @param name The name of a configuration specifying address and port or multicast and port.
     * @param listener The listener to call back with messages.
     *
     * @throws IllegalArgumentException on unconfigured name.
     */
    void addUDPListener(String name, UDPListener listener);

    /**
     * Removes a listener for received udp data.
     *
     * @param name The name of a configuration specifying address and port or multicast and port.
     * @param listener The listener to remove, or null for all.
     *
     * @throws IllegalArgumentException on unconfigured name.
     */
    void removeUDPListener(String name, UDPListener listener);

    /**
     * Sends a TCP request on a named TCP config.
     *
     * @param name The named config to send to.
     * @param request A callback that provides a request output stream and a response input stream.
     *
     * @throws IllegalArgumentException on unconfigured name.
     */
    void sendTCPRequest(String name, TCPRequest request) throws IOException;

    /**
     * Sets a listener for incoming TCP requests. There can only be one per name.
     *
     * @param name The named config to add listener for.
     * @param listener The listener to add.
     *
     * @throws IllegalArgumentException on unconfigured name.
     */
    void setTCPRequestListener(String name, TCPListener listener);

    /**
     * Removes a listener for incoming TCP requests.
     *
     * @param name The named config to remove a listener for.
     *
     * @throws IllegalArgumentException on unconfigured name.
     */
    void removeTCPRequestListener(String name);

    /**
     * Returns a list of names matching the specified regexp.
     *
     * @param regexp The regexp to get names for.
     *
     * @return A list of the matching names. If none were found the list will be empty.
     */
    List<String> getNames(String regexp);
}
