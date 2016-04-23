package se.natusoft.osgi.aps.discoveryservice

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.osgi.aps.api.net.tcpip.APSTCPIPService
import se.natusoft.osgi.aps.tools.APSLogger

/**
 * This is a task that sends one ServiceDescription to another DiscoveryService instance.
 */
@CompileStatic
@TypeChecked
class TCPSendTask implements Runnable {
    //
    // Properties
    //

    /** The service data to send. */
    byte[] serviceInfo

    /** The service to send with. */
    APSTCPIPService tcpipService

    /** The TCP connection point to send to. */
    URI tcpSendConnectionPoint

    /** Failures are logged on this. */
    APSLogger logger

    //
    // Methods
    //

    /**
     * Sends a service description using TCP. This method is called from an ExecutorService.
     */
    public void run() {
        try {
            this.tcpipService.sendStreamedRequest(tcpSendConnectionPoint, {
                URI sendPoint, OutputStream requestStream, InputStream responseStream ->

                requestStream.write(serviceInfo)
                requestStream.flush()
            })
        }
        catch (Exception e) {
            this.logger.error("Failed TCP send task for '${tcpSendConnectionPoint}'!", e)
        }
    }
}
