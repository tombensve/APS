/*
 *
 * PROJECT
 *     Name
 *         APS RabbitMQ Cluster Service Provider
 *
 *     Code Version
 *         1.0.0
 *
 *     Description
 *         Provides an implementation of APSMessageService using RabbitMQ Java Client.
 *
 * COPYRIGHTS
 *     Copyright (C) 2012 by Natusoft AB All rights reserved.
 *
 * LICENSE
 *     Apache 2.0 (Open Source)
 *
 *     Licensed under the Apache License, Version 2.0 (the "License")
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
 *         2013-09-01: Created!
 *
 */
package se.natusoft.osgi.aps.net.messaging.service

import com.rabbitmq.client.Connection
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.osgi.aps.api.core.config.event.APSConfigChangedEvent
import se.natusoft.osgi.aps.api.core.config.event.APSConfigChangedListener
import se.natusoft.osgi.aps.api.net.messaging.service.APSClusterService
import se.natusoft.osgi.aps.api.net.messaging.types.APSCluster
import se.natusoft.osgi.aps.codedoc.Implements
import se.natusoft.osgi.aps.codedoc.Issue
import se.natusoft.osgi.aps.net.messaging.apis.ConnectionProvider
import se.natusoft.osgi.aps.net.messaging.config.RabbitMQClusterServiceConfig
import se.natusoft.osgi.aps.net.messaging.rabbitmq.PeskyWabbitConnectionManager
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.annotation.activator.*

/**
 * This provides an implementation of APSSimpleMessageService using RabbitMQ.
 *
 * Please note that message clusters are represented by RabbitMQ exchanges. When a name is joined
 * an exchange with that name and type "fanout" is created. The receiver uses an anonymous queue.
 * Each "name" has its own receiver and sender channel. Each name will also have its own receiver
 * thread. My first attempt was to reuse channels as much as possible, but that did not work
 * very well, but I'm rather new to RabbitMQ and have to admit I haven't yet fully understood
 * all its features. It was however rather easy to install and get upp and running.
 */
@SuppressWarnings("UnnecessaryQualifiedReference")
@OSGiServiceProvider(properties = [
        @OSGiProperty(name = APSClusterService.CLUSTER_PROVIDER_PROPERTY, value = "RabbitMQ")
], threadStart = true)
@Issue(
        target = "IntelliJ IDEA", targetVersion = "14.0.2",
        id = "IDEA-134832",
        description = [
                "Shows warning about ",
                "'unnecessary qualified reference' for APSClusterService.CLUSTER_PROVIDER_PROPERTY, ",
                "which is incorrect! The qualified reference is required for the code to compile."
        ],
        url = "https://youtrack.jetbrains.com/issue/IDEA-134832"
)
@CompileStatic
@TypeChecked
public class APSRabbitMQClusterServiceProvider implements APSClusterService {
    //
    // Private Members
    //

    /** Our logger. */
    @Managed(loggingFor = "aps-rabbitmq-cluster-service-provider")
    private APSLogger logger

    /** Listens to configuration changes. */
    private APSConfigChangedListener configChangedListener

    /** For connecting to RabbitMQ. */
    private PeskyWabbitConnectionManager rabbitMQConnectionManager

    /** The defined clusters. */
    private Map<String, APSClusterProvider> clusters = new HashMap<>()


    //
    // Constructors
    //

    /**
     * Creates a new APSRabbitMQClusterServiceProvider.
     */
    public APSRabbitMQClusterServiceProvider() {}

    //
    // Methods
    //

