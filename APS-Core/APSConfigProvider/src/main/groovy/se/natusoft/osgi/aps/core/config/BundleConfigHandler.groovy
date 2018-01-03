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

/**
 * This listens to bundles and manages configurations.
 *
 * This  is how it works:
 * - Bundles MANIFEST.MF is checked for 'APS-Config-Id:', 'APS-Config-Schema:' and 'APS-Config-Default-Resource:'.
 *
 * - 'APS-Config-Id' is a key used to identify the config.
 *
 * - The 'APS-Config-Defaut-Resource' is used the first time a config is seen to provide default values.
 *   It is also checked for default values when managed config does not provide a value. This will happen
 *   when a new value have been added to a config at a later time.
 *
 * - An APSConfig implementation will be published as an OSGi service for each configuration, also with
 *   'APS-Config-Id=<id>' in the properties for the published service. This is used by config owner to
 *   lookup the correct APSConfig instance to use.
 */
// Nothing other than APSActivator will be referencing this, and it does it via reflection. Thereby the IDE
// cannot tell that this is actually used.
@SuppressWarnings("GroovyUnusedDeclaration")
class BundleConfigHandler {

    //
    // Private Members
    //

    @Managed(loggingFor = "bundle-config-handler")
    APSLogger logger

    @OSGiService
    private APSJSONService apsJsonService

    @OSGiService
    private APSFileSystemService

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
