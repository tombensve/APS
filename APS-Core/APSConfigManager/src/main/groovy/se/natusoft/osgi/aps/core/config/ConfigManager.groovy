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
import org.osgi.framework.BundleContext
import org.osgi.framework.ServiceRegistration
import se.natusoft.docutations.NotNull
import se.natusoft.docutations.Note
import se.natusoft.docutations.Nullable
import se.natusoft.docutations.Unbreakable
import se.natusoft.osgi.aps.activator.annotation.BundleStop
import se.natusoft.osgi.aps.activator.annotation.Initializer
import se.natusoft.osgi.aps.activator.annotation.Managed
import se.natusoft.osgi.aps.activator.annotation.OSGiService
import se.natusoft.osgi.aps.api.core.config.APSConfig
import se.natusoft.osgi.aps.api.core.filesystem.service.APSFilesystemService
import se.natusoft.osgi.aps.api.core.store.APSLockableDataStoreService
import se.natusoft.osgi.aps.api.messaging.APSMessage
import se.natusoft.osgi.aps.api.messaging.APSMessageSender
import se.natusoft.osgi.aps.api.messaging.APSMessageSubscriber
import se.natusoft.osgi.aps.types.APSLockable
import se.natusoft.osgi.aps.types.APSResult
import se.natusoft.osgi.aps.types.APSUUID
import se.natusoft.osgi.aps.types.ID
import se.natusoft.osgi.aps.util.APSLogger

/**
 * This class actually manages all configurations.
 *
 * ## Rules (aps-104)
 *
 * - Try fetching config from cluster, If not in cluster, fetch from local storage and publish in cluster.
 * - Subscribe to config update messages and fetch and store config locally.
 * - Store config changes locally and in cluster and publish update message.
 * - Publish APSConfig instances as OSGi services with config id in properties.
 **/
@CompileStatic
@TypeChecked
class ConfigManager {

    //
    // Constants
    //

    //
    // Private Members
    //

    @Managed
    private BundleContext bundleContext

    @Managed( loggingFor = "aps-config-provider:config-manager" )
    private APSLogger logger

    @OSGiService( additionalSearchCriteria = "(messaging-clustered=true)", nonBlocking = true )
    private APSMessageSender<Map<String, Object>> messageSender

    @OSGiService( additionalSearchCriteria = "(messaging-clustered=true)", nonBlocking = true )
    private APSMessageSubscriber<Map<String, Object>> messageSubscriber

    /** Filesystem access that won't go away on redeploy. */
    @OSGiService( timeout = "15 sec", nonBlocking = true )
    private APSFilesystemService fsService

    @OSGiService( additionalSearchCriteria = "(service-persistence-scope=clustered)", nonBlocking = true )
    private APSLockableDataStoreService dataStoreService

    /** Holds service registrations per configuration id. */
    private Map<String, ServiceRegistration> regs = [:]

    /** Holds published instances per configuration id. */
    private Map<String, APSConfiguration> providers = [:]

    /** A unique id for this manager. This is passed along in bus publications, so that we can ignore messages from
     * self. */
    private String confMgrId = UUID.randomUUID().toString()

    private static final ID SUBSCRIBER_ID = new APSUUID()

    //
    // Initializer / Shutdown
    //

    @SuppressWarnings( "GroovyUnusedDeclaration" )
    @Note( "Called through reflection by APSActivator. Gets called after injections are done." )
    @Initializer
    void init() {

        this.messageSubscriber.subscribe( APSConfig.CONFIG_EVENT_DESTINATION, SUBSCRIBER_ID ) { APSResult res ->

            if ( !res.success() ) {

                this.logger.error( "", res.failure() )
            }

        } {

            APSMessage<Map<String, Object>> message ->

                Map<String, Object> content = message.content()

                if ( content.messageType == "CONFIG_UPDATED" ) {

                    // Ignore if message is from ourselves.
                    if ( content.configMgrId != this.confMgrId ) {

                        String apsConfigId = content.apsConfigId

                        // In this case the configuration in the cluster have been updated, so we
                        // want to take it from the cluster and save locally. Do note that the
                        // contents of the local in memory held APSConfiguration is updated first
                        // and then it is saved to disk.
                        saveLocalFromCluster( apsConfigId, null )
                    }
                }
                else {

                    this.logger.error( "Got unexpected message type on '${ APSConfig.CONFIG_EVENT_DESTINATION }': " +
                            "${ content.messageType }!" )
                }
        }
    }

    @SuppressWarnings( "GroovyUnusedDeclaration" )
    @Note( "Called through reflection by APSActivator." )
    @BundleStop
    void shutDown() {

        this.messageSubscriber.unsubscribe( SUBSCRIBER_ID ) { APSResult res ->

            if ( !res.success() ) {

                this.logger.error( "Failed to unsubscribe for config messages!", res.failure() )
            }
        }

        this.regs.each { String key, ServiceRegistration svcReg -> svcReg.unregister() }

        this.regs = null
        this.providers = null
    }

    //
    // Methods
    //

