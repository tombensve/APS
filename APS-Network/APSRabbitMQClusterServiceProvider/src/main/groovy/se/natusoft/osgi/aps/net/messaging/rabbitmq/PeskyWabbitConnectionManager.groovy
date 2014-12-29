package se.natusoft.osgi.aps.net.messaging.rabbitmq

import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.osgi.aps.net.messaging.config.RabbitMQClusterServiceConfig

/**
 * Manages the RabbitMQ connection.
 */
@CompileStatic
@TypeChecked
public class PeskyWabbitConnectionManager {
    //
    // Private Members
    //

    /** A connection factory for connecting to RabbitMQ. */
    private ConnectionFactory connectionFactory = null

    /** The RabbitMQ service connection. */
    private Connection connection = null

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
            this.connectionFactory = new ConnectionFactory()
            connectionFactory.setHost(RabbitMQClusterServiceConfig.managed.get().host.toString())
            connectionFactory.setPort(RabbitMQClusterServiceConfig.managed.get().port.toInt())

            if (RabbitMQClusterServiceConfig.managed.get().user.toString().length() > 0) {
                connectionFactory.setUsername(RabbitMQClusterServiceConfig.managed.get().user.toString())
            }

            if (RabbitMQClusterServiceConfig.managed.get().password.toString().length() > 0) {
                connectionFactory.setPassword(RabbitMQClusterServiceConfig.managed.get().password.toString())
            }

            if (RabbitMQClusterServiceConfig.managed.get().virtualHost.toString().length() > 0) {
                connectionFactory.setVirtualHost(RabbitMQClusterServiceConfig.managed.get().virtualHost.toString())
            }

            if (RabbitMQClusterServiceConfig.managed.get().timeout.toInt() > 0) {
                connectionFactory.setConnectionTimeout(RabbitMQClusterServiceConfig.managed.get().timeout.toInt())
            }
        }

        if (this.connection == null || !this.connection.isOpen()) {
            try {
                this.connection = connectionFactory.newConnection()
            }
            catch (IOException ioe) {
                if (this.connection != null) {
                    try {
                        this.connection.close()
                    }
                    catch (IOException ioe2) { /* It has already failed and that failure is handled below. */ }
                }
                throw new IOException("Failed to ensure connection to RabbitMQ due to channel create failure!", ioe)
            }
        }

        return "Connected to RabbitMQ server at " + RabbitMQClusterServiceConfig.managed.get().host + ":" +
                RabbitMQClusterServiceConfig.managed.get().port + "!"
    }

    /**
     * Closes the connection if not closed already.
     */
    public  synchronized String ensureConnectionClosed() throws IOException {

        if (this.connection != null) {
            try {
                this.connection.close()
                this.connection = null
            }
            catch (IOException ioe) {
                throw new IOException("Failed to disconnect from RabbitMQ!", ioe)
            }
        }

        this.connectionFactory = null

        return "Disconnected from RabbitMQ server at " + RabbitMQClusterServiceConfig.managed.get().host +
                ":" + RabbitMQClusterServiceConfig.managed.get().port + "!"
    }

    /**
     * Closes connection and then creates a new again.
     */
    public synchronized void reconnect() throws IOException {
        ensureConnectionClosed()
        ensureConnection()
    }

    /**
     * @return The current connection.
     */
    public Connection getConnection() throws IOException {
        ensureConnection()

        return this.connection
    }

}
