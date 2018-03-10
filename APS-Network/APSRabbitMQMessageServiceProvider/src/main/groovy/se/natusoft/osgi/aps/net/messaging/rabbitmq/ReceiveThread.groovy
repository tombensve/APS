package se.natusoft.osgi.aps.net.messaging.rabbitmq

import com.rabbitmq.client.Channel
import com.rabbitmq.client.ConsumerCancelledException
import com.rabbitmq.client.QueueingConsumer
import com.rabbitmq.client.ShutdownSignalException
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.osgi.aps.api.messaging.APSMessage
import se.natusoft.osgi.aps.model.APSHandler
import se.natusoft.osgi.aps.model.ID
import se.natusoft.osgi.aps.net.messaging.config.Config
import se.natusoft.osgi.aps.tools.APSLogger

/**
 * Thread that receives queue types.
 */
@CompileStatic
@TypeChecked
class ReceiveThread extends Thread {

    //
    // Private Members
    //

    /** Used to stop thread. It will be running as long as this is true. */
    private boolean running = true

    /** The instanceChannel we receive messages on.  */
    private Channel recvChannel = null

    /** The name of the receive queue. */
    private String recvQueueName

    /** The listeners to notify of received messages. */
    private List<APSHandler<APSMessage<byte[]>>> subscribers =
            Collections.synchronizedList( new LinkedList<APSHandler<APSMessage<byte[]>>>() )

    /** To handle removing handlers. */
    private Map<ID, APSHandler<APSMessage<byte[]>>> idToSubscriber = [ : ]

    //
    // Properties
    //

    /** The logger to log to. */
    APSLogger logger

    /**
     * Provides a RabbitMQ Connection. Rather than taking a Connection directly, this can
     * always provide a fresh connection.
     */
    ConnectionProvider connectionProvider

    /** The topic this receiver is working for. */
    String topic

    //
    // Constructor
    //

    ReceiveThread() {
        setName( "rabbit-mq-receiver-thread" )
    }

    //
    // Methods
    //

    /**
     * Stops this thread.
     */
    synchronized void stopThread() {
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
    void addMessageSubscriber( ID id, APSHandler<APSMessage<byte[]>> subscriber ) {
        this.subscribers += subscriber
        this.idToSubscriber[ id ] = subscriber
    }

    /**
     * Removes a message listener from this receiver thread.
     *
     * @param listener The listener to remove.
     */
    void removeMessageSubscriber( ID id ) {
        APSHandler<APSMessage<byte[]>> subscriber = this.idToSubscriber[ id ]
        this.subscribers -= subscriber
        this.idToSubscriber.remove( id )
    }

    /**
     * Returns true if there are listeners available.
     */
    @SuppressWarnings("GroovyUnusedDeclaration")
    boolean haveSubscribers() {
        return !this.subscribers.isEmpty()
    }

    /**
     * Removes all listeners.
     */
    @SuppressWarnings("GroovyUnusedDeclaration")
    void removeAllSubscribers() {
        this.subscribers.clear()
    }

    /**
     * Returns the receive instanceChannel.
     *
     * @throws Exception
     */
    private Channel getRecvChannel() throws IOException {
        if ( this.recvChannel == null || !this.recvChannel.isOpen() ) {
            this.recvChannel = this.connectionProvider.connection.createChannel()

            Map<String, String> defaultInst = ( Config.config.instances as Map<String, Serializable> ).default as Map<String, String>
            this.recvChannel.exchangeDeclare( defaultInst.exchange, defaultInst.exchangeType )
            this.recvQueueName = this.recvChannel.queueDeclare().getQueue()

            String routingKey = defaultInst.routingKey
            if ( routingKey != null && routingKey.isEmpty() ) {
                routingKey = null
            }
            this.recvChannel.queueBind( this.recvQueueName, defaultInst.exchange, routingKey )
        }
        return this.recvChannel
    }

    /**
     * Sets up a new QueueingConsumer and returns it.
     */
    private QueueingConsumer setupConsumer() {
        QueueingConsumer consumer = new QueueingConsumer( getRecvChannel() )
        getRecvChannel().basicConsume( this.recvQueueName, true, consumer )
        return consumer
    }

    /**
     * Thread entry and exit point.
     */
    void run() {

        int failureCount = 0

        try {

            QueueingConsumer consumer = setupConsumer()

            while ( keepRunning() ) {
                if ( !this.subscribers.isEmpty() ) {
                    try {
                        QueueingConsumer.Delivery delivery = consumer.nextDelivery( 5000 )
                        //noinspection StatementWithEmptyBody
                        if ( delivery != null ) {
                            //logger.debug("======== Received message of length " + body.length + " ==========")
                            //logger.debug("  Current no listeners: " + this.listeners.size())

                            for ( APSHandler<APSMessage<byte[]>> subscriber : this.subscribers ) {
                                try {
                                    subscriber.handle( new APSMessage.Provider<byte[]>( delivery.body ) )
                                }
                                catch ( RuntimeException re ) {
                                    this.logger.error( "Failure during listener call: " + re.getMessage(), re )
                                }
                            }
                        }
                        else {
                            //logger.debug("====== TIMEOUT ======")
                        }

                    }
                    catch ( ShutdownSignalException | ConsumerCancelledException sse ) {
                        throw sse
                    }
                    // We don't want this thread to die on Exception!
                    catch ( Exception e ) {
                        this.logger.error( "ReceiverThread got an Exception!", e )
                        if ( failureCount < 3 ) {
                            ++failureCount
                            //noinspection UnnecessaryQualifiedReference
                            Thread.sleep( 2000 )
                            consumer = setupConsumer()
                        }
                        else {
                            this.logger.error( "Sleeping for 15 seconds hoping for better times! If this keeps recurring " +
                                                       "there is a serious problem!" )
                            //noinspection UnnecessaryQualifiedReference
                            Thread.sleep( 15000 )
                            failureCount = 0
                        }
                    }
                }
                else {
                    // If we don't have any listeners, then we don't fetch any messages.
                    sleep( 5000 )
                }
            }

            this.recvChannel.close()
            this.recvChannel = null
        }
        catch ( ShutdownSignalException sse ) {
            this.logger.error( "RabbitMQ are being shutdown!", sse )
        }
        catch ( ConsumerCancelledException cce ) {
            this.logger.error( "RabbitMQ ReceiverThread: The consumer has been cancelled!", cce )
        }
        catch ( IOException ioe ) {
            this.logger.error( "RabbitMQ ReceiverThread: Failed to create consumer! This thread will die and not " +
                                       "receive anything!", ioe )
        }
        catch ( Exception e ) {
            this.logger.error( e.getMessage(), e )
        }
    }

}