    /**
     * This makes the config manager manage the specified configuration for a bundle.
     *
     * @param configId The unique id of the configuration.
     * @param owner The bundle owning the configuration.
     * @param schemaPath The path within the bundle to the schema for the configuration. It will be
     *                   validated.
     * @param defaultConfigPath The path within the bundle to the default configuration which will
     *                          be used the first time the configuration is seen.
     */
    @Unbreakable
    void addManagedConfig( @NotNull String configId, @NotNull Bundle owner, @NotNull String schemaPath,
                           @NotNull String defaultConfigPath ) {

        try {

            APSConfiguration provider = new APSConfiguration(

                    logger: this.logger,
                    apsConfigId: configId,
                    fsService: this.fsService,
                    owner: owner,
                    schemaPath: schemaPath,
                    defaultConfigPath: defaultConfigPath,

                    // This will be called when a local config value is changed.
                    syncNotifier: { APSConfiguration configProvider ->

                        updateOtherNodesOfChangedConfig( configProvider )
                    },
                    saveToCluster: { APSConfiguration configProvider ->
                        loadClusterFromLocal( configProvider.apsConfigId as String )
                    }
            ).init()

            this.providers[ configId ] = provider

            initializeConfig( configId ) // This will populate the provider from cluster or from disk.

            Properties confProps = new Properties()
            confProps[ "apsConfigId" ] = configId

            ServiceRegistration serviceRegistration = this.bundleContext.registerService(

                    APSConfig.class.name, provider, confProps
            )

            regs[ configId ] = serviceRegistration

        }
        catch ( Exception e ) {

            this.logger.error( "Failed to load config '${ configId }' for bundle '${ owner.symbolicName }'!", e )
        }
    }

    /**
     * Removes a managed configuration.
     *
     * @param configId The id of the configuration to remove.
     */
    void removeManagedConfig( @NotNull String configId ) {

        ServiceRegistration svcReg = this.regs.remove( configId )
        if ( svcReg != null ) {

            svcReg.unregister()
        }

        APSConfiguration provider = this.providers.remove( configId )
        if ( provider != null ) {

            provider.saveConfigToDisk()
        }
    }

    /**
     * This publishes the id of the configuration only since when a config is updated
     * the cluster instance of the config is also updated, and it is the cluster instance
     * that is always used. But when this is received the cluster instance should be copied
     * to local disk store. Each node needs a local copy of the cluster version. Whichever
     * node is started first provides the original cluster version.
     */
    private void updateOtherNodesOfChangedConfig( APSConfiguration configProvider ) {

        this.messageSender.send(

                APSConfig.CONFIG_EVENT_DESTINATION,
                [
                        messageType: "CONFIG_UPDATED",
                        apsConfigId: configProvider.apsConfigId,
                        configMgrId: this.confMgrId

                ] as Map<String, Object>

        ) { APSResult res ->

            if ( !res.success() ) {

                logger.error( "Failed to publish new configuration!", res.failure() )
            }
        }
    }

    /**
     * Initializes the configuration by trying to load from cluster and save locally, and if that fails
     * loading from local and store in cluster.
     *
     * @param configId The id of the configuration to initialize.
     */
    private void initializeConfig( @NotNull String configId ) {

        saveLocalFromCluster( configId ) { ->

            this.logger.info( "[${ configId }] No cluster config found, loading from disk ..." )

            // This is executed when nothing in cluster.
            loadClusterFromLocal( configId )

            this.logger.info( "[${ configId }] ... and populated cluster!" )
        }
    }

    /**
     * Loads the local config and stores it in the cluster.
     *
     * @param configId The id of the configuration to load.
     */
    private void loadClusterFromLocal( @NotNull String configId ) {

        String clusterKey = "aps.config.${ configId }"

        APSConfiguration provider = this.providers[ configId ]

        provider.loadConfig()

        this.dataStoreService.lock( clusterKey ) { APSResult<APSLockable.APSLock> lock ->

            if ( lock.success() ) {
                this.dataStoreService.store( clusterKey, provider.toSerializable() ) { APSResult res ->

                    if ( !res.success() ) {

                        this.logger.error( "Failed to store loaded config[${ configId }] in cluster!", res.failure() )
                    }
                    else {

                        this.logger.info( "[${ configId }]: Stored config in cluster!" )
                    }
                }
            }
        } // Automatic unlock!
    }

    /**
     * Updates the local configuration from the one in the cluster.
     *
     * @param configId The id of the config to update.
     * @param onNoClusterConf A closure to call if there were no configuration in the cluster.
     */
    private void saveLocalFromCluster( @NotNull String configId, @Nullable Closure onNoClusterConf ) {

        String clusterKey = "aps.config.${ configId }"

        APSConfiguration provider = this.providers[ configId ]

        this.dataStoreService.lock( clusterKey ) { APSResult<APSLockable.APSLock> lock ->

            if ( lock.success() ) {

                this.logger.info( "[${ configId }]: Successfully locked cluster store ..." )

                this.dataStoreService.retrieve( clusterKey ) { APSResult<Map<String, Object>> res ->

                    Map<String, Object> conf = null

                    if ( res.success() ) {

                        conf = res.result().content()

                        if ( conf != null ) {

                            provider.fromDeserialized( conf as Serializable )
                            provider.saveConfigToDisk()

                            this.logger.info( "[${ configId }]: Took cluster config!" )

                        }
                    }
                    else {
                        this.logger.error( "Failed lookup of config for id '${ configId }'!", res.failure() )
                    }

                    if ( conf == null && onNoClusterConf != null ) {
                        onNoClusterConf()
                    }
                }
            }
            else {
                this.logger.error( "Failed to get a lock to cluster store!", lock.failure() )
            }
        } // Automatic unlock!
    }

}
