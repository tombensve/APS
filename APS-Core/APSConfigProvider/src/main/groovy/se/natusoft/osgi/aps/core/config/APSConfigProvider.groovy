package se.natusoft.osgi.aps.core.config

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.osgi.framework.Bundle
import se.natusoft.osgi.aps.api.core.config.APSConfig
import se.natusoft.osgi.aps.api.core.filesystem.model.APSDirectory
import se.natusoft.osgi.aps.api.core.filesystem.model.APSFilesystem
import se.natusoft.osgi.aps.api.core.filesystem.service.APSFilesystemService
import se.natusoft.osgi.aps.model.APSHandler
import se.natusoft.osgi.aps.core.lib.MapJsonDocValidator
import se.natusoft.osgi.aps.core.lib.StructMap
import se.natusoft.osgi.aps.exceptions.APSConfigException
import se.natusoft.osgi.aps.exceptions.APSIOException
import se.natusoft.osgi.aps.json.JSON
import se.natusoft.osgi.aps.json.JSONErrorHandler
import se.natusoft.osgi.aps.tools.APSLogger

/**
 * This class represents one individual configuration.
 */
@CompileStatic
@TypeChecked
class APSConfigProvider extends StructMap implements APSConfig {

    //
    // Properties
    //

    /** The logger to use. */
    APSLogger logger

    /** The Id of this config. */
    String apsConfigId

    /** For notifying about sync request. */
    Closure syncNotifier

    /** Filesystem access that won't go away on redeploy. */
    APSFilesystemService fsService

    /** Bundle owning this configuration. */
    Bundle owner

    /** The bundle path to the config schema. */
    String schemaPath

    /** The bundle path to the default configuration. */
    String defaultConfigPath

    //
    // Private Members
    //

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
     * @param structPath The structPath to lookup. Null or blank will return the whole root config Map, which is also a StructMap.
     * @param valueHandler The handler receiving the looked up value. Do not make any assumptions on what thread will call the handler.
     */
    @Override
    void lookup( String structPath, APSHandler<Object> valueHandler ) {
        if ( structPath == null || structPath.isEmpty() ) {
            valueHandler.handle( this )
        }
        else {
            Object value = super.lookup( structPath )
            if ( value == null ) {
                value = this.defaultConfig.lookup( structPath )
                if ( value != null ) {
                    provide( structPath, value )
                    // No, we should not notify on this!
                }
            }

            valueHandler.handle( value )
        }
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

        saveConfig()
    }

    /**
     * Triggers a notification of config being updated on cluster bus.
     */
    @SuppressWarnings("GroovyUnusedDeclaration")
    void notifyUpdate() {
        this.syncNotifier()
    }

    /**
     * Make sure we have a config dir in our filesystem. If not create it. this.configDir will be updated.
     */
    private void setupConfigDir() {
        if (this.configDir == null) {
            APSFilesystem fs = this.fsService.getFilesystem( "aps-config-provider" )
            if ( fs == null ) {

                fs = this.fsService.createFilesystem( "aps-config-provider" )
            }

            APSDirectory root = fs.getRootDirectory()
            if ( !root.exists( "configs" ) ) {

                root.createDir( "configs" )
            }

            this.configDir = fs.getDirectory( "configs" )
        }
    }

    /**
     * Loads a configuration.
     */
    void loadConfig() {

        setupConfigDir(  )

        // Load default config from bundle.

        InputStream defaultConfigStream = null
        try {
            defaultConfigStream = this.owner.getResource( this.defaultConfigPath ).openStream()
            this.defaultConfig = new StructMap( JSON.readJSONAsMap( defaultConfigStream, this.jsonErrorHandler ) )
        }
        catch ( IOException ioe ) {
            this.logger.error( "Failed to load default configuration from bundle: ${this.owner.symbolicName}!", ioe )
            this.defaultConfig = null
        }
        finally {
            if ( defaultConfigStream != null ) {
                defaultConfigStream.close()
            }
        }

        // Load config schema from bundle.

        InputStream schemaStream = this.owner.getResource( this.schemaPath ).openStream()
        Map<String, Object> configSchema = null
        try {
            configSchema = JSON.readJSONAsMap( schemaStream, this.jsonErrorHandler )
        }
        catch ( IOException ioe ) {
            this.logger.error( "Failed to load configuration schema! Config will not be verified!", ioe )
        }
        finally {
            schemaStream.close()
        }

        // Try load local config.

        if ( this.configDir.exists( "${this.apsConfigId}.json" ) ) {

            try {

                StructMap loaded = new StructMap(
                        JSON.readJSONAsMap( this.configDir.getFile( "${this.apsConfigId}.json" ).createInputStream(),
                                            this.jsonErrorHandler )
                )

                if ( configSchema != null ) {
                    MapJsonDocValidator validator = new MapJsonDocValidator( validStructure: configSchema )
                    validator.validate( loaded )
                }

                clear()
                putAll( loaded )
            }
            catch ( IOException ioe ) {
                throw new APSIOException( "Failed to load configuration for bundle ${this.owner.symbolicName}!", ioe )
            }
            finally {
                schemaStream.close()
            }
        }
        else {
            // Create from default
            clear()
            if ( this.defaultConfig != null ) {
                putAll( this.defaultConfig )
                saveConfig()
            }
        }
    }

    /**
     * Saves current configuration.
     */
    void saveConfig() {

        setupConfigDir(  )

        OutputStream os = this.configDir.getFile( "${apsConfigId}.json" ).createOutputStream()
        try {
            JSON.writeMapAsJSON( this, os )
        }
        catch ( IOException ioe ) {
            throw new APSIOException( "Failed to save configuration for bundle ${this.owner.symbolicName}!", ioe )
        }
        finally {
            os.close()
        }
    }

}
