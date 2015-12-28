package se.natusoft.osgi.aps.tcpipsvc.meta

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.osgi.aps.api.core.meta.APSMetaDataBean

/**
 * Meta data for the APSTCPIPService.
 */
@CompileStatic
@TypeChecked
class APSTCPIPServiceMetaData implements APSMetaDataBean {
    //
    // Properties
    //

    String ownerName = "APSTCPIPService"
    String version = "1.0.0"
    synchronized long multicastSends
    synchronized long multicastReceives
    synchronized long multicastListeners
    synchronized long udpSends
    synchronized long udpReceives
    synchronized long udpListeners
    synchronized long tcpServices
    synchronized long tcpRequests
    synchronized long tcpListeners

    //
    // Methods
    //


}
