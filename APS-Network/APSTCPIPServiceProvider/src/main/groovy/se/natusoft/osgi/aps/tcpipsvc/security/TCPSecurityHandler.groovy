package se.natusoft.osgi.aps.tcpipsvc.security

import se.natusoft.osgi.aps.api.net.tcpip.APSTCPSecurityService
import se.natusoft.osgi.aps.tools.APSServiceTracker
import se.natusoft.osgi.aps.tools.annotation.activator.Managed
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiService
import se.natusoft.osgi.aps.tools.exceptions.APSNoServiceAvailableException

/**
 * Wraps APSTCPSecurityService throwing an IOException if service is not available.
 */
class TCPSecurityHandler {
    //
    // Private Members
    //

    @OSGiService(timeout = "10 seconds")
    private APSTCPSecurityService tcpSecurityService

    //
    // Methods
    //

    /**
     * This will return a secure socket.
     *
     * @param host The host to connect to.
     * @param port The port to connect to.
     * @param secure true if security is wanted.
     *
     * @throws IOException on failure to create a secure socket. This is most probably caused by not having an APSTCPSecurityService.
     */
    Socket createSocket(InetAddress host, int port, boolean secure) throws IOException {
        Socket socket = null
        if (secure) {
            try {
                socket = tcpSecurityService.socketFactory.createSocket(host, port)
            }
            catch (APSNoServiceAvailableException nsae) {
                throw new IOException("Security was requested, but no APSTCPSecurityService is available!", nsae)
            }
        }
        else {
            socket = new Socket(host, port)
        }
        return socket
    }

    /**
     * If an APSTCPSecurityService is available it is used to get a secure server socket, otherwise a non
     * secure server socket is returned.
     *
     * @param secure true if security is wanted.
     *
     * @throws IOException
     */
    ServerSocket createServerSocket(boolean secure) throws IOException {
        ServerSocket socket = null
        if (secure) {
            try {
                socket = tcpSecurityService.serverSocketFactory.createServerSocket()
            }
            catch (APSNoServiceAvailableException nsae) {
                throw new IOException("Security was requested, but no APSTCPSecurityService is available!", nsae)
            }
        }
        else {
            socket = new ServerSocket()
        }
        return socket
    }
}
