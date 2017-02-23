package se.natusoft.osgi.aps.web.adminweb

import se.natusoft.osgi.aps.apsadminweb.service.APSAdminWebService
import se.natusoft.osgi.aps.tools.annotation.activator.Initializer
import se.natusoft.osgi.aps.tools.annotation.activator.Managed
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiService

/**
 * General services.
 */
class AdminWebsProvider {

    //
    // Private Members
    //

    @Managed
    private LocalEventBus localBus

    @OSGiService
    private APSAdminWebService adminWebServcie

    //
    // Init
    //

    @Initializer
    void init() {
        this.localBus.consume { Map<String, Object> event ->
            if (event [ Constants.])
        }
    }
}
