package se.natusoft.osgi.aps.api.net.tcpip;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import java.net.URI;

/**
 * This service provides socket factories. Unfortunately SocketFactory and ServerSocketFactory are not interfaces
 * and can't thereby be published as services directly.
 */
@SuppressWarnings("PackageAccessibility")
public interface APSTCPSecurityService {

    /**
     * Returns a SocketFactory providing some security implementation like SSL for example.
     *
     * @param connectionPoint Can be used to map to a configuration for the SocketFactory.
     */
    SocketFactory getSocketFactory(URI connectionPoint);

    /**
     * Returns a ServerSocketFactory providing some security implementation like SSL for example.
     *
     * @param connectionPoint Can be used to map to a configuration for the ServerSocketFactory.
     */
    ServerSocketFactory getServerSocketFactory(URI connectionPoint);
}
