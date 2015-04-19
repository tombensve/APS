package se.natusoft.osgi.aps.tcpipsvc.security

import se.natusoft.osgi.aps.api.net.tcpip.APSTCPSecurityService
import se.natusoft.osgi.aps.tools.APSServiceTracker
import se.natusoft.osgi.aps.tools.annotation.activator.Managed
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiService

/**
 * Wraps APSTCPSecurityService if such is available otherwise it provides its own non secure implementation.
 */
class TCPSecurityHandler {
    //
    // Private Members
    //

    @OSGiService
    private APSServiceTracker<APSTCPSecurityService> tcpSecurityServiceTracker = null

    //
    // Methods
    //


    /**
     * If an APSTCPSecurityService is available it is used to get a secure socket, otherwise a non secure
     * socket is returned.
     *
     * @param host The host to connect to.
     * @param port The port to connect to.
     * @param secure true if security is wanted.
     *
     * @throws IOException
     */
    Socket createSocket(InetAddress host, int port, boolean secure) throws IOException {
        Socket socket = null
        if (secure && this.tcpSecurityServiceTracker != null && this.tcpSecurityServiceTracker.hasTrackedService()) {
            APSTCPSecurityService securityService = this.tcpSecurityServiceTracker.allocateService()
            socket = securityService.socketFactory.createSocket(host, port)
            this.tcpSecurityServiceTracker.releaseService()
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
        if (secure && this.tcpSecurityServiceTracker != null && this.tcpSecurityServiceTracker.hasTrackedService()) {
            APSTCPSecurityService securityService = this.tcpSecurityServiceTracker.allocateService()
            socket = securityService.serverSocketFactory.createServerSocket()
            this.tcpSecurityServiceTracker.releaseService()
        }
        else {
            socket = new ServerSocket()
        }
        return socket
    }
}
