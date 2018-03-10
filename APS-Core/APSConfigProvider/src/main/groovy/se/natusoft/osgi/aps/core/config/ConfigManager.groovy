package se.natusoft.osgi.aps.core.config

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.osgi.framework.Bundle
import org.osgi.framework.ServiceRegistration
import se.natusoft.docutations.Note
import se.natusoft.osgi.aps.api.core.APSLockable
import se.natusoft.osgi.aps.api.core.config.APSConfig
import se.natusoft.osgi.aps.api.core.filesystem.service.APSFilesystemService
import se.natusoft.osgi.aps.api.core.platform.service.APSExecutionService
import se.natusoft.osgi.aps.api.core.store.APSLockableDataStoreService
import se.natusoft.osgi.aps.api.messaging.APSMessage
import se.natusoft.osgi.aps.api.messaging.APSMessageService
import se.natusoft.osgi.aps.api.messaging.APSMessagePublisher
import se.natusoft.osgi.aps.model.APSResult
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.annotation.activator.BundleStop
import se.natusoft.osgi.aps.tools.annotation.activator.Initializer
import se.natusoft.osgi.aps.tools.annotation.activator.Managed
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiService

import static se.natusoft.osgi.aps.model.APSProperties.props

/**
 * This class actually manages all configurations.
 */
@CompileStatic
@TypeChecked
class ConfigManager {
    // TODO: Publish APSConfig as service.
    //
    // Constants
    //

    //
    // Private Members
    //

    @Managed(loggingFor = "aps-config-provider:config-manager")
    private APSLogger logger

    @OSGiService(additionalSearchCriteria = "(messaging-clustered=true)", nonBlocking = true)
    private APSMessageService<Map<String, Object>> messageService

    /** Filesystem access that won't go away on redeploy. */
    @OSGiService(timeout = "15 sec")
    private APSFilesystemService fsService

    @OSGiService(additionalSearchCriteria = "(service-persistence-scope=clustered)", nonBlocking = true)
    private APSLockableDataStoreService dataStoreService

    @OSGiService(nonBlocking = true)
    private APSExecutionService apsExecutor

    /** Holds service registrations per configuration id. */
    private Map<String, ServiceRegistration> regs = [ : ]

    /** Holds published instances per configuration id. */
    private Map<String, APSConfigProvider> providers = [ : ]

    /** A unique id for this manager. This is passed along in bus publishings, so that we can ignore messages from self. */
    private String confMgrId = UUID.randomUUID().toString()

    //
    // Initializer / Shutdown
    //

    @SuppressWarnings("GroovyUnusedDeclaration")
    @Note("Called through reflection by APSActivator.")
    @Initializer
    void init() {
        this.messageService.subscribe( APSConfig.CLUSTER_CONFIG_REFRESH_ADDRESS ) { APSMessage<Map<String, Object>> apsMsg ->

            Map<String, Object> message = apsMsg.content()

            String configId = message[ 'apsConfigId' ]

            if ( configId != null ) {

                saveLocalFromCluster( configId ) {
                    this.logger.error( "Have been notified to save config locally, but no cluster config was found!" )
                }

                publishConfigAvailable( configId )
            }
            else {
                this.logger.error( "Got null 'apsConfigId' from other cluster member!" )
            }
        }

        this.messageService.subscribe( APSConfig.CLUSTER_CONFIG_REQUEST_ADDRESS ) { APSMessage<Map<String, Object>> apsMsg ->

            Map<String, Object> message = apsMsg.content()

            String configId = message[ 'apsConfigId' ]

            publishConfigAvailable( configId )
        }
    }

