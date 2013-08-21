/* 
 * 
 * PROJECT
 *     Name
 *         APS Groups
 *     
 *     Code Version
 *         0.9.2
 *     
 *     Description
 *         Provides network groups where named groups can be joined as members and then send and
 *         receive data messages to the group. This is based on multicast and provides a verified
 *         multicast delivery with acknowledgements of receive to the sender and resends if needed.
 *         The sender will get an exception if not all members receive all data. Member actuality
 *         is handled by members announcing themselves relatively often and will be removed when
 *         an announcement does not come in expected time. So if a member dies unexpectedly
 *         (network goes down, etc) its membership will resolve rather quickly. Members also
 *         tries to inform the group when they are doing a controlled exit. Most network aspects
 *         are configurable. Please note that this does not support streaming! That would require
 *         a far more complex protocol. It waits in all packets of a message before delivering
 *         the message.
 *         
 *         Note that even though this is an OSGi bundle, the jar produced can also be used as a
 *         library outside of OSGi. The se.natusoft.apsgroups.APSGroups API should then be used.
 *         This API has no external dependencies, only this jar is required for that use.
 *         
 *         When run with java -jar a for test command line shell will run where you can check
 *         members, send messages and files.
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
 *     Tommy Svensson (tommy@natusoft.se)
 *         Changes:
 *         2012-12-28: Created!
 *         
 */
package se.natusoft.apsgroups.config;

import java.util.List;

/**
 * Provides an implementation of APSGroupsConfig and also setters for the config values.
 */
public class APSGroupsConfigProvider implements APSGroupsConfig {
    //
    // Private Members
    //

    private int sendTimeout = 20;
    private int resendInterval = 5;
    private int memberAnnounceInterval = 5;

    //
    // Constructors
    //

    /**
     * Creates a new APSGroupsConfigProvider.
     * <p/>
     * This constructor will give you quite reasonable default values.
     */
    public APSGroupsConfigProvider() {}

    /**
     * Creates a new APSGroupsConfigProvider.
     * <p/>
     * All times are in seconds!
     *
     * @param sendTimeout How long to wait for a send to succeed before timeout.
     * @param resendInterval How long to wait for an acknowledgement before doing a resend of a packet.
     * @param memberAnnounceInterval The interval at which members announce themselves.
     */
    public APSGroupsConfigProvider(
            int sendTimeout,
            int resendInterval,
            int memberAnnounceInterval
    ) {
        this.sendTimeout = sendTimeout;
        this.resendInterval = resendInterval;
        this.memberAnnounceInterval = memberAnnounceInterval;
    }

    //
    // Members
    //

    /**
     * Sets how long to wait for a send to succeed before timeout.
     *
     * @param sendTimeout The timeout time to set.
     */
    public void setSendTimeout(int sendTimeout) {
        this.sendTimeout = sendTimeout;
    }

    /**
     * The number of seconds to allow for a send of a message before timeout.
     */
    @Override
    public int getSendTimeout() {
        return this.sendTimeout;
    }

    /**
     * How long to wait for an acknowledgement before doing a resend of a packet.
     *
     * @param resendInterval The resend interval to set.
     */
    public void setResendInterval(int resendInterval) {
        this.resendInterval = resendInterval;
    }

    /**
     * The number of seconds to wait before a packet is resent if not acknowledged.
     */
    @Override
    public int getResendInterval() {
        return this.resendInterval;
    }

    /**
     * The interval at which members announce themselves.
     *
     * @param memberAnnounceInterval The interval to set.
     */
    public void setMemberAnnounceInterval(int memberAnnounceInterval) {
        this.memberAnnounceInterval = memberAnnounceInterval;
    }

    /**
     * The interval in seconds that members announce that they are (sill) members. If a member has
     * not announced itself again within this time other members of the group will drop the member.
     */
    @Override
    public int getMemberAnnounceInterval() {
        return this.memberAnnounceInterval;
    }

    /**
     * Returns the configured transports.
     */
    @Override
    public List<TransportConfig> getTransports() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    //
    // Inner Classes
    //

    public static class APSTransportConfigProvider implements TransportConfig {
        //
        // Private Members
        //

        private TransportType transportType = TransportType.MULTICAST;

        private String host = "224.0.0.1";

        private int port = 58100;

        //
        // Constructors
        //

        /**
         * Creates a new APSTransportConfigProvider.
         */
        public APSTransportConfigProvider() {}

        /**
         * Creates a new APSTransportConfigProvider.
         *
         * @param transportType The type of transport configured.
         * @param host The host to talk to.
         * @param port The port on the host to talk to.
         */
        public APSTransportConfigProvider(TransportType transportType, String host, int port) {
            this.transportType = transportType;
            this.host = host;
            this.port = port;
        }

        //
        // Methods
        //

        /**
         * Sets the transport type.
         *
         * @param transportType The transport type to set.
         */
        public void setTransportType(TransportType transportType) {
            this.transportType = transportType;
        }

        /**
         * Returns the type of the transport.
         */
        @Override
        public TransportType getTransportType() {
            return this.transportType;
        }

        /**
         * Sets the host to communicate with.
         *
         * @param host The host to set.
         */
        public void setHost(String host) {
            this.host = host;
        }

        /**
         * Returns the host of the transport. IP address or hostname. This is only required for TCP_SENDER.
         */
        @Override
        public String getHost() {
            return this.host;
        }

        /**
         * Sets the port to use.
         *
         * @param port The port to set.
         */
        public void setPort(int port) {
            this.port = port;
        }

        /**
         * Returns the port to talk on.
         */
        @Override
        public int getPort() {
            return this.port;
        }
    }
}
