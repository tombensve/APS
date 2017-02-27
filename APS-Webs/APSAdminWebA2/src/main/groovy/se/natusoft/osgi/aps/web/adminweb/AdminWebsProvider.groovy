package se.natusoft.osgi.aps.web.adminweb

import se.natusoft.osgi.aps.apsadminweb.service.APSAdminWebService
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.annotation.activator.Initializer
import se.natusoft.osgi.aps.tools.annotation.activator.Managed
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiService
import static se.natusoft.osgi.aps.web.adminweb.Constants.*

/**
 * General services.
 */
class AdminWebsProvider {
    //
    // Private Members
    //

    @Managed(loggingFor = "aps-admin-web:admin-webs-provider")
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
                EventDefinition.validate(event)

                switch ((event[EVENT.FIELD.BODY])[EVENT.FIELD.ACTION] as String) {
                    case EVENT.ACTION.GET_WEBS:
                        handleGetWebs(event)
                        EventDefinition.validate(event)
                        break
                    default:
                        event[EVENT.FIELD.ERROR] = EventDefinition.createError([ code: 2, message: "Bad action!"])
                }

            }
            catch (Exception e) {
                this.logger.error("Problem with received event: ${e.message}")
                event[EVENT.FIELD.ERROR] = EventDefinition.createError([ code: 1, message: "Problem with received event: ${e.message}"])
            }

            null // Might need a real Disposable implementation here!
        }
    }

    private void handleGetWebs(Map<String, Object> event) {

    }
}
