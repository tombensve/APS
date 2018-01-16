package se.natusoft.osgi.aps.net.messaging.rabbitmq

import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.osgi.aps.net.messaging.config.Config

/**
 * Manages the RabbitMQ connection.
 */
@CompileStatic
@TypeChecked
class PeskyWabbitConnectionManager {
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
    synchronized String ensureConnection() throws IOException {

        if ( this.connectionFactory == null ) {
            this.connectionFactory = new ConnectionFactory()
            connectionFactory.host = Config.config.host as String
            connectionFactory.port = Config.config.port as int

            if ( !( Config.config.user as String ).isEmpty() ) {
                connectionFactory.username = Config.config.user as String
            }

            if ( !( Config.config.password as String ).isEmpty() ) {
                connectionFactory.password = Config.config.password as String
            }

            if ( !( Config.config.virtualHost as String ).isEmpty() ) {
                connectionFactory.virtualHost = Config.config.virtualHost as String
            }

            if ( (Config.config.timeout as int) > 0 ) {
                connectionFactory.connectionTimeout = Config.config.timeout as int
            }
        }

        if ( this.connection == null || !this.connection.isOpen() ) {
            try {
                this.connection = connectionFactory.newConnection()
            }
            catch ( IOException ioe ) {
                if ( this.connection != null ) {
                    try {
                        this.connection.close()
                    }
                    catch ( IOException ignored ) { /* It has already failed and that failure is handled below. */
                    }
                }
                throw new IOException( "Failed to ensure connection to RabbitMQ due to instanceChannel create failure!", ioe )
            }
        }

        return "Connected to RabbitMQ server at ${Config.config.host} : ${Config.config.port}!"
    }

    /**
     * Closes the connection if not closed already.
     */
    synchronized String ensureConnectionClosed() throws IOException {

        if ( this.connection != null ) {
            try {
                this.connection.close()
                this.connection = null
            }
            catch ( IOException ioe ) {
                throw new IOException( "Failed to disconnect from RabbitMQ!", ioe )
            }
        }

        this.connectionFactory = null

        return "Disconnected from RabbitMQ server at  ${Config.config.host} : ${Config.config.port}!"
    }

    /**
     * Closes connection and then creates a new again.
     */
    @SuppressWarnings("GroovyUnusedDeclaration")
    synchronized void reconnect() throws IOException {
        ensureConnectionClosed()
        ensureConnection()
    }

    /**
     * @return The current connection.
     */
    Connection getConnection() throws IOException {
        ensureConnection()

        return this.connection
    }

}
