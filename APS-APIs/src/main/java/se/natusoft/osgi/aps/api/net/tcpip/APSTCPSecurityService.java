package se.natusoft.osgi.aps.api.net.tcpip;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;

/**
 * This service provides socket factories. Unfortunately SocketFactory and ServerSocketFactory are not interfaces
 * and can't thereby be published as services directly.
 */
public interface APSTCPSecurityService {

    /**
     * Returns a SocketFactory providing some security implementation like SSL for example.
     *
     * @param name This corresponds to a APSTCPIPService named configuration. Can be used to map to a configuration
     *             for the SocketFactory.
     */
    SocketFactory getSocketFactory(String name);

    /**
     * Returns a ServerSocketFactory providing some security implementation like SSL for example.
     *
     * @param name This corresponds to a APSTCPIPService named configuration. Can be used to map to a configuration
     *             for the ServerSocketFactory.
     */
    ServerSocketFactory getServerSocketFactory(String name);
}
