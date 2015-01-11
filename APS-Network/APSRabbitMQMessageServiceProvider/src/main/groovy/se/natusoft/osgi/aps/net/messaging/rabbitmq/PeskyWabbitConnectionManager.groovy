package se.natusoft.osgi.aps.net.messaging.rabbitmq

import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.osgi.aps.net.messaging.config.RabbitMQMessageServiceConfig

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
            connectionFactory.setHost(RabbitMQMessageServiceConfig.managed.get().host.string)
            connectionFactory.setPort(RabbitMQMessageServiceConfig.managed.get().port.int)

            if (RabbitMQMessageServiceConfig.managed.get().user.string.length() > 0) {
                connectionFactory.setUsername(RabbitMQMessageServiceConfig.managed.get().user.string)
            }

            if (RabbitMQMessageServiceConfig.managed.get().password.string.length() > 0) {
                connectionFactory.setPassword(RabbitMQMessageServiceConfig.managed.get().password.string)
            }

            if (RabbitMQMessageServiceConfig.managed.get().virtualHost.string.length() > 0) {
                connectionFactory.setVirtualHost(RabbitMQMessageServiceConfig.managed.get().virtualHost.string)
            }

            if (RabbitMQMessageServiceConfig.managed.get().timeout.int > 0) {
                connectionFactory.setConnectionTimeout(RabbitMQMessageServiceConfig.managed.get().timeout.int)
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
                    catch (IOException ignored) { /* It has already failed and that failure is handled below. */ }
                }
                throw new IOException("Failed to ensure connection to RabbitMQ due to instanceChannel create failure!", ioe)
            }
        }

        return "Connected to RabbitMQ server at " + RabbitMQMessageServiceConfig.managed.get().host + ":" +
                RabbitMQMessageServiceConfig.managed.get().port + "!"
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

        return "Disconnected from RabbitMQ server at " + RabbitMQMessageServiceConfig.managed.get().host +
                ":" + RabbitMQMessageServiceConfig.managed.get().port + "!"
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
