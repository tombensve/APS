package se.natusoft.osgi.aps.core.config

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.osgi.framework.Bundle
import se.natusoft.docutations.Implements
import se.natusoft.docutations.NotNull
import se.natusoft.docutations.Nullable
import se.natusoft.osgi.aps.api.core.APSSerializableData
import se.natusoft.osgi.aps.api.core.config.APSConfig
import se.natusoft.osgi.aps.api.core.filesystem.model.APSDirectory
import se.natusoft.osgi.aps.api.core.filesystem.model.APSFilesystem
import se.natusoft.osgi.aps.api.core.filesystem.service.APSFilesystemService
import se.natusoft.osgi.aps.core.lib.MapJsonDocSchemaValidator
import se.natusoft.osgi.aps.core.lib.StructMap
import se.natusoft.osgi.aps.exceptions.APSConfigException
import se.natusoft.osgi.aps.exceptions.APSIOException
import se.natusoft.osgi.aps.json.JSON
import se.natusoft.osgi.aps.json.JSONErrorHandler
import se.natusoft.osgi.aps.model.APSHandler
import se.natusoft.osgi.aps.util.APSLogger

/**
 * This class represents one individual configuration.*/
@CompileStatic
@TypeChecked
class APSConfiguration extends StructMap implements APSConfig, APSSerializableData {

    //
    // Properties
    //

    /** The logger to use. */
    @NotNull
    APSLogger logger

    /** The Id of this config. */
    @NotNull
    String apsConfigId

    /** For notifying about sync request. */
    @NotNull
    Closure syncNotifier

    /** For updating cluster with locally updated config. */
    @NotNull
    Closure saveToCluster

    /** Filesystem access that won't go away on redeploy. */
    @NotNull
    APSFilesystemService fsService

    /** Bundle owning this configuration. */
    @NotNull
    Bundle owner

    /** The bundle path to the config schema. */
    @NotNull
    String schemaPath

    /** The bundle path to the default configuration. */
    @NotNull
    String defaultConfigPath

    //
    // Private Members
    //

    /** The schema for the config. */
    private Map<String, Object> configSchema = null

    /** A validator for validating agains configSchema. */
    private MapJsonDocSchemaValidator configValidator

    /** Save of config dir for when saving. */
    private APSDirectory configDir

    /** The default config. */
    private StructMap defaultConfig

    /** For when parsing JSON. */
    private JSONErrorHandler jsonErrorHandler = new JSONErrorHandler() {

        @Override
        void warning( String message ) {
            logger.warn( message )
        }

        @Override
        void fail( String message, Throwable cause ) throws RuntimeException {

            logger.error( message, cause )
            throw new APSConfigException( message, cause )
        }
    }

    //
    // Init
    //

    /**
     * Call this after setting all bean properties. This is designed for Groovy bean constructor.
     *
     * From Java do:
     *     APSConfiguration config = new APSConfiguration();
     *     config.setLogger(logger);
     *     config.setApsConfigId("someId");
     *     ...
     *     config.init();
     *
     * From Groovy do:
     *
     *     APSConfiguration config = new APSConfiguration(
     *         logger: logger,
     *         apsConfigId: "someId",
     *         ...
     *     ).init()
     */
    APSConfiguration init() {
        loadConfig()

        this
    }

    //
    // Methods
    //

    /**
     * Looks up the value of a specified struct Path.
     *
     * @param structPath The structPath to lookup. Null or blank will return the whole root config Map, which is also
     *        a StructMap.
     * @param valueHandler The handler receiving the looked up value. Do not make any assumptions on what thread will
     *        call the handler.
     */
    @Override
    @Implements( APSConfig.class )
    void lookupr( @NotNull String structPath, @NotNull APSHandler<Object> valueHandler ) {

        valueHandler.handle( lookup( structPath ) )
    }

    /**
     * Returns the value at the specified struct path.
     *
     * @param structPath The struct path to lookup.
     *
     * @return value or null.
     */
    @Override
    @Implements( APSConfig.class )
    Object lookup( String structPath ) {

        Object res = this

        if ( structPath != null && !structPath.isEmpty() ) {

            Object value = super.lookup( structPath )

            if ( value == null ) {

                value = this.defaultConfig.lookup( structPath )

                if ( value != null ) {

                    provide( structPath, value )
                    // No, we should not notify on this!
                }
            }

            res = value
        }

        res
    }

