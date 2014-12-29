package se.natusoft.osgi.aps.net.messaging.service

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.joda.time.DateTime
import se.natusoft.osgi.aps.api.net.messaging.exception.APSMessagingException
import se.natusoft.osgi.aps.api.net.messaging.types.APSCluster
import se.natusoft.osgi.aps.api.net.messaging.types.APSMessage
import se.natusoft.osgi.aps.codedoc.CodeNote
import se.natusoft.osgi.aps.codedoc.Implements
import se.natusoft.osgi.aps.net.messaging.apis.ConnectionProvider
import se.natusoft.osgi.aps.net.messaging.apis.TimestampProvider
import se.natusoft.osgi.aps.net.messaging.config.RabbitMQClusterServiceConfig
import se.natusoft.osgi.aps.net.messaging.rabbitmq.ClusterReceiveThread
import se.natusoft.osgi.aps.tools.APSLogger

/**
 * Represents one cluster node member.
 */
@CompileStatic
@TypeChecked
public class APSClusterProvider implements APSCluster {

    //
    // Private Members
    //

    /** Basic RabbitMQ config for sending messages. */
    private AMQP.BasicProperties basicProperties

    /** Our sending channel. */
    private Channel channel

    /** This receives messages from the cluster. */
    private ClusterReceiveThread clusterReceiveThread

    /** The cluster time. */
    private DateTime clusterTime

    //
    // Properties
    //

    /** The logger to log to. */
    APSLogger logger

    /**
     * Provides a RabbitMQ Connection. Rather than taking a Connection directly, this can
     * always provide a fresh connection.
     */
    ConnectionProvider connectionProvider;

    /** A configuration for this specific cluster provider instance. */
    RabbitMQClusterServiceConfig.RMQCluster clusterConfig;

    /** Configured properties for this cluster. */
    Properties props

    //
    // Constructors
    //

    public APSClusterProvider() {
        AMQP.BasicProperties.Builder bob = new AMQP.BasicProperties.Builder()
        this.basicProperties = bob.contentType("application/octet-stream").build()
    }

    //
    // Methods
    //

    /**
     * Ensures an open channel and returns it.
     *
     * @throws IOException
     */
    private Channel ensureOpenChannel() throws IOException {

        if (this.channel == null || !this.channel.isOpen()) {
            this.channel = this.connectionProvider.connection.createChannel()
            this.channel.exchangeDeclare(this.clusterConfig.exchange.toString(), this.clusterConfig.exchangeType.toString())
            this.channel.queueDeclare(this.clusterConfig.queue.toString(), true, false, false, null)
            String routingKey = this.clusterConfig.routingKey.toString()
            if (routingKey != null && routingKey.isEmpty()) {
                routingKey = null
            }
            this.channel.queueBind(this.clusterConfig.queue.toString(), this.clusterConfig.exchange.toString(), routingKey)
        }

        return this.channel
    }

    /**
     * Ensures a cluster receive thread and returns it.
     */
    private ClusterReceiveThread ensureClusterReceiveThread() {
        if (this.clusterReceiveThread == null) {
            this.clusterReceiveThread = new ClusterReceiveThread(
                    name: "cluster-receive-thread-" + getName(),
                    connectionProvider: this.connectionProvider,
                    clusterConfig: this.clusterConfig,
                    timestampProvider: new TimestampProvider() {
                        @Override
                        long getDateTime() {
                            return 0 // TODO: Fix.
                        }
                    },
                    logger: this.logger)
            this.clusterReceiveThread.start()
        }

        return this.clusterReceiveThread
    }

    /**
     * Returns the name of this cluster.
     */
    @Override
    @Implements(APSCluster.class)
    public String getName() {
        return this.clusterConfig.name.toString()
    }

    /**
     * Returns the read only properties of this cluster.
     */
    @Override
    @Implements(APSCluster.class)
    public Properties getProperties() {
        return this.props
    }

    /**
     * Returns the Clusters common DateTime that is independent of local machine times.
     * <p/>
     * Always returns now time.
     */
    @Override
    @Implements(APSCluster.class)
    public DateTime getDateTime() {
        return this.clusterTime
    }

    /**
     * Sends a messaging.
     *
     * @param message The message to send.
     * @return true if the messaging was sent.
     * @throws se.natusoft.osgi.aps.api.net.messaging.exception.APSMessagingException on failure.
     */
    @Override
    @Implements(APSCluster.class)
    public boolean sendMessage(APSMessage message) throws APSMessagingException {
        try {
            String routingKey = this.clusterConfig.routingKey.toString()
            if (routingKey.isEmpty()) {
                routingKey = null
            }
            ensureOpenChannel()
                    .basicPublish(this.clusterConfig.exchange.toString(),routingKey, this.basicProperties, message.content)

        }
        catch (Exception e) {
            throw new APSMessagingException(e.getMessage(), e);
        }

        return true
    }

    /**
     * Adds a listener for types.
     *
     * @param listener The listener to add.
     */
    @Override
    @Implements(APSCluster.class)
    public void addMessageListener(APSCluster.Listener listener) {
        ensureClusterReceiveThread().addMessageListener(listener)
    }

    /**
     * Removes a messaging listener.
     *
     * @param group    The name listening to.
     * @param listener The listener to remove.
     */
    @Override
    @Implements(APSCluster.class)
    public void removeMessageListener(String group, APSCluster.Listener listener) {
        ensureClusterReceiveThread().removeMessageListener(listener)
    }

    /**
     * Stops this cluster.
     *
     * @throws IOException
     */
    void stop() throws IOException {
        if (this.clusterReceiveThread != null) {
            this.clusterReceiveThread.stopThread();
            try {
                this.clusterReceiveThread.wait(60000 * 2)
            }
            catch (Exception e) {
                throw new APSMessagingException("Failed waiting for cluster receive thread to stop!", e)
            }
        }
        this.channel.close()
    }
}
