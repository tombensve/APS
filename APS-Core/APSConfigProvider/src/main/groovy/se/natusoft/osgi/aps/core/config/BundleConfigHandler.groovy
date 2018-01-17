package se.natusoft.osgi.aps.core.config

import org.osgi.framework.Bundle
import org.osgi.framework.BundleEvent
import JSONErrorHandler
import se.natusoft.osgi.aps.api.misc.json.model.JSONObject
import se.natusoft.osgi.aps.api.misc.json.model.JSONValue
import se.natusoft.osgi.aps.api.misc.json.service.APSJSONService
import se.natusoft.osgi.aps.core.lib.MapJsonDocValidator
import se.natusoft.osgi.aps.exceptions.APSValidationException
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
 * - The 'APS-Config-Default-Resource' is used the first time a config is seen to provide default values.
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

    @Managed(loggingFor = "aps-config-provider:bundle-config-handler")
    APSLogger logger

    @OSGiService
    private APSJSONService apsJsonService

    @Managed
    private ConfigManager configManager

    private jsonErrorHandler = new JSONErrorHandler() {

        @Override
        void warning( String message ) { logger.warn( message ) }

        @Override
        void fail( String message, Throwable cause ) throws RuntimeException { logger.error( message, cause ) }
    }

    //
    // Methods
    //

    /**
     * This receives events from other bundles and determines if there are any new configurations to manage.
     *
     * @param event A received bundle event.
     */
    @BundleListener
    void handleEvent( BundleEvent event ) {
        if ( event.type == BundleEvent.STARTED ) {
            handleNewBundle( event.bundle )
        }
        else if ( event.type == BundleEvent.STOPPED ) {
            handleLeavingBundle( event.bundle )
        }
    }

    /**
     * Manages config for new bundle.
     *
     * @param bundle The new bundle to manage config for.
     */
    private void handleNewBundle( Bundle bundle ) {
        String configId = bundle.getHeaders().get( "APS-Config-Id" )
        if ( configId != null ) {
            this.logger.info( "Found bundle with configuration id: " + configId )

            String schemaResourcePath = bundle.headers.get( "APS-Config-Schema" )

            if ( schemaResourcePath != null ) {
                try {

                    BufferedInputStream schemaStream = new BufferedInputStream( System.getResourceAsStream( schemaResourcePath ) )
                    JSONValue jsonValue = this.apsJsonService.readJSON( schemaStream, this.jsonErrorHandler )

                    if ( JSONObject.class.isAssignableFrom( jsonValue.class ) ) {

                        Map<String, Object> schema = ( jsonValue as JSONObject ).toMap()


                        String defaultResourcePath = bundle.headers.get( "APS-Config-Default-Resource" )

                        if ( defaultResourcePath != null ) {

                            BufferedInputStream configStream = new BufferedInputStream( System.getResourceAsStream( defaultResourcePath ) )
                            jsonValue = this.apsJsonService.readJSON( configStream, this.jsonErrorHandler )

                            if ( JSONObject.class.isAssignableFrom( jsonValue.class ) ) {

                                this.configManager.publishConfig( configId, schema, ( jsonValue as JSONObject ).toMap() )
                            }
                            else {
                                this.logger.error(
                                        "Got bad config JSON in the form of: '${jsonValue.toString()}' from bundle '${bundle.symbolicName}'!"
                                )
                            }
                        }
                        else {
                            this.logger.error( "No APS-Config-Default-Resource have been provided by bundle '${bundle.symbolicName}'!" )
                        }
                    }
                    else {
                        this.logger.error( "Got bad schema JSON in the form of: '${jsonValue.toString()}' from bundle '${bundle.symbolicName}'!" )
                    }

                }
                catch ( IOException ioe ) {
                    this.logger.error( "Failed to read config from: ${schemaResourcePath} for bundle '${bundle.symbolicName}'!", ioe )
                }
            }
            else {
                this.logger.error( "Bad bundle ('${bundle.symbolicName}')! APS-ConfigId is available, but no APS-Config-Schema found!" )
            }
        }

    }

    private Map<String, Object> upgradeConfig( Map<String, Object> config ) {

    }

    /**
     * Handles a valid configuration.
     *
     * @param schema
     * @param config
     */
    private void handleConfiguration( Map<String, Object> schema, Map<String, Object> config ) {

    }

    private void handleLeavingBundle( Bundle bundle ) {

    }
}
