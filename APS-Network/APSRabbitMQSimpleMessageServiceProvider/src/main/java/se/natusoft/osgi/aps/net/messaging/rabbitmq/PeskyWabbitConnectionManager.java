package se.natusoft.osgi.aps.net.messaging.rabbitmq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import se.natusoft.osgi.aps.net.messaging.config.RabbitMQMessageServiceConfig;

import java.io.IOException;

/**
 * Basically a WabbitMQ wrapper.
 */
public class PeskyWabbitConnectionManager {
    //
    // Private Members
    //

    /** A connection factory for connecting to RabbitMQ. */
    private ConnectionFactory connectionFactory = null;

    /** The RabbitMQ service connection. */
    private Connection connection = null;

    private AMQP.BasicProperties basicProperties;


    //
    // Constructor
    //

    /**
     * Creates a new PeskyWabbitManager.
     */
    public PeskyWabbitConnectionManager() {
        AMQP.BasicProperties.Builder bob = new AMQP.BasicProperties.Builder();
        this.basicProperties = bob.contentType("application/octet-stream").build();
    }

    //
    // Methods
    //

    /**
     * Ensures that there is a connection setup.
     *
     * @throws IOException on failure to setup a connection.
     */
    public synchronized String ensureConnection() throws IOException {
        if (this.connectionFactory == null) {
            this.connectionFactory = new ConnectionFactory();
            connectionFactory.setHost(RabbitMQMessageServiceConfig.managed.get().host.toString());
            connectionFactory.setPort(RabbitMQMessageServiceConfig.managed.get().port.toInt());

            if (RabbitMQMessageServiceConfig.managed.get().user.toString().length() > 0) {
                connectionFactory.setUsername(RabbitMQMessageServiceConfig.managed.get().user.toString());
            }

            if (RabbitMQMessageServiceConfig.managed.get().password.toString().length() > 0) {
                connectionFactory.setPassword(RabbitMQMessageServiceConfig.managed.get().password.toString());
            }

            if (RabbitMQMessageServiceConfig.managed.get().virtualHost.toString().length() > 0) {
                connectionFactory.setVirtualHost(RabbitMQMessageServiceConfig.managed.get().virtualHost.toString());
            }

            if (RabbitMQMessageServiceConfig.managed.get().timeout.toInt() > 0) {
                connectionFactory.setConnectionTimeout(RabbitMQMessageServiceConfig.managed.get().timeout.toInt());
            }
        }

        if (this.connection == null || !this.connection.isOpen()) {
            try {
                this.connection = connectionFactory.newConnection();
            }
            catch (IOException ioe) {
                if (this.connection != null) {
                    try {
                        this.connection.close();
                    }
                    catch (IOException ioe2) { /* It has already failed and that failure is handled below. */ }
                }
                throw new IOException("Failed to ensure connection to RabbitMQ due to channel create failure!", ioe);
            }
        }

        return "Connected to RabbitMQ server at " + RabbitMQMessageServiceConfig.managed.get().host + ":" +
                RabbitMQMessageServiceConfig.managed.get().port + "!";
    }

    /**
     * Closes the connection if not closed already.
     */
    public  synchronized String ensureConnectionClosed() throws IOException {

        if (this.connection != null) {
            try {
                this.connection.close();
                this.connection = null;
            }
            catch (IOException ioe) {
                throw new IOException("Failed to disconnect from RabbitMQ!", ioe);
            }
        }

        this.connectionFactory = null;

        return "Disconnected from RabbitMQ server at " + RabbitMQMessageServiceConfig.managed.get().host +
                ":" + RabbitMQMessageServiceConfig.managed.get().port + "!";
    }

    /**
     * Closes connection and then creates a new again.
     */
    public synchronized void reconnect() throws IOException {
        ensureConnectionClosed();
        ensureConnection();
    }

    /**
     * @return The current connection.
     */
    public Connection getConnection() throws IOException {
        ensureConnection();

        return this.connection;
    }

    public RabbitMQMessageServiceConfig.GEQ getGroupConfig(String group) throws IOException {
        for (RabbitMQMessageServiceConfig.GEQ cgroup : RabbitMQMessageServiceConfig.managed.get().groups) {
            if (cgroup.toString().equals(group)) {
                return cgroup;
            }
        }

        throw new IOException("No such group: '" + group + "'!");
    }

    public Channel ensureOpenChannel(Channel channel, String exchange, String type, String queueName, String routingKey) throws IOException {
        if (channel == null || !channel.isOpen()) {
            channel = getConnection().createChannel();
            channel.exchangeDeclare(exchange, type);
            channel.queueDeclare(queueName, true, false, false, null);
            if (routingKey != null && routingKey.isEmpty()) {
                routingKey = null;
            }
            channel.queueBind(queueName, exchange, routingKey);
        }

        return channel;
    }

    public Channel ensureOpenChannel(Channel channel, RabbitMQMessageServiceConfig.GEQ groupConfig) throws IOException {
        return ensureOpenChannel(
                channel,
                groupConfig.exchange.toString(),
                groupConfig.exchangeType.toString(),
                groupConfig.queue.toString(),
                groupConfig.routingKey.toString()
        );
    }



    public void publishMessage(String group, byte[] data) throws IOException {
        RabbitMQMessageServiceConfig.GEQ groupConfig = getGroupConfig(group);

        String routingKey = groupConfig.routingKey.toString();
        if (routingKey.isEmpty()) {
            routingKey = null;
        }
        ensureOpenChannel(groupConfig).basicPublish(groupConfig.exchange.toString(),routingKey, this.basicProperties, data);
    }


}
