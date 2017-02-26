package se.natusoft.osgi.aps.web.adminweb

import se.natusoft.osgi.aps.apsadminweb.service.APSAdminWebService
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.annotation.activator.Initializer
import se.natusoft.osgi.aps.tools.annotation.activator.Managed
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiService
import se.natusoft.osgi.aps.tools.groovy.lib.MapJsonDocVerifier

/**
 * General services.
 */
class AdminWebsProvider {
    //
    // Constants
    //

    private static final Map<String, Object> EVENT_SCHEMA = [
            header_1: [
                    type_1      : "service",
                    address_1   : "?aps\\.admin\\..*",
                    classifier_1: "?public|private"
            ],
            body_1  : [
                    action_1: "get-webs"
            ],
            reply_0: [
                    webs_1: [
                            [
                                    name_1: "?.*",
                                    url_1: "?^https?://.*"
                            ]
                    ]
            ]
    ] as Map<String, Object>

    private static final MapJsonDocVerifier EVENT_VERIFIER = new MapJsonDocVerifier( validStructure: EVENT_SCHEMA)

    //
    // Private Members
    //

    @Managed( loggingFor = "aps-admin-web:admin-webs-provider" )
    private APSLogger logger

    @Managed
    private LocalEventBus localBus

    @OSGiService
    private APSAdminWebService adminWebService

    //
    // Init
    //

    @Initializer
    void init() {
        this.localBus.consume { Map<String, Object> event ->
            try {
                EVENT_VERIFIER.validate(event)


            }
            catch (Exception e) {
                this.logger.error("Problem with received event: $e.message")
            }
        }
    }
}
