/* 
 * 
 * PROJECT
 *     Name
 *         APS Groups
 *     
 *     Code Version
 *         1.0.0
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

/**
 * Provides an implementation of APSGroupsConfig and also setters for the config values.
 */
public class APSGroupsConfigProvider implements APSGroupsConfig {
    //
    // Private Members
    //

    private String multicastAddress = "224.0.0.1";
    private int multicastPort = 58100;
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
     * @param multicastAddress The multicast address to use.
     * @param multicastPort The multicast port to use.
     * @param sendTimeout How long to wait for a send to succeed before timeout.
     * @param resendInterval How long to wait for an acknowledgement before doing a resend of a packet.
     * @param memberAnnounceInterval The interval at which members announce themselves.
     */
    public APSGroupsConfigProvider(
            String multicastAddress,
            int multicastPort,
            int sendTimeout,
            int resendInterval,
            int memberAnnounceInterval
    ) {
        this.multicastAddress = multicastAddress;
        this.multicastPort = multicastPort;
        this.sendTimeout = sendTimeout;
        this.resendInterval = resendInterval;
        this.memberAnnounceInterval = memberAnnounceInterval;
    }

    //
    // Members
    //

    /**
     * Sets the multicast address to use.
     *
     * @param multicastAddress The address to set.
     */
    public void setMulticastAddress(String multicastAddress) {
        this.multicastAddress = multicastAddress;
    }

    /**
     * The multicast address to use.
     */
    @Override
    public String getMulticastAddress() {
        return this.multicastAddress;
    }

    /**
     * Sets the multicast port to use.
     *
     * @param multicastPort The port to set.
     */
    public void setMulticastPort(int multicastPort) {
        this.multicastPort = multicastPort;
    }

    /**
     * The multicast target port to use.
     */
    @Override
    public int getMulticastPort() {
        return this.multicastPort;
    }

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
}
