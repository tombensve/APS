package se.natusoft.osgi.aps.core.config

import org.osgi.framework.Bundle
import org.osgi.framework.BundleContext
import org.osgi.framework.ServiceRegistration
import se.natusoft.osgi.aps.api.core.APSLockable
import se.natusoft.osgi.aps.api.core.config.APSConfig
import se.natusoft.osgi.aps.api.core.filesystem.service.APSFilesystemService
import se.natusoft.osgi.aps.api.core.platform.service.APSExecutionService
import se.natusoft.osgi.aps.api.core.store.APSLockableDataStoreService
import se.natusoft.osgi.aps.api.messaging.APSMessageService
import se.natusoft.osgi.aps.api.messaging.APSPublisher
import se.natusoft.osgi.aps.api.reactive.APSResult
import se.natusoft.osgi.aps.api.reactive.APSValue
import se.natusoft.osgi.aps.api.util.APSProperties
import se.natusoft.osgi.aps.constants.APS
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.annotation.activator.BundleStop
import se.natusoft.osgi.aps.tools.annotation.activator.Initializer
import se.natusoft.osgi.aps.tools.annotation.activator.Managed
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiService

import static se.natusoft.osgi.aps.api.util.APSProperties.props

/**
 * This class actually manages all configurations.
 */
class ConfigManager {

    //
    // Constants
    //

    private static final String CLUSTER_UPDATE_BUS_ADDRESS = "aps-config-updated"

    //
    // Private Members
    //

    @Managed(loggingFor = "aps-config-provider:config-manager")
    private APSLogger logger

    @Managed
    private BundleContext context

    @OSGiService(additionalSearchCriteria = "(${APS.Messaging.Clustered}=true)", nonBlocking = true)
    private APSMessageService messageService

    /** Filesystem access that won't go away on redeploy. */
    @OSGiService(timeout = "15 sec")
    private APSFilesystemService fsService

    @OSGiService(additionalSearchCriteria = "(${APS.Service.PersistenceScope}=clustered)", nonBlocking = true)
    private APSLockableDataStoreService dataStoreService

    @OSGiService(nonBlocking = true)
    private APSExecutionService execService

    private Map<String, ServiceRegistration> regs = [ : ]
    private Map<String, APSConfigProvider> providers = [ : ]

    private APSProperties clusterConfigExchange = props() + APSMessageService.TARGET >> CLUSTER_UPDATE_BUS_ADDRESS

    private String confMgrId = UUID.randomUUID().toString()

    //
    // Initializer / Shutdown
    //

    @Initializer
    private void init() {
        this.messageService.subscribe( clusterConfigExchange ) { APSValue<Map<String, Object>> value ->

            Map<String, Object> message = value.value()

            if ( message[ 'confMgrId' ] != this.confMgrId ) {
                String configId = message[ 'apsConfigId' ]
                if ( configId != null ) {
                    updateLocalFromCluster( configId )
                }
                else {
                    this.logger.error( "Got null 'apsConfigId' from other cluster member!" )
                }
            }
        }
    }

