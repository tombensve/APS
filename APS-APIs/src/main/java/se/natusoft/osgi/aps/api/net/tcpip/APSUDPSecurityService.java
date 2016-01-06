package se.natusoft.osgi.aps.api.net.tcpip;

import java.net.DatagramPacket;
import java.net.URI;
import java.util.Map;

/**
 * Secures/unsecures UDP byte data.
 */
public interface APSUDPSecurityService {

    /**
     * This creates and returns some service client specific context that should always
     * be passed back to to the service calls.
     *
     * It is optional for implementations to make use of this, and if they don't null
     * should be returned.
     *
     * Client should however not make any assumptions about the service implementation
     * and always call this method and pass its value back to the other service methods.
     *
     * @param connectionPoint This can be used to map to specific configuration.
     */
    Object createSecurityContext(URI connectionPoint);

    /**
     * Secures the passed data and returns the secured version of it.
     *
     * @param data The data to secure.
     * @param securityContext The security context for the operation.
     *
     * @return A secured version of the data.
     */
    DatagramPacket secure(DatagramPacket data, Object securityContext);

    /**
     * Unsecures the passed data and returns the unsecure version of it.
     *
     * @param data The data to unsecure.
     * @param securityContext The security context for the operation.
     *
     * @return An unsecured version of the data.
     */
    DatagramPacket unsecure(DatagramPacket data, Object securityContext);
}
