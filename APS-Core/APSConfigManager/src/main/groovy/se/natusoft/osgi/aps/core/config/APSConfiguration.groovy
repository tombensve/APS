/*
 *
 * PROJECT
 *     Name
 *         APSConfigManager
 *
 *     Code Version
 *         1.0.0
 *
 * COPYRIGHTS
 *     Copyright (C) 2012 by Natusoft AB All rights reserved.
 *
 * LICENSE
 *     Apache 2.0 (Open Source)
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 * AUTHORS
 *     tommy ()
 *         Changes:
 *         2018-05-25: Created!
 *
 */
package se.natusoft.osgi.aps.core.config

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.osgi.framework.Bundle
import se.natusoft.docutations.Implements
import se.natusoft.docutations.NotNull
import se.natusoft.docutations.Nullable
import se.natusoft.osgi.aps.types.APSSerializableData
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
import se.natusoft.osgi.aps.types.APSHandler
import se.natusoft.osgi.aps.types.APSResult
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

//    /** Save of config dir for when saving. */
//    private APSDirectory configDir

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

        saveConfigToDisk()

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
    private void withConfigDir( Closure doCode ) {

        // Note: If the APSFileSystemService is not available yet, this operation will be queued and
        //       called when it is available. So this can potentially be delayed. This is due to the
        //       nonBlocking = true flag on the APSServiceTracker wrapped service injected to ConfigManager
        //       and passed to this class. This is also why the APSFilesystemService API is reactive.
        //       Otherwise this wouldn't work. This is to avoid having multiple threads with locked threads
        //       just waiting for things.
        this.fsService.getFilesystem( "aps-config-provider" ) { APSResult<APSFilesystem> res ->

            if ( res.success() ) {

                APSFilesystem fs = res.result().content()

                APSDirectory root = fs.getRootDirectory()

                if ( !root.exists( "configs" ) ) {

                    root.createDir( "configs" )
                }

                APSDirectory configDir = fs.getDirectory( "configs" )

                doCode( configDir )
            }
            else {
                this.logger.error( res.failure().message, res.failure() )
            }
        }

    }

    /**
     * Validates config if validator exists.
     *
     * @param config The config to validate.
     */
    private validateConfig( Map<String, Object> config ) {

        if ( config != null ) {
            if ( this.configValidator != null ) {

                this.configValidator.validate( config )
            }
            else {

                this.logger.warn "Config '${ this.apsConfigId }' has not schema to validate against!"
            }

        }
        else {
            this.logger.error "Attempted validation of null config!"
        }
    }

    /**
     * Loads a configuration.
     */
    void loadConfig() {

        this.clear()

        // Strangeness: The code below loading the schema and default config from the bundle completely
        // fail if run within the "withConfigDir" closure below. Outside of it, it works fine. I fail
        // to see a reason for that. The side effect is that there might be a delay between the loading
        // of the default config and the last saved config.

        // Load config schema from bundle.
        this.logger.info "(configId:${this.apsConfigId}):Loading schema: ${ this.schemaPath }"
        URL schemaUrl = this.owner.getResource( this.schemaPath )
        this.logger.debug ">>>> schemaUrl: ${schemaUrl}"

        InputStream schemaStream = schemaUrl.openStream(  )

        if (schemaStream != null) {
            try {

                this.configSchema = JSON.readJSONAsMap( schemaStream, this.jsonErrorHandler )
                this.configValidator = new MapJsonDocSchemaValidator( validStructure: configSchema )
                this.logger.info "Loaded schema: ${ this.configSchema }"

            }
            catch ( IOException ioe ) {

                this.logger.error( "Failed to load configuration schema! Config will not be verified!", ioe )
            }
            finally {

                if ( schemaStream != null ) schemaStream.close()
            }
        }
        else {
            this.logger.error "(configId:${this.apsConfigId}):No configuration schema is available!"
        }

        // Load default config from bundle.

        InputStream defaultConfigStream = null
        try {

            defaultConfigStream = this.owner.getResource( this.defaultConfigPath ).openStream()
            this.defaultConfig = new StructMap( JSON.readJSONAsMap( defaultConfigStream, this.jsonErrorHandler ) )
        }
        catch ( IOException ioe ) {

            this.logger.error(
                    "(configId:${this.apsConfigId}):Failed to load default configuration from bundle: ${ this.owner.symbolicName }!", ioe )

            this.defaultConfig = null
        }
        finally {

            if ( defaultConfigPath != null ) defaultConfigStream.close()
        }

        this.logger.info "(configId:${this.apsConfigId}):Loaded default config: ${ this.defaultConfig.toString() }"

        // Validate default config.
        validateConfig( this.defaultConfig )

        // We populate our config with the default values first and then let the last saved config overwrite.
        // If the default config have a new value it will then be available since the saved config will not
        // have it until next save.
        putAll( this.defaultConfig )

        // Try load local config.

        // IMPORTANT: This closure can potentially be run at a later time if the APSFilesytemService is not
        //            available yet!
        withConfigDir() { APSDirectory configDir ->

            if ( configDir.exists( "${ this.apsConfigId }.json" ) ) {

                try {

                    StructMap loaded = new StructMap(

                            JSON.readJSONAsMap(

                                    configDir.getFile( "${ this.apsConfigId }.json" ).createInputStream(),
                                    this.jsonErrorHandler
                            )
                    )

                    validateConfig( loaded )

                    putAll( loaded )
                }
                catch ( IOException ioe ) {

                    throw new APSIOException( "Failed to load configuration for bundle ${ this.owner.symbolicName }!",
                            ioe )
                }
                finally {

                    schemaStream.close()
                }

                validateConfig( this )
            }
            else { // We have no previous config saved.

                saveConfigToDisk()
            }
        }

    }

    /**
     * Saves current configuration to disk.
     */
    void saveConfigToDisk() {

        withConfigDir() { APSDirectory configDir ->

            OutputStream os = configDir.getFile( "${ apsConfigId }.json" ).createOutputStream()
            try {

                JSON.writeMapAsJSON( this, os )
            }
            catch ( IOException ioe ) {

                throw new APSIOException( "Failed to save configuration for bundle ${ this.owner.symbolicName }!", ioe )
            }
            finally {

                if ( os != null ) os.close()
            }
        }
    }

    /**
     * @return A Serializable object of the type provided by getSerializedType().
     */
    @Override
    @Implements( APSSerializableData.class )
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
    @Implements( APSSerializableData.class )
    void fromDeserialized( Serializable serializable ) {

        clear()
        putAll( serializable as Map<String, Object> )
    }

    /**
     * @return The serialized type.
     */
    @Override
    @Implements( APSSerializableData.class )
    Class getSerializedType() {

        LinkedHashMap.class
    }
}
