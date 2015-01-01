package se.natusoft.osgi.aps.net.messaging.service

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.osgi.aps.api.net.messaging.exception.APSMessagingException
import se.natusoft.osgi.aps.api.net.messaging.messages.APSRootMessage
import se.natusoft.osgi.aps.api.net.messaging.types.APSCluster
import se.natusoft.osgi.aps.api.net.messaging.types.APSClusterDateTime
import se.natusoft.osgi.aps.api.net.messaging.types.APSMessage
import se.natusoft.osgi.aps.codedoc.Implements
import se.natusoft.osgi.aps.net.messaging.apis.ConnectionProvider
import se.natusoft.osgi.aps.net.messaging.config.RabbitMQClusterServiceConfig
import se.natusoft.osgi.aps.net.messaging.messages.ClusterTimeMasterChallengeMessage
import se.natusoft.osgi.aps.net.messaging.messages.ClusterTimeMasterMessage
import se.natusoft.osgi.aps.net.messaging.messages.ClusterTimeRequestMessage
import se.natusoft.osgi.aps.net.messaging.messages.ClusterTimeValueMessage
import se.natusoft.osgi.aps.net.messaging.rabbitmq.ReceiveThread
import se.natusoft.osgi.aps.tools.APSLogger

/**
 * Represents one cluster node member.
 */
