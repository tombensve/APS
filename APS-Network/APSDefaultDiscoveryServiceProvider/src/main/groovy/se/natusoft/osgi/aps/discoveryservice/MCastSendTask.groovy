package se.natusoft.osgi.aps.discoveryservice

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.osgi.aps.api.net.discovery.model.ServiceDescription
import se.natusoft.osgi.aps.api.net.tcpip.APSTCPIPService
import se.natusoft.osgi.aps.tools.APSLogger

/**
 * This is a task that sends one ServiceDescription to multiple instances of this service via multicast.
 */
@CompileStatic
@TypeChecked
class MCastSendTask implements Runnable {
    //
    // Properties
    //

    /** The service to share. */
    ServiceDescription serviceDescription

    /** Add or remove. */
    byte headerByte

    /** The service to use for sending. */
    APSTCPIPService tcpipService

    /** A named config in the APSTCPIPService to use. */
    URI mcastConnectionPoint

    /** For logging failures. */
    APSLogger logger

    //
    // Methods
    //

    /**
     * Sends a service description using multicast. This will be called by a ExecutorService.
     */
    public void run() {
        try {
            this.tcpipService.sendDataPacket(this.mcastConnectionPoint, ReadWriteTools.toBytes(headerByte, serviceDescription))
        }
        catch (Exception e) {
            this.logger.error("Failed multicast send task for '${mcastConnectionPoint}'!", e)
        }
    }
}
