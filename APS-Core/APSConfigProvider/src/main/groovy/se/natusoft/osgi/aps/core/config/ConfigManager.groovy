package se.natusoft.osgi.aps.core.config

import io.vertx.core.AsyncResult
import io.vertx.core.shareddata.AsyncMap
import io.vertx.core.shareddata.Lock
import org.osgi.framework.ServiceRegistration
import se.natusoft.osgi.aps.api.core.config.APSConfig
import se.natusoft.osgi.aps.api.core.filesystem.model.APSDirectory
import se.natusoft.osgi.aps.api.core.filesystem.model.APSFilesystem
import se.natusoft.osgi.aps.api.core.filesystem.service.APSFilesystemService
import se.natusoft.osgi.aps.api.core.platform.service.APSExecutionService
import se.natusoft.osgi.aps.api.core.platform.service.APSNodeInfoService
import se.natusoft.osgi.aps.api.core.store.APSDataStoreService
import se.natusoft.osgi.aps.api.messaging.APSMessageService
import se.natusoft.osgi.aps.core.lib.MapJsonDocValidator
import se.natusoft.osgi.aps.core.lib.StructMap
import se.natusoft.osgi.aps.exceptions.APSConfigException
import se.natusoft.osgi.aps.exceptions.APSValidationException
import se.natusoft.osgi.aps.json.JSONErrorHandler
import se.natusoft.osgi.aps.tools.APSLogger
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

    @OSGiService(timeout = "15 sec")
    private APSFilesystemService fsService

    @OSGiService(additionalSearchCriteria = "(aps-messaging-protocol=vertx-eventbus)", nonBlocking = true)
    private APSMessageService messageService

    @OSGiService(nonBlocking = true)
    private APSNodeInfoService nodeInfoService

    @OSGiService(additionalSearchCriteria = "(service-persistence-scope=clustered)", nonBlocking = true)
    private APSDataStoreService dataStoreService

    @OSGiService(nonBlocking = true)
    private APSExecutionService execService

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
    // Initializer / Shutdown
    //

    @Initializer
    void init() {

    }

    @BundleStop
    private void shutDown() {

    }

    //
    // Methods
    //

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

}
