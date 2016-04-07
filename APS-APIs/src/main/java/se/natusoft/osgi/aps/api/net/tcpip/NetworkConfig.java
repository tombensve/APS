/*
 *
 * PROJECT
 *     Name
 *         APS APIs
 *
 *     Code Version
 *         1.0.0
 *
 *     Description
 *         Provides the APIs for the application platform services.
 *
 * COPYRIGHTS
 *     Copyright (C) 2012 by Natusoft AB All rights reserved.
 *
 * LICENSE
 *     Apache 2.0 (Open Source)
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 * AUTHORS
 *     tommy ()
 *         Changes:
 *         2016-02-27: Created!
 *
 */
package se.natusoft.osgi.aps.api.net.tcpip;

import se.natusoft.osgi.aps.api.net.discovery.DiscoveryKeys;

import java.util.Properties;

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
         * @param svcProps Use data from this discovered service.
         */
        public NetworkConfigProvider(Properties svcProps) {
            this.name = svcProps.getProperty(DiscoveryKeys.NAME);
            this.type = Type.valueOf(svcProps.getProperty(DiscoveryKeys.CONTENT_TYPE));
            this.address = svcProps.getProperty(DiscoveryKeys.HOST);
            this.port = Integer.valueOf(svcProps.getProperty(DiscoveryKeys.PORT));
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
