package se.natusoft.apsgroups.internal.net;

import se.natusoft.apsgroups.logging.APSGroupsLogger;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * This holds multiple transports.
 */
public class Transports {
    //
    // Private Members
    //

    private APSGroupsLogger logger;

    private List<Transport> multicastTransports = new LinkedList<>();

    private List<Transport> receivingTCPTransports = new LinkedList<>();

    private List<Transport> sendingTCPTransports = new LinkedList<>();

    //
    // Constructors
    //

    /**
     * Creates a new Transports instance.
     *
     * @param logger The logger to log to.
     */
    public Transports(APSGroupsLogger logger) {
        this.logger = logger;
    }

    //
    // Methods
    //

    /**
     * Adds a receiving TCP transport.
     *
     * @param transport The transport to add.
     */
    public void addReceivingTCPTransport(Transport transport) {
        this.receivingTCPTransports.add(transport);
    }

    /**
     * Returns the receiving TCP transports.
     */
    public List<Transport> getReceivingTCPTransports() {
        return this.receivingTCPTransports;
    }

    /**
     * Adds a sending TCP transport.
     *
     * @param transport The transport to add.
     */
    public void addSendingTCPTransport(Transport transport) {
        this.sendingTCPTransports.add(transport);
    }

    /**
     * Returns the sending TCP transports.
     */
    public List<Transport> getSendingTCPTransports() {
        return this.sendingTCPTransports;
    }

    /**
     * Adds a multicast transport.
     *
     * @param transport The transport to add.
     */
    public void addMulticastTransport(Transport transport) {
        this.multicastTransports.add(transport);
    }

    /**
     * Returns the multicast transports.
     */
    public List<Transport> getMulticastTransports() {
        return this.multicastTransports;
    }

    /**
     * Returns all receiving transports.
     */
    public List<Transport> getReceivingTransports() {
        List<Transport> transports = new LinkedList<>();
        transports.addAll(this.multicastTransports);
        transports.addAll(this.receivingTCPTransports);
        return transports;
    }

    /**
     * Returns all sending transports.
     */
    public List<Transport> getSendingTransports() {
        List<Transport> transports = new LinkedList<>();
        transports.addAll(this.multicastTransports);
        transports.addAll(this.sendingTCPTransports);
        return transports;
    }

    /**
     * Opens all transports.
     *
     * @throws IOException
     */
    public void openTransports() throws IOException {
        for (Transport transport : this.multicastTransports) {
            transport.open();
        }
        for (Transport transport : this.sendingTCPTransports) {
            transport.open();
        }
        for (Transport transport : this.receivingTCPTransports) {
            transport.open();
        }
    }

    /**
     * Closes all transports.
     *
     * @throws IOException
     */
    public void closeTransports() throws IOException {
        IOException exception = null;
        for (Transport transport : this.multicastTransports) {
            try {
                transport.close();
            }
            catch (IOException e) {
                this.logger.error("Failed to close transport!", e);
                exception = e;
            }
        }
        for (Transport transport : this.sendingTCPTransports) {
            try {
                transport.close();
            }
            catch (IOException e) {
                this.logger.error("Failed to close transport!", e);
                exception = e;
            }
        }
        for (Transport transport : this.receivingTCPTransports) {
            try {
                transport.close();
            }
            catch (IOException e) {
                this.logger.error("Failed to close transport!", e);
                exception = e;
            }
        }

        if (exception != null) {
            throw exception;
        }
    }

    /**
     * Sends the byte of data to the destination(s). This is a convenience for passing the data to
     * all transports.
     *
     * @param data The data to send.
     *
     * @throws java.io.IOException on failure to send.
     */
    public void send(byte[] data) throws IOException {
        for (Transport transport : getSendingTransports()) {
            transport.send(data);
        }
    }

}