    @BundleStop
    private void shutDown() {
        this.messageService.unsubscribe( clusterConfigExchange )
        this.regs.each { String key, ServiceRegistration svcReg ->
            svcReg.unregister(  )
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
                    syncNotifier: { APSConfigProvider configProvider ->

                        updateClusterWithLocal( configProvider )

                        this.messageService.publisher( clusterConfigExchange ) { APSPublisher<Map<String, Object>> publisher ->

                            publisher.publish( [
                                    apsConfigId: configProvider.apsConfigId,
                                    confMgrId  : this.confMgrId
                            ] )
                        }
                    }
            )

            this.providers[ configId ] = provider

            // First we need to get the config from cluster store, and if we get something some other node
            // have already loaded the config.

            loadFromClusterOrLocal( configId, owner, schemaPath, defaultConfigPath )

            if ( provider.isEmpty() ) {
                // We have no choice other than use local config.
                this.logger.alarm( "Failed to get nor update cluster config! Using locally loaded config." )
                provider.loadConfig( owner, schemaPath, defaultConfigPath )
            }

            ServiceRegistration svcReg = context.registerService( APSConfig.class.name, provider,
                                                                  [ 'aps-config-id': configId ] as Properties )
            this.regs[ configId ] = svcReg
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
     * It will look in the cluster store for a configuration, and if not available, load it from disk
     * locally and store it in the cluster.
     *
     * That correct configuration is provided is very important and have consequences if it fails.
     * Thereby it will do logging at alarm level which will cause APSAlarmService to be called if
     * available. That said, it will take the last locally saved configuration if it fails to get
     * the cluster config and a local config is available since this is probably better than the
     * default values which will be the last resort.
     *
     * But in the case an alarm is triggered, the best action is to shut the service down again and
     * resolve why it failed.
     *
     * @param provider
     * @param configId
     * @param owner
     * @param schemaPath
     * @param defaultConfigPath
     */
    private void loadFromClusterOrLocal( String configId, Bundle owner, String schemaPath, String defaultConfigPath ) {

        String clusterKey = "aps-config-store.${configId}"

        APSConfigProvider provider = this.providers[ configId ]

        updateLocalFromCluster( configId ) { boolean storeInCluster ->
            // we have to get local config.
            // Not in cluster store, so load it locally ...
            provider.loadConfig( owner, schemaPath, defaultConfigPath )

            // ... and then publish it in cluster store.
            if ( storeInCluster ) {
                this.dataStoreService.store( clusterKey, provider ) { APSResult sres ->
                    if ( !sres.success() ) {
                        this.logger.alarm( "Failed to update cluster store for config '${clusterKey}'",
                                           sres.failure() )
                    }
                }
            }
        }
    }

    /**
     * Updates the local configuration from the one in the cluster.
     *
     * @param configId The id of the config to update.
     */
    private void updateLocalFromCluster( String configId ) {
        updateLocalFromCluster( configId, null )
    }

    /**
     * Updates the local configuration from the one in the cluster.
     *
     * @param configId The id of the config to update.
     * @param onNoClusterConf A closure to call if there were no configuration in the cluster.
     */
    private void updateLocalFromCluster( String configId, Closure onNoClusterConf ) {
        String clusterKey = "aps-config-store.${configId}"

        APSConfigProvider provider = this.providers[ configId ]

        this.dataStoreService.lock( clusterKey ) { APSResult<APSLockable.APSLock> lock ->
            if ( lock.success() ) {

                this.dataStoreService.retrieve( clusterKey ) { APSResult res ->

                    Object conf = null
                    if ( res.success() ) {

                        conf = res.result().value()
                        if ( Map.class.isAssignableFrom( conf.class ) ) {

                            provider.clear()
                            provider.putAll( conf as Map<String, Object> )
                        }
                        else {

                            this.logger.alarm( "Non expected config type found: ${conf.class}!" )
                        }
                    }
                    else {
                        this.logger.alarm( "Failed lookup of config for id '${configId}'!", res.failure() )
                    }

                    if ( conf == null && onNoClusterConf != null ) {
                        onNoClusterConf( res.success() )
                    }
                }
            }
            else {
                this.logger.alarm( "Failed to get a lock to cluster store!", lock.failure() )
            }
        } // Automatic unlock!
    }

    /**
     * Updates the cluster with the local configuration.
     *
     * @param provider The configuration provider to update in the cluster.
     */
    private void updateClusterWithLocal( APSConfigProvider provider ) {
        String clusterKey = "aps-config-store.${provider.apsConfigId}"

        if ( provider != null ) {
            this.dataStoreService.lock( clusterKey ) { APSResult<APSLockable.APSLock> lock ->
                if ( lock.success() ) {
                    this.dataStoreService.store( clusterKey, provider ) { APSResult sres ->
                        if ( !sres.success() ) {
                            this.logger.error( "Failed to update cluster store!", sres.failure() )
                        }
                    }
                }
                else {
                    this.logger.error( "Failed to lock cluster store for config '${clusterKey}" )
                }
            }
        }
    }
}
