package se.natusoft.osgi.aps.core.config

import org.osgi.framework.Bundle
import org.osgi.framework.BundleEvent
import se.natusoft.osgi.aps.api.misc.json.JSONErrorHandler
import se.natusoft.osgi.aps.api.misc.json.model.JSONObject
import se.natusoft.osgi.aps.api.misc.json.model.JSONValue
import se.natusoft.osgi.aps.api.misc.json.service.APSJSONService
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.annotation.activator.BundleListener
import se.natusoft.osgi.aps.tools.annotation.activator.Managed
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiService
import static se.natusoft.osgi.aps.api.core.config.model.APSConfig.*

// Nothing other than APSActivator will be referencing this, and it does it via reflection. Thereby the IDE
// cannot tell that this is actually used.
@SuppressWarnings("GroovyUnusedDeclaration")
class BundleConfigHandler {

    //
    // Private Members
    //

    @Managed(loggingFor = "bundle-config-handler")
    APSLogger logger

    @Managed
    private ConfigManager configManager

    @OSGiService
    private APSJSONService apsJsonService

    //
    // Methods
    //

    /**
     * This receives events from other bundles and determines if there are any new configurations to manage.
     *
     * @param event A received bundle event.
     */
    @BundleListener
    void handleEvent(BundleEvent event) {
        if (event.type == BundleEvent.STARTED) {
            handleNewBundle(event.bundle)
        }
        else if (event.type == BundleEvent.STOPPED) {
            handleLeavingBundle(event.bundle)
        }
    }

    /**
     * Manages config for new bundle.
     *
     * @param bundle The new bundle to manage config for.
     */
    private void handleNewBundle(Bundle bundle) {
        String configId = bundle.getHeaders().get(APS_CONFIG_ID)
        if (configId != null) {
            this.logger.info("Found bundle with configuration id: " + configId)

            String schemaResourcePath = bundle.headers.get(APS_CONFIG_SCHEMA)
            if (schemaResourcePath != null) {
                try {
                    BufferedInputStream configStream = new BufferedInputStream(System.getResourceAsStream(schemaResourcePath))
                    JSONValue jsonValue = this.apsJsonService.readJSON(configStream, new JSONErrorHandler() {
                        @Override
                        void warning(String message) {
                            logger.warn(message)
                        }
                        @Override
                        void fail(String message, Throwable cause) throws RuntimeException {
                            logger.error(message, cause)
                        }
                    })

                    if (JSONObject.class.isAssignableFrom(jsonValue.class)) {

                    }
                    else {
                        this.logger.error("Got bad JSON in the form of: ${jsonValue.toString()}")
                    }

                }
                catch (IOException ioe) {
                    this.logger.error("Failed to read config from: " + schemaResourcePath, ioe)
                }
            }
            else {
                this.logger.error("Bad bundle! APS-ConfigId is available, but no APS-ConfigSchema found!")
            }
        }

    }

    private void handleLeavingBundle(Bundle bundle) {

    }
}
