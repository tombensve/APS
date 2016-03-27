package se.natusoft.osgi.aps.api.net.tcpip;

import se.natusoft.osgi.aps.api.net.discovery.model.ServiceDescription;

/**
 * This defines a network configuration.
 */
public interface NetworkConfig {

    /**
     * The valid types of configuration.
     */
    enum Type {
        TCP,
        UDP,
        Multicast
    }

    /**
     * Returns the name of the configuration.
     */
    String getName();

    /**
     * Returns the type of this configuration.
     */
    Type getType();

    /**
     * Returns the IP address to listen or send to.
     */
    String getAddress();

    /**
     * Returns the port to listen or send to.
     */
    int getPort();

    /**
     * If true and security service is available it will be used.
     */
    boolean isSecure();

    /**
     * A convenience implementation of NetworkConfig with builder type setters.
     */
    class NetworkConfigProvider implements NetworkConfig {
        //
        // Private Members
        //

        private String name = "";
        private Type type = Type.TCP;
        private String address;
        private int port;
        private boolean secure = false;

        //
        // Constructors
        //

        /**
         * Creates a new NetworkConfigProvider.
         */
        public NetworkConfigProvider() {}

        /**
         * Creates a new NetworkConfigProvider.
         *
         * @param sd Use data from this ServiceDescription.
         */
        public NetworkConfigProvider(ServiceDescription sd) {
            this.name = sd.getServiceId();
            this.type = Type.valueOf(sd.getServiceProtocol());
            this.address = sd.getServiceHost();
            this.port = sd.getServicePort();
        }

        //
        // Methods
        //

        public NetworkConfigProvider setName(String name) {
            this.name = name;
            return this;
        }

        public NetworkConfigProvider setType(Type type) {
            this.type = type;
            return this;
        }

        public NetworkConfigProvider setAddress(String address) {
            this.address = address;
            return this;
        }

        public NetworkConfigProvider setPort(int port) {
            this.port = port;
            return this;
        }

        public NetworkConfigProvider setSecure(boolean secure) {
            this.secure = secure;
            return this;
        }

        /**
         * Returns the name of the configuration.
         */
        @Override
        public String getName() {
            return null;
        }

        /**
         * Returns the type of this configuration.
         */
        @Override
        public Type getType() {
            return null;
        }

        /**
         * Returns the IP address to listen or send to.
         */
        @Override
        public String getAddress() {
            return null;
        }

        /**
         * Returns the port to listen or send to.
         */
        @Override
        public int getPort() {
            return 0;
        }

        /**
         * If true and security service is available it will be used.
         */
        @Override
        public boolean isSecure() {
            return false;
        }
    }
}