    /**
     * provides a value.
     *
     * @param structPath The value path.
     * @param value The value.
     */
    @Override
    @Implements( APSConfig.class )
    void provide( @NotNull String structPath, @Nullable Object value ) {

        super.provide( structPath, value )

        saveConfig()

        this.saveToCluster.call( this )

        notifyUpdate()
    }

    /**
     * Triggers a notification of config being updated on cluster bus.
     */
    @SuppressWarnings( "GroovyUnusedDeclaration" )
    void notifyUpdate() {

        this.syncNotifier.call( this )
    }

    /**
     * Make sure we have a config dir in our filesystem. If not create it. this.configDir will be updated.
     */
    private void setupConfigDir() {

        if ( this.configDir == null ) {

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
     * Validates config if validator exists.
     *
     * @param config The config to validate.
     */
    private validateConfig( Map<String, Object> config ) {

        if ( this.configValidator != null ) {

            this.configValidator.validate( config )
        }
        else {

            this.logger.warn( "Config '${ this.apsConfigId }' has not schema to validate against!" )
        }
    }

    /**
     * Loads a configuration.
     */
    void loadConfig() {

        this.clear()

        setupConfigDir()

        // Load config schema from bundle.

        InputStream schemaStream = this.owner.getResource( this.schemaPath ).openStream()

        try {

            this.configSchema = JSON.readJSONAsMap( schemaStream, this.jsonErrorHandler )
            this.configValidator = new MapJsonDocSchemaValidator( validStructure: configSchema )

        }
        catch ( IOException ioe ) {

            this.logger.error( "Failed to load configuration schema! Config will not be verified!", ioe )
        }
        finally {

            schemaStream.close()
        }

        // Load default config from bundle.

        InputStream defaultConfigStream = null
        try {

            defaultConfigStream = this.owner.getResource( this.defaultConfigPath ).openStream()
            this.defaultConfig = new StructMap( JSON.readJSONAsMap( defaultConfigStream, this.jsonErrorHandler ) )
        }
        catch ( IOException ioe ) {

            this.logger.error(
                    "Failed to load default configuration from bundle: ${ this.owner.symbolicName }!", ioe )

            this.defaultConfig = null
        }
        finally {

            defaultConfigStream.close()
        }

        // Validate default config.
        validateConfig( this.defaultConfig )

        // Try load local config.

        if ( this.configDir.exists( "${ this.apsConfigId }.json" ) ) {

            try {

                StructMap loaded = new StructMap(

                        JSON.readJSONAsMap(

                                this.configDir.getFile( "${ this.apsConfigId }.json" ).createInputStream(),
                                this.jsonErrorHandler
                        )
                )

                validateConfig( loaded )

                clear()
                putAll( loaded )
            }
            catch ( IOException ioe ) {

                throw new APSIOException( "Failed to load configuration for bundle ${ this.owner.symbolicName }!", ioe )
            }
            finally {

                schemaStream.close()
            }

            validateConfig( this )
        }
        else {
            // Create from default
            clear()

            if ( this.defaultConfig != null ) {

                putAll( this.defaultConfig )
                validateConfig( this )
                saveConfig()
            }
        }
    }

    /**
     * Saves current configuration.
     */
    void saveConfig() {
        saveConfigToDisk(  )
    }

    /**
     * Saves current configuration to disk.
     */
    void saveConfigToDisk() {

        setupConfigDir()

        OutputStream os = this.configDir.getFile( "${ apsConfigId }.json" ).createOutputStream()
        try {

            JSON.writeMapAsJSON( this, os )
        }
        catch ( IOException ioe ) {

            throw new APSIOException( "Failed to save configuration for bundle ${ this.owner.symbolicName }!", ioe )
        }
        finally {

            os.close()
        }
    }

    void saveConfigToCluster() {

    }

    /**
     * @return A Serializable object of the type provided by getSerializedType().
     */
    @Override
    Serializable toSerializable() {

        Map<String, Object> serializableContent = [:]
        serializableContent.putAll( this )

        serializableContent
    }

    /**
     * Receives a deserialized object of the type provided by getSerializedType().
     *
     * @param serializable The deserialized object received.
     */
    @Override
    void fromDeserialized( Serializable serializable ) {

        clear()
        putAll( serializable as Map<String, Object> )
    }

    /**
     * @return The serialized type.
     */
    @Override
    Class getSerializedType() {

        LinkedHashMap.class
    }
}