    @SuppressWarnings("GroovyUnusedDeclaration")
    @Note("Called through reflection by APSActivator.")
    @BundleStop
    void shutDown() {
        this.messageService.unsubscribe( APSConfig.CLUSTER_CONFIG_REFRESH_ADDRESS )

        this.regs.each { String key, ServiceRegistration svcReg ->
            svcReg.unregister()
        }

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
    void addManagedConfig( String configId, Bundle owner, String schemaPath, String defaultConfigPath ) {

        try {

            APSConfigProvider provider = new APSConfigProvider(
                    logger: this.logger,
                    apsConfigId: configId,
                    fsService: this.fsService,
                    owner: owner,
                    schemaPath: schemaPath,
                    defaultConfigPath: defaultConfigPath,
                    syncNotifier: { APSConfigProvider configProvider ->

                        this.messageService.publisher( APSConfig.CLUSTER_CONFIG_REFRESH_ADDRESS ) { APSMessagePublisher<Map<String, Object>> publisher ->

                            publisher.publish( [
                                    apsConfigId: configProvider.apsConfigId,
                                    confMgrId  : this.confMgrId
                            ] )
                        }
                    }
            )

            this.providers[ configId ] = provider

            initializeConfig( configId ) // This will populate the provider from cluster or from disk.

            publishConfigAvailable( configId )
        }
        catch ( Exception e ) {
            this.logger.error( "Failed to load config '${configId}' for bundle '${owner.symbolicName}'!", e )
        }
    }

    /**
     * Removes a managed configuration.
     *
     * @param configId The id of the configuration to remove.
     */
    void removeManagedConfig( String configId ) {

        ServiceRegistration svcReg = this.regs.remove( configId )
        if ( svcReg != null ) {
            svcReg.unregister()
        }

        APSConfigProvider provider = this.providers.remove( configId )
        if ( provider != null ) {
            provider.saveConfig()
        }
    }

    /**
     * Publishes a message containing the specified config.
     *
     * @param configId The id of the config to publish.
     */
    void publishConfigAvailable( String configId ) {

        APSConfigProvider provider = this.providers[ configId ]

        if ( provider != null ) {
            this.messageService.publisher(

                    props() >> APSMessageService.TARGET >> ( APSConfig.APS_CONFIG_AVAILABLE_ADDRESS_START + configId )

            ) { APSMessagePublisher<Map<String, Object>> publisher ->

                publisher.publish( [

                        apsConfigId: configId as Object,
                        apsConfig  : provider

                ] ) { APSResult res ->

                    if ( !res.success() ) {
                        this.logger.error( "Failed to publish configuration of id '${configId}'!", res.failure() )
                    }
                }
            }
        }
        else {
            this.logger.error( "Tried to publish a non existing config: '${configId}'" )
        }
    }

    /**
     * Initializes the configuration by trying to load from cluster and save locally, and if that fails
     * loading from local and store in cluster.
     *
     * @param configId The id of the configuration to initialize.
     */
    private void initializeConfig( String configId ) {

        saveLocalFromCluster( configId ) { ->

            // This is executed when nothing in cluster.
            this.apsExecutor.submit() { ->
                loadClusterFromLocal( configId )
            }
        }
    }

    /**
     * Loads the local config and stores it in the cluster.
     *
     * @param configId The id of the configuration to load.
     */
    private void loadClusterFromLocal( String configId ) {
        String clusterKey = "aps.config.${configId}"

        APSConfigProvider provider = this.providers[ configId ]

        provider.loadConfig()

        this.dataStoreService.lock( clusterKey ) { APSResult<APSLockable.APSLock> lock ->
            if ( lock.success() ) {
                this.dataStoreService.store( clusterKey, provider ) { APSResult res ->
                    if ( !res.success() ) {
                        this.logger.error( "Failed to store loaded config in cluster!", res.failure() )
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
    private void saveLocalFromCluster( String configId, Closure onNoClusterConf ) {
        String clusterKey = "aps.config.${configId}"

        APSConfigProvider provider = this.providers[ configId ]

        this.dataStoreService.lock( clusterKey ) { APSResult<APSLockable.APSLock> lock ->
            if ( lock.success() ) {

                this.dataStoreService.retrieve( clusterKey ) { APSResult res ->

                    Object conf = null
                    if ( res.success() ) {

                        conf = res.result().content()
                        if ( conf != null ) {
                            if ( Map.class.isAssignableFrom( conf.class ) ) {

                                provider.clear()
                                provider.putAll( conf as Map<String, Object> )
                            }
                            else {

                                this.logger.alarm( "Non expected config type found: ${conf.class}!" )
                            }
                        }
                    }
                    else {
                        this.logger.alarm( "Failed lookup of config for id '${configId}'!", res.failure() )
                    }

                    if ( conf == null && onNoClusterConf != null ) {
                        onNoClusterConf()
                    }
                }
            }
            else {
                this.logger.alarm( "Failed to get a lock to cluster store!", lock.failure() )
            }
        } // Automatic unlock!
    }

}