    /**
     * This method is run on bundle start.
     *
     * It will register a configuration listener and then start all configured clusters. The configuration listener
     * will reconnect to the RabbitMQ message bus in case connection config has changed, and then take down deleted
     * clusters and start newly defined clusters.
     */
    @BundleStart
    public void startup() {
        // Please note that we can access config here due to the threadStart=true in the @OSGiServiceProvider
        // above. The instance of this class will be created in a separate thread from the OSGi server startup
        // thread that calls the activator.

        try {
            this.rabbitMQConnectionManager = new PeskyWabbitConnectionManager()

            this.logger.info(this.rabbitMQConnectionManager.ensureConnection())

            this.configChangedListener = new APSConfigChangedListener() {
                @Override
                public void apsConfigChanged(APSConfigChangedEvent event) {
                    synchronized (APSRabbitMQClusterServiceProvider.this) {
                        try {
                            APSRabbitMQClusterServiceProvider.this.rabbitMQConnectionManager.reconnect()
                            refreshClusters()
                        }
                        catch (IOException ioe) {
                            APSRabbitMQClusterServiceProvider.this.logger.error("Failed reconnecting to RabbitMQ!", ioe)
                        }
                    }
                }
            }

            RabbitMQClusterServiceConfig.managed.get().addConfigChangedListener(this.configChangedListener)

            startAllClusters()
        }
        catch (IOException ioe) {
            this.logger.error("Failed to connect to RabbitMQ!", ioe)
        }
    }

    /**
     * This method is run on bundle stop.
     *
     * It will take down all clusters.
     */
    @BundleStop
    public void shutdown() {
        if (this.configChangedListener != null) {
            RabbitMQClusterServiceConfig.managed.get().removeConfigChangedListener(this.configChangedListener)
        }

        stopAllClusters()

        try {
            this.logger.info(this.rabbitMQConnectionManager.ensureConnectionClosed())
        }
        catch (IOException ioe) {
            this.logger.error("Failed to stop RabbitMQ connection!", ioe)
        }
    }

    private void startAllClusters() {
        try {
            RabbitMQClusterServiceConfig.managed.get().clusters.each { RabbitMQClusterServiceConfig.RMQCluster cluster ->
                startCluster(cluster)
            }
        }
        catch (Throwable t) {
            this.logger.debug("Cluster setup failure: " + t.message, t)
        }
    }

    private void stopAllClusters() {
        this.clusters.keySet().each { String name ->
            APSClusterProvider clusterNodeProvider = this.clusters.get(name)
            stopCluster(clusterNodeProvider)
        }
    }

    private void startCluster(RabbitMQClusterServiceConfig.RMQCluster cluster) {
        this.clusters.put(cluster.name.toString(),
                new APSClusterProvider(
                        connectionProvider: new ConnectionProvider() {
                            @Override
                            Connection getConnection() throws IOException {
                                return APSRabbitMQClusterServiceProvider.this.rabbitMQConnectionManager.connection
                            }
                        },
                        clusterConfig: cluster,
                        logger: this.logger
                )
        )
    }

    private void stopCluster(APSClusterProvider clusterNodeProvider) {
        try {
            clusterNodeProvider.stop()
        }
        catch (IOException ioe) {
            this.logger.error("Failed to stop APSCluster instance! [" + ioe.getMessage() + "]", ioe)
        }
    }

    /**
     * This gets called on configuration change.
     */
    private void refreshClusters() {
        closeRemovedClusters()
        startNewClusters()
    }

    private void closeRemovedClusters() {
        this.clusters.findAll { String name, APSClusterProvider cluster ->
            !RabbitMQClusterServiceConfig.managed.get().clusters.any {
                RabbitMQClusterServiceConfig.RMQCluster clusterConfig ->
                    return clusterConfig.name.toString().equals(name)
            }
        }.each { String name, APSClusterProvider cluster ->
            stopCluster(cluster)
        }
    }

    @Issue(
            target = "IntelliJ IDEA", targetVersion = "14.0.2",
            id ="IDEA-134831",
            description = "'Object cluster ->' is incorrectly marked as an error!",
            url = "https://youtrack.jetbrains.com/issue/IDEA-134831"
    )
    private void startNewClusters() {
        RabbitMQClusterServiceConfig.managed.get().clusters.findAll { RabbitMQClusterServiceConfig.RMQCluster cluster ->
            !this.clusters.containsKey(cluster.name.toString())
        }.each { Object cluster ->
            startCluster((RabbitMQClusterServiceConfig.RMQCluster)cluster)
        }
    }

    /**
     * Returns the named cluster or null if no cluster with specified name exists.
     *
     * @param name The name of the cluster to get.
     */
    @Override
    @Implements(APSClusterService.class)
    APSCluster getClusterByName(String name) {
        return this.clusters.get(name)
    }
}

