package se.natusoft.osgi.aps.net.messaging.rabbitmq

import com.rabbitmq.client.Channel
import com.rabbitmq.client.ConsumerCancelledException
import com.rabbitmq.client.QueueingConsumer
import com.rabbitmq.client.ShutdownSignalException
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.osgi.aps.api.net.messaging.types.APSCluster
import se.natusoft.osgi.aps.api.net.messaging.types.APSMessage
import se.natusoft.osgi.aps.net.messaging.apis.ConnectionProvider
import se.natusoft.osgi.aps.net.messaging.config.RabbitMQClusterServiceConfig
import se.natusoft.osgi.aps.tools.APSLogger

/**
 * Thread that receives queue types.
 */
@CompileStatic
@TypeChecked
public class ReceiveThread extends Thread {

    //
    // Private Members
    //

    /** Used to stop thread. It will be running as long as this is true. */
    private boolean running = true

    /** The clusterChannel we receive messages on.  */
    private Channel recvChannel = null

    /** The name of the receive queue. */
    private String recvQueueName;

    /** The listeners to notify of received messages. */
    private List<APSCluster.Listener> listeners =
            Collections.synchronizedList(new LinkedList<APSCluster.Listener>())

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

    /** The config for this receiver. */
    RabbitMQClusterServiceConfig.RMQCluster clusterConfig

    /** For resolving received messages. */
    APSCluster.MessageResolver messageResolver

    /** The exchange to listen to. */
    String exchange

    //
    // Methods
    //

    /**
     * Stops this thread.
     */
    public synchronized void stopThread() {
        this.running = false
    }

    /**
     * Returns true for as long as the thread has not been stopped.
     */
    private synchronized boolean keepRunning() {
        return this.running
    }

    /**
     * Adds a message listener to this receiver thread.
     *
     * @param listener The listener to add.
     */
    public void addMessageListener(APSCluster.Listener listener) {
        this.listeners.add(listener)
    }

    /**
     * Removes a message listener from this receiver thread.
     *
     * @param listener The listener to remove.
     */
    public void removeMessageListener(APSCluster.Listener listener) {
        this.listeners.remove(listener)
    }

    /**
     * Returns true if there are listeners available.
     */
    public boolean haveListeners() {
        return !this.listeners.isEmpty()
    }

    /**
     * Removes all listeners.
     */
    public void removeAllListeners() {
        this.listeners.clear()
    }

    /**
     * Returns the receive clusterChannel.
     *
     * @throws Exception
     */
    private Channel getRecvChannel() throws IOException {
        if (this.recvChannel == null || !this.recvChannel.isOpen()) {
            this.recvChannel = this.connectionProvider.connection.createChannel()
            this.recvChannel.exchangeDeclare(exchange, "fanout")
            this.recvQueueName = this.recvChannel.queueDeclare().getQueue()
            String routingKey = this.clusterConfig.routingKey.string
            if (routingKey != null && routingKey.isEmpty()) {
                routingKey = null
            }
            this.recvChannel.queueBind(this.recvQueueName, this.clusterConfig.exchange.string, routingKey)
        }
        return this.recvChannel
    }

    /**
     * Sets up a new QueueingConsumer and returns it.
     */
    private QueueingConsumer setupConsumer() {
        QueueingConsumer consumer = new QueueingConsumer(getRecvChannel())
        getRecvChannel().basicConsume(this.recvQueueName, true, consumer)
        return consumer
    }

    /**
     * Thread entry and exit point.
     */
    public void run() {

        int failureCount = 0

        try {

            QueueingConsumer consumer = setupConsumer()

            while (keepRunning()) {
                if (!this.listeners.isEmpty()) {
                    try {
                        QueueingConsumer.Delivery delivery = consumer.nextDelivery(5000)
                        //noinspection StatementWithEmptyBody
                        if (delivery != null) {
                            byte[] body = delivery.getBody()
                            //logger.debug("======== Received message of length " + body.length + " ==========")
                            //logger.debug("  Current no listeners: " + this.listeners.size())
                            APSMessage message = this.messageResolver.resolveMessage(body)

                            for (APSCluster.Listener listener : this.listeners) {
                                try {
                                    listener.messageReceived(message)
                                }
                                catch (RuntimeException re) {
                                    this.logger.error("Failure during listener call: " + re.getMessage(), re)
                                }
                            }
                        } else {
                            //logger.debug("====== TIMEOUT ======")
                        }

                    }
                    catch (ShutdownSignalException | ConsumerCancelledException sse) {
                        throw sse
                    }
                    // We don't want this thread to die on Exception!
                    catch (Exception e) {
                        this.logger.error("ReceiverThread got an Exception!", e)
                        if (failureCount < 3) {
                            ++failureCount
                            //noinspection UnnecessaryQualifiedReference
                            Thread.sleep(1000);
                            consumer = setupConsumer()
                        } else {
                            this.logger.error("Sleeping for 15 seconds hoping for better times! If this keeps recurring " +
                                    "there is a serious problem!")
                            //noinspection UnnecessaryQualifiedReference
                            Thread.sleep(15000)
                            failureCount = 0
                        }
                    }
                }
                else {
                    // If we don't have any listeners, then we don't fetch any messages.
                    Thread.sleep(10000)
                }
            }

            this.recvChannel.close()
            this.recvChannel = null
        }
        catch (ShutdownSignalException sse) {
            this.logger.error("RabbitMQ are being shutdown!", sse)
        }
        catch (ConsumerCancelledException cce) {
            this.logger.error("RabbitMQ ReceiverThread: The consumer has been cancelled!", cce)
        }
        catch (IOException ioe) {
            this.logger.error("RabbitMQ ReceiverThread: Failed to create consumer! This thread will die and not " +
                    "receive anything!", ioe)
        }
        catch (Exception e) {
            this.logger.error(e.getMessage(), e)
        }
    }

}
