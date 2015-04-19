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
     */
    SocketFactory getSocketFactory();

    /**
     * Returns a ServerSocketFactory providing some security implementation like SSL for example.
     */
    ServerSocketFactory getServerSocketFactory();
}
