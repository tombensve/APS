package se.natusoft.osgi.aps.core.config

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.osgi.framework.Bundle
import se.natusoft.osgi.aps.api.core.config.APSConfig
import se.natusoft.osgi.aps.api.core.filesystem.model.APSDirectory
import se.natusoft.osgi.aps.api.core.filesystem.model.APSFilesystem
import se.natusoft.osgi.aps.api.core.filesystem.service.APSFilesystemService
import se.natusoft.osgi.aps.api.reactive.APSHandler
import se.natusoft.osgi.aps.core.lib.MapJsonDocValidator
import se.natusoft.osgi.aps.core.lib.StructMap
import se.natusoft.osgi.aps.exceptions.APSConfigException
import se.natusoft.osgi.aps.json.JSON
import se.natusoft.osgi.aps.json.JSONErrorHandler
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.annotation.activator.Managed
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiService

/**
 * This class represents one individual configuration.
 */
@CompileStatic
@TypeChecked
class APSConfigProvider extends StructMap implements APSConfig {

    //
    // Properties
    //

    /** The Id of this config. */
    String apsConfigId

    /** For notifying about updates. This instance should always be passed as argument! */
    Closure updatedNotifier

    //
    // Private Members
    //

    @Managed(loggingFor = "aps-config-provider")
    private APSLogger logger

    /** Filesystem access that won't go away on redeploy. */
    @OSGiService(timeout = "15 sec")
    private APSFilesystemService fsService

    /** Save of config dir for when saving. */
    private APSDirectory configDir

    /** The default config. */
    private StructMap defaultConfig

    /** For when parsing JSON. */
    private JSONErrorHandler jsonErrorHandler = new JSONErrorHandler() {

        @Override
        void warning( String message ) { logger.warn( message ) }

        @Override
        void fail( String message, Throwable cause ) throws RuntimeException {

            logger.error( message, cause )
            throw new APSConfigException( message, cause )
        }
    }

    //
    // Methods
    //

    /**
     * Looks up the value of a specified struct Path.
     *
     * @param structPath The structPath to lookup.
     * @param valueHandler The handler receiving the looked up value.
     */
    @Override
    void lookup( String structPath, APSHandler<Object> valueHandler ) {
        Object value = super.lookup( structPath )
        if ( value == null ) {
            value = this.defaultConfig.lookup( structPath )
            if ( value != null ) {
                super.provide( structPath, value )
            }
        }

        valueHandler.handle( value )
    }

    /**
     * provides a value.
     *
     * @param structPath The value path.
     * @param value The value.
     */
    @Override
    void provide( String structPath, Object value ) {
        super.provide( structPath, value )

        saveConfig(  )

        this.updatedNotifier( this )
    }

    /**
     * Loads a configuration.
     *
     * @param configId The unique id of the config.
     * @param schemaPath The path to the configuration schema.
     * @param defaultConfigPath The path to the default configuration values.
     */
    void loadConfig( String configId, Bundle ownerBundle, String schemaPath, String defaultConfigPath ) {

        APSConfigProvider config = null

        APSFilesystem fs = this.fsService.getFilesystem( "aps-config-provider" )
        if ( fs == null ) {

            fs = this.fsService.createFilesystem( "aps-config-provider" )
        }

        APSDirectory root = fs.getRootDirectory()
        if ( !root.exists( "configs" ) ) {

            root.createDir( "configs" )
        }

        this.configDir = fs.getDirectory( "configs" )
        if ( this.configDir.exists( "${configId}.json" ) ) {

            try {

                Map<String, Object> configSchema =
                        JSON.readJSONAsMap( schemaStream, this.jsonErrorHandler )

                StructMap loaded = new StructMap(
                        JSON.readJSONAsMap( this.configDir.getFile( "${configId}.json" ).createInputStream(), this.jsonErrorHandler )
                )

                MapJsonDocValidator validator = new MapJsonDocValidator( validStructure: configSchema )
                validator.validate( loaded )

                StructMap defaultConfig = new StructMap( JSON.readJSONAsMap( defaultConfigStream, this.jsonErrorHandler ) )

                config = createConfig( configId, defaultConfig )
                config.putAll( loaded )

            }
            catch ( APSConfigException ce ) {
                this.logger.error( "Failed to load config with id '${configId}'!", ce )
                return
            }
        }
        else {
            // Create from default
            config = createConfig( configId, defaultConfig )
            config.putAll( defaultConfig )
            saveConfig( )
        }

        config
    }

    // Consider moving this to ConfigManager!
    private static APSConfigProvider createConfig( String configId, StructMap defaultConfig ) {
        new APSConfigProvider(
                apsConfigId: configId,
                defaultConfig: defaultConfig
        )
    }

    void saveConfig() {
        OutputStream os = this.configDir.getFile( "${apsConfigId}.json" ).createOutputStream()
        try {
            JSON.writeMapAsJSON( this, os )
        }
        finally {
            os.close()
        }
    }

}