@CompileStatic
@TypeChecked
public class APSClusterProvider implements
        APSCluster, APSCluster.Listener, APSCluster.MessageResolver, APSClusterDateTimeProvider.ControlChannelSender {

    //
    // Private Members
    //

    /** Basic RabbitMQ config for sending messages. */
    private AMQP.BasicProperties basicProperties

    /** Our cluster channel. */
    private Channel clusterChannel

    /** Control channel for internal messages. */
    private Channel controlChannel

    /** This receives messages from the cluster. */
    private ReceiveThread clusterReceiveThread

    /** This receives messages from the control exchange. */
    private ReceiveThread controlReceiveThread

    /** Handles cluster date and time. */
    private APSClusterDateTimeProvider clusterDateTimeProvider

    /** The name of the control exchange. */
    private String controlExchange

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

    /** For resolving received messages. */
    APSCluster.MessageResolver messageResolver

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

    // ----- Side effects of using Groovy properties constructor ...

    private validate(Object what, String message) {
        if (what == null) throw new IllegalArgumentException(message)
    }

    private void validate() {
        validate(this.connectionProvider, "Missing required 'connectionProvider'!")
        validate(this.clusterConfig, "Missing required 'clusterConfig'!")
    }

    // -----

    /**
     * Ensures an open clusterChannel and returns it.
     *
     * @throws IOException
     */
    private Channel ensureClusterChannel() throws IOException {
        validate()

        if (this.clusterChannel == null || !this.clusterChannel.isOpen()) {
            this.clusterChannel = this.connectionProvider.connection.createChannel()
            this.clusterChannel.exchangeDeclare(this.clusterConfig.exchange.string, this.clusterConfig.exchangeType.string)
            this.clusterChannel.queueDeclare(this.clusterConfig.queue.string, true, false, false, null)
            String routingKey = this.clusterConfig.routingKey.string
            if (routingKey != null && routingKey.isEmpty()) {
                routingKey = null
            }
            this.clusterChannel.queueBind(this.clusterConfig.queue.string, this.clusterConfig.exchange.string, routingKey)
        }

        return this.clusterChannel
    }

    /**
     * Ensures an open controlChannel and returns it.
     *
     * @throws IOException
     */
    private Channel ensureControlChannel() throws IOException {
        validate()

        if (this.controlChannel == null || !this.controlChannel.isOpen()) {
            this.controlChannel = this.connectionProvider.connection.createChannel()
            this.controlExchange = this.clusterConfig.exchange.string + "-control";
            this.controlChannel.exchangeDeclare(this.controlExchange, this.clusterConfig.exchangeType.string)
            String queue = this.clusterConfig.queue.string + "-control"
            this.controlChannel.queueDeclare(queue, true, false, false, null)
            String routingKey = this.clusterConfig.routingKey.string
            if (routingKey != null && routingKey.isEmpty()) {
                routingKey = null
            }
            this.controlChannel.queueBind(queue, this.controlExchange, routingKey)
        }

        return this.controlChannel
    }

    /**
     * Ensures a cluster receive thread and returns it.
     */
    private ReceiveThread ensureClusterReceiveThread() {
        validate()
        validate(this.messageResolver, "Missing required 'messageResolver'!")

        if (this.clusterReceiveThread == null) {
            this.clusterReceiveThread = new ReceiveThread(
                    exchange: this.clusterConfig.exchange.string,
                    name: "cluster-receive-thread-" + getName(),
                    connectionProvider: this.connectionProvider,
                    clusterConfig: this.clusterConfig,
                    logger: this.logger,
                    messageResolver: this.messageResolver
            )
            this.clusterReceiveThread.start()
        }

        return this.clusterReceiveThread
    }

    /**
     * Ensures a control receive thread and returns it.
     */
    private ReceiveThread ensureControlReceiveThread() {
        validate()

        if (this.controlReceiveThread == null) {
            this.controlReceiveThread = new ReceiveThread(
                    exchange: this.controlExchange,
                    name: "control-receive-thread-" + getName(),
                    connectionProvider: this.connectionProvider,
                    clusterConfig: this.clusterConfig,
                    logger: this.logger,
                    messageResolver: this
            )
            this.controlReceiveThread.start()
        }

        return this.controlReceiveThread
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
     *
     * Always returns now time.
     */
    APSClusterDateTime getClusterDateTime() {
        return this.clusterDateTime
    }

    /**
     * Sends a message on the RabbitMQ bus.
     *
     * @param channel The channel to send on.
     * @param exchange The exchange to send to.
     * @param message The message to send.
     */
    private void sendBusMessage(Channel channel, String exchange, APSMessage message) {
        String routingKey = this.clusterConfig.routingKey.string
        if (routingKey.isEmpty()) {
            routingKey = null
        }
        channel.basicPublish(exchange,routingKey, this.basicProperties, message.bytes)
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
            sendBusMessage(ensureClusterChannel(), this.clusterConfig.exchange.toString(), message)
        }
        catch (Exception e) {
            throw new APSMessagingException(e.getMessage(), e);
        }

        return true
    }

    /**
     * Sends a control message on the control channel and exchange.
     *
     * @param message The message to send.
     */
    @Override
    @Implements(APSClusterDateTimeProvider.ControlChannelSender.class)
    public void sendControlMessage(APSMessage message) {
        sendBusMessage(ensureControlChannel(), this.controlExchange, message)
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
    public void removeMessageListener(APSCluster.Listener listener) {
        ensureClusterReceiveThread().removeMessageListener(listener)
    }

    /**
     * Start the cluster provider.
     */
    public APSClusterProvider start() {
        ensureControlReceiveThread().addMessageListener(this)
        this.clusterDateTimeProvider = new APSClusterDateTimeProvider(
                controlChannelSender: this
        )
        this.clusterDateTimeProvider.sendTimeRequest()

        return this
    }

    /**
     * Stops this cluster.
     *
     * @throws IOException
     */
    public void stop() throws IOException {
        if (this.clusterReceiveThread != null) {
            this.clusterReceiveThread.stopThread();
            try {
                this.clusterReceiveThread.wait(1000 * 60)
            }
            catch (Exception e) {
                throw new APSMessagingException("Failed waiting for cluster receive thread to stop!", e)
            }
        }
        if (this.clusterChannel != null && this.clusterChannel.isOpen()) {
            this.clusterChannel.close()
        }

        if (this.controlReceiveThread != null) {
            this.controlReceiveThread.stopThread()
            try {
                this.clusterReceiveThread.wait(1000 * 60)
            }
            catch (Exception e) {
                throw new APSMessagingException("Failed waiting for control receive thread to stop!", e)
            }
        }
        if (this.controlChannel != null && this.controlChannel.isOpen()) {
            this.controlChannel.close()
        }
    }

    /**
     * This is called when a message is received.
     *
     * @param message The received message.
     */
    @Override
    public void messageReceived(APSMessage message) {
        switch (message.class) {
            case ClusterTimeRequestMessage.class:
            case ClusterTimeValueMessage.class:
            case ClusterTimeMasterMessage.class:
            case ClusterTimeMasterChallengeMessage.class:
                this.clusterDateTimeProvider.messageReceived(message)
                break
        }
    }

    /**
     * Returns an APSMessage implementation based on the message data.
     *
     * @param messageData The message data.
     */
    @Override
    APSMessage resolveMessage(byte[] messageData) {
        APSMessage message = null
        String msgType = APSRootMessage.getType(messageData)
        switch (msgType) {
            case ClusterTimeValueMessage.MESSAGE_TYPE:
                message = new ClusterTimeValueMessage()
                break
            case ClusterTimeRequestMessage.MESSAGE_TYPE:
                message = new ClusterTimeRequestMessage()
                break
            case ClusterTimeMasterMessage.MESSAGE_TYPE:
                message = new ClusterTimeMasterMessage()
                break
            case ClusterTimeMasterChallengeMessage.MESSAGE_TYPE:
                message = new ClusterTimeMasterChallengeMessage()
                break
            default:
                this.logger.error("Received unknown message type! [" + msgType + "]")
        }

        if (message != null) {
            message.bytes = messageData
        }

        return message
    }
}
