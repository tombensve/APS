package se.natusoft.osgi.aps.net.messaging

import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.annotation.activator.BundleStart
import se.natusoft.osgi.aps.tools.annotation.activator.BundleStop
import se.natusoft.osgi.aps.tools.annotation.activator.Managed

/**
 * Starts and stops service instances.
 */
class BundleManagement {

    @Managed(loggingFor = "aps-tcp-cluster-service-provider")
    APSLogger logger

    @BundleStart(thread = true)
    public startup() {

    }

    @BundleStop
    public shutdown() {

    }
}
