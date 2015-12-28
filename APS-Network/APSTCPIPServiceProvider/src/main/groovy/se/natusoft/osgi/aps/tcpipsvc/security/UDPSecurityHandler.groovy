package se.natusoft.osgi.aps.tcpipsvc.security

import se.natusoft.osgi.aps.api.net.tcpip.APSTCPSecurityService
import se.natusoft.osgi.aps.api.net.tcpip.APSUDPSecurityService
import se.natusoft.osgi.aps.tools.APSServiceTracker
import se.natusoft.osgi.aps.tools.annotation.activator.Managed
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiService
import se.natusoft.osgi.aps.tools.exceptions.APSNoServiceAvailableException

/**
 * Wraps APSUDPSecurityService and throwing an IOException if none is available.
 */
class UDPSecurityHandler {

    //
    // Private Members
    //

    @OSGiService(timeout = "10 seconds")
    private APSUDPSecurityService udpSecurityService

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
        if (secure) {
            try {
                this.udpSecurityService.secure(data, getSecurityContext(this.udpSecurityService))
            }
            catch (APSNoServiceAvailableException nsae) {
                throw new IOException("Failed to secure packet, no APSUDPSecurityService available!", nsae)
            }
        }
    }

    public void unsecure(DatagramPacket data, boolean secure) throws IOException {
        if (secure) {
            try {
                this.udpSecurityService.unsecure(data, getSecurityContext(this.udpSecurityService))
            }
            catch (APSNoServiceAvailableException nsae) {
                throw new IOException("Failed to unsecure packet, no APSUDPSecurityService available!", nsae)
            }
        }
    }
}
