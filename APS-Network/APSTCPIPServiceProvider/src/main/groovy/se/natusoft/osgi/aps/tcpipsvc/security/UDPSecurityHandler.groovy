package se.natusoft.osgi.aps.tcpipsvc.security

import se.natusoft.osgi.aps.api.net.tcpip.APSTCPSecurityService
import se.natusoft.osgi.aps.api.net.tcpip.APSUDPSecurityService
import se.natusoft.osgi.aps.tools.APSServiceTracker
import se.natusoft.osgi.aps.tools.annotation.activator.Managed
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiService

/**
 * Wraps APSUDPSecurityService calling it if service is available, otherwise just returning input as is.
 */
class UDPSecurityHandler {

    //
    // Private Members
    //

    @OSGiService
    private APSServiceTracker<APSUDPSecurityService> udpSecurityServiceTracker = null

    private Object securityContext = null

    //
    // Methods
    //

    private boolean securitySvcAvailable() {
        return this.udpSecurityServiceTracker != null && this.udpSecurityServiceTracker.hasTrackedService()
    }

    private Object getSecurityContext(APSUDPSecurityService securityService) {
        if (this.securityContext == null) {
            this.securityContext = securityService.createSecurityContext(UUID.randomUUID().toString())
        }
        return this.securityContext
    }

    public void secure(DatagramPacket data, boolean secure) throws IOException {
        if (secure && securitySvcAvailable()) {
            APSUDPSecurityService apsudpSecurityService = this.udpSecurityServiceTracker.allocateService()
            apsudpSecurityService.secure(data, getSecurityContext(apsudpSecurityService))
            this.udpSecurityServiceTracker.releaseService()
        }
    }

    public void unsecure(DatagramPacket data, boolean secure) throws IOException {
        if (secure && securitySvcAvailable()) {
            APSUDPSecurityService apsudpSecurityService = this.udpSecurityServiceTracker.allocateService()
            result = apsudpSecurityService.unsecure(data, getSecurityContext(apsudpSecurityService))
            this.udpSecurityServiceTracker.releaseService()
        }
    }
}
