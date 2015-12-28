package se.natusoft.osgi.aps.discoveryservice

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.osgi.aps.api.net.discovery.model.ServiceDescription
import se.natusoft.osgi.aps.api.net.tcpip.APSTCPIPService
import se.natusoft.osgi.aps.api.net.tcpip.TCPRequest
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

    /** The service description to send. */
    ServiceDescription serviceDescription

    /** Add or remove. */
    byte headerByte

    /** The service to send with. */
    APSTCPIPService tcpipService

    /** The APSTCPIPService named config to use for sending. */
    String tcpSendConfig

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
            this.tcpipService.sendTCPRequest(tcpSendConfig, new TCPRequest() {
                void tcpRequest(OutputStream requestStream, InputStream responseStream) throws IOException {
                    ObjectOutputStream ooStream = new ObjectOutputStream(requestStream)
                    ReadWriteTools.writeServiceDescription(headerByte, serviceDescription, ooStream)
                    ooStream.close()
                    requestStream.close()
                }
            })
        }
        catch (Exception e) {
            this.logger.error("Failed TCP send task for '${tcpSendConfig}'!", e)
        }
    }
}
