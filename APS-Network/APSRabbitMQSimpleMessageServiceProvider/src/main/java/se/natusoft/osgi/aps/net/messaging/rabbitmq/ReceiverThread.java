package se.natusoft.osgi.aps.net.messaging.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;
import se.natusoft.osgi.aps.api.net.messaging.service.APSMessageService;
import se.natusoft.osgi.aps.net.messaging.config.RabbitMQMessageServiceConfig;
import se.natusoft.osgi.aps.net.messaging.service.APSRabbitMQMessageServiceProvider;
import se.natusoft.osgi.aps.tools.APSLogger;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Thread that receives queue types.
 */
public class ReceiverThread extends Thread {
    //
    // Private Members
    //

    private APSLogger logger;

    private boolean running = true;

    private Channel recvChannel = null;

    private RabbitMQMessageServiceConfig.GEQ groupConfig;

    List<APSMessageService.Listener> listeners =
            Collections.synchronizedList(new LinkedList<APSMessageService.Listener>());

    //
    // Constructors
    //

    /**
     * Creates a new ReceiverThread.
     *
     * @param logger The logger to log to.
     */
    public ReceiverThread(APSLogger logger) {
        this.logger = logger;
    }

    //
    // Methods
    //

    /**
     * Stops this thread.
     */
    public synchronized void stopThread() {
        this.running = false;
    }

    /**
     * Returns true for as long as the thread has not been stopped.
     */
    private synchronized boolean keepRunning() {
        return this.running;
    }

    /**
     * Adds a message listener to this receiver thread.
     *
     * @param listener The listener to add.
     */
    public void addMessageListener(APSMessageService.Listener listener) {
        this.listeners.add(listener);
    }

    /**
     * Removes a message listener from this receiver thread.
     *
     * @param listener The listener to remove.
     */
    public void removeMessageListener(APSMessageService.Listener listener) {
        this.listeners.remove(listener);
    }

    /**
     * Returns true if there are listeners available.
     */
    public boolean haveListeners() {
        return !this.listeners.isEmpty();
    }

    /**
     * Removes all listeners.
     */
    public void removeAllListeners() {
        this.listeners.clear();
    }

    /**
     * Returns the receive channel.
     *
     * @throws Exception
     */
    private Channel getRecvChannel() throws IOException {
        if (this.recvChannel == null || !this.recvChannel.isOpen()) {
            this.recvChannel = getConnection().createChannel();
            this.recvChannel.exchangeDeclare(name, "fanout");
            this.recvQueueName = this.recvChannel.queueDeclare().getQueue();
            this.recvChannel.queueBind(this.recvQueueName, name, "");
        }
        return this.recvChannel;
    }

    /**
     * Thread entry and exit point.
     */
    public void run() {

        int failureCount = 0;

        try {

            QueueingConsumer consumer = new QueueingConsumer(getRecvChannel());
            getRecvChannel().basicConsume(this.recvQueueName, true, consumer);

            while (keepRunning()) {
                try {
                    QueueingConsumer.Delivery delivery = consumer.nextDelivery(5000);
                    //noinspection StatementWithEmptyBody
                    if (delivery != null) {
                        byte[] body = delivery.getBody();
                        //logger.debug("======== Received message of length " + body.length + " ==========");
                        //logger.debug("  Current no listeners: " + this.listeners.size());
                        Content message = createMessage(body);

                        new ListenerCallThread(name, message, this.listeners).start();
                    }
                    else {
                        //logger.debug("====== TIMEOUT ======");
                    }

                }
                catch (ShutdownSignalException | ConsumerCancelledException sse) {
                    throw sse;
                }
                // We dont want this thread to die on Exception!
                catch (Exception e) {
                    logger.error("ReceiverThread got an Exception!", e);
                    if (failureCount < 3) {
                        ++failureCount;

                        consumer = new QueueingConsumer(getRecvChannel());
                        getRecvChannel().basicConsume(this.recvQueueName, true, consumer);
                    }
                    else {
                        logger.error("Sleeping for 5 seconds hoping for better times! If this keeps recurring there is " +
                                "a serious problem!");
                        Thread.sleep(5000);
                        failureCount = 0;
                    }
                }
            }

            this.recvChannel.close();
            this.recvChannel = null;
        }
        catch (ShutdownSignalException sse) {
            logger.error("We are being shutdown!", sse);
        }
        catch (ConsumerCancelledException cce) {
            logger.error("The consumer has been cancelled!", cce);
        }
        catch (IOException ioe) {
            logger.error("ReceiverThread: Failed to create consumer! This thread will die and not " +
                    "receive anything!", ioe);
        }
        catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Lets protect ourself from called user code!
     */
    private class ListenerCallThread extends Thread {
        String name;
        Content message;
        LinkedList<APSMessageService.Listener> listenersCopy = new LinkedList<>();

        public ListenerCallThread(String name, Content message, List<APSMessageService.Listener> listeners) {
            super("APSRabbitMQSimpleMessageServiceProvider-ListenerCallThread");
            this.name = name;
            this.message = message;
            this.listenersCopy.addAll(listeners);
        }

        @Override
        public void run() {
            // TODO: This is still not optimal since if a 'listener.receiveMessage(...)' call
            // decides to not return consecutive listeners will not be called!
            for (APSMessageService.Listener listener: this.listenersCopy) {
                try {
                    listener.receiveMessage(this.name, message);
                }
                catch (Exception e) {
                    logger.error("Calling listener '" + listener + "' failed!", e);
                }
            }
        }
    }
}
