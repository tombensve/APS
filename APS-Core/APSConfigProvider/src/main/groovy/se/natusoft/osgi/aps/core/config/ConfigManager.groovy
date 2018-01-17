package se.natusoft.osgi.aps.core.config

import io.vertx.core.AsyncResult
import io.vertx.core.Vertx
import io.vertx.core.shareddata.AsyncMap
import io.vertx.core.shareddata.Lock
import io.vertx.core.shareddata.SharedData
import org.osgi.framework.BundleContext
import org.osgi.framework.ServiceReference
import org.osgi.framework.ServiceRegistration
import se.natusoft.osgi.aps.api.core.config.APSConfig
import se.natusoft.osgi.aps.api.core.filesystem.model.APSDirectory
import se.natusoft.osgi.aps.api.core.filesystem.model.APSFilesystem
import se.natusoft.osgi.aps.api.core.filesystem.service.APSFilesystemService
import JSONErrorHandler
import se.natusoft.osgi.aps.api.misc.json.service.APSJSONService
import se.natusoft.osgi.aps.core.lib.MapJsonDocValidator
import se.natusoft.osgi.aps.core.lib.StructMap
import se.natusoft.osgi.aps.exceptions.APSConfigException
import se.natusoft.osgi.aps.exceptions.APSValidationException
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.APSServiceTracker
import se.natusoft.osgi.aps.tools.annotation.activator.BundleStop
import se.natusoft.osgi.aps.tools.annotation.activator.Initializer
import se.natusoft.osgi.aps.tools.annotation.activator.Managed
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiService

class ConfigManager {

    //
    // Private Members
    //

    @Managed(loggingFor = "aps-config-provider:config-manager")
    private APSLogger logger

    @Managed
    private BundleContext context

    @OSGiService
    private APSJSONService apsJsonService

    @OSGiService(timeout = "6 seconds")
    private APSFilesystemService fsService

    @OSGiService(additionalSearchCriteria = "(vertx-object=Vertx)", timeout = "forever")
    private APSServiceTracker<Vertx> vertxTracker

    private SharedData _sharedData = null

    private Map<String, ServiceRegistration> regs = [ : ]

    private jsonErrorHandler = new JSONErrorHandler() {

        @Override
        void warning( String message ) { logger.warn( message ) }

        @Override
        void fail( String message, Throwable cause ) throws RuntimeException {

            logger.error( message, cause )
            throw new APSConfigException( message, cause )
        }
    }

    //
    // Initializer
    //

    @Initializer
    void init() {

    }

    //
    // Methods
    //

    /**
     * Gets called if adn when Vertx is available.
     *
     * @param vertx The Vertx instance.
     * @param vertxRef The Vertx service reference. (not used)
     */
    private void onVertxAvailable( Vertx vertx, ServiceReference vertxRef ) {
        sharedData = vertx.sharedData()
    }

    /**
     * Gets called if Vertx have been available and is leaving.
     *
     * @param vertxRef The Vertx service reference. (not used)
     * @param vertxAPIClass The API class. (not used)
     */
    private void onVertxLeaving( ServiceReference vertxRef, Class vertxAPIClass ) {
        sharedData = null
    }

    /**
     * This is to provide thread synchronization when accessing sharedData.
     *
     * @param sharedData The SharedData instance.
     */
    private synchronized void setSharedData( SharedData sharedData ) {
        this._sharedData = sharedData
    }

    /**
     * @return The SharedData instance or null if not available.
     */
    private synchronized SharedData getSharedData() {
        this._sharedData
    }

    void publishConfig( String configId, Map<String, Object> schema, StructMap defaultConfig ) {

        try {

            APSConfig config = loadConfig( configId, schema, defaultConfig )
        }
        catch ( APSValidationException ve ) {

            this.logger.error( "Got bad config: ${ve.message}", ve )
        }

    }

    private APSConfig loadConfig( String configId, Map<String, Object> schema, StructMap defaultConfig ) {

        APSConfig config = null

        APSFilesystem fs = this.fsService.getFilesystem( "aps-config-provider" )
        if ( fs == null ) {

            fs = this.fsService.createFilesystem( "aps-config-provider" )
        }

        APSDirectory root = fs.getRootDirectory()
        if ( !root.exists( "configs" ) ) {

            root.createDir( "configs" )
        }

        APSDirectory dir = fs.getDirectory( "configs" )
        if ( dir.exists( "${configId}.json" ) ) {

            try {

                Map<String, Object> conf = this.apsJsonService.readJSONObject( dir.getFile( "${configId}.json" ).createInputStream(),
                                                                               jsonErrorHandler )

                MapJsonDocValidator validator = new MapJsonDocValidator( validStructure: schema )
                validator.validate( conf )

                config = new APSConfigProvider(
                        apsConfigId: configId,
                        defaultConfig: defaultConfig,
                        updatedNotifier: { APSConfigProvider _this, String structPath, Object value ->
                            saveConfig( _this )
                            if ( sharedData != null ) {

                                sharedData.getLock( "aps-config-provider:${_this.apsConfigId}" ) { AsyncResult<Lock> lres ->

                                    sharedData.getClusterWideMap(
                                            "aps-config-provider:${_this.apsConfigId}" ) { AsyncResult<AsyncMap> mres ->

                                        mres.result().put( structPath, value ) { AsyncResult<Void> ar ->
                                            this.logger.info( "Updated: k:${structPath} for configId:${_this.apsConfigId}!" )
                                        }
                                    }

                                    lres.result(  ).release(  )
                                }
                            }
                        }
                )
                config.putAll( conf )
            }
            catch ( APSValidationException ignore ) {
                // Try upgrading config using schema and default

            }
            catch ( APSConfigException ignore ) {
                return null
            }
        }
        else {
            // Create from default
        }

        config
    }

    void saveConfig( APSConfigProvider configProvider ) {

    }

    void unpublishConfig( String configId ) {

    }

    private void handleConfigDataChanged( APSConfig config ) {

    }

    @BundleStop
    private void shutDown() {

    }
}
