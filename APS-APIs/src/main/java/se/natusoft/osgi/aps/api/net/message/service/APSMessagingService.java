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
 *     Tommy Svensson (tommy@natusoft.se)
 *         Changes:
 *         2014-10-27: Created!
 *
 */
package se.natusoft.osgi.aps.api.net.message.service;

import se.natusoft.osgi.aps.api.net.message.exception.APSMessageException;
import se.natusoft.osgi.aps.api.net.message.messages.APSMessageTypeManager;

/**
 * This defines a higher level message service that is easy to use.
 *
 * The intention with this service is to build on top of APSMessagingProviderService
 * and APSMessageGroupService, which is what the APS project supplies. This can however
 * be implemented in any way wanted.
 */
public interface APSMessagingService<MsgBaseType> {

    /**
     * Connects to a specific group to send and receive messages to/from.
     *
     * @param group The name of the group to connect to.
     * @param msgMgr An implementation of APSMessageTypeManager that is involved in
     *               both writing and reading message type and producing message
     *               instances. This is supplied externally since it involves the
     *               actual message protocol, which this service in itself does
     *               not care about.
     *
     * @return An MessageGroup instance representing the group connected to. All communication with the group are
     *         done with this.
     *
     * @throws APSMessageException on any failure to connect to the group. This depends a lot on the underlying
     *         messaging provider.
     */
    public MessageGroup<MsgBaseType> connectToGroup(String group, APSMessageTypeManager msgMgr) throws APSMessageException;

    /**
     * This represents a connected to group to which messages will be send and received.
     *
     * @param <MsgBaseType> The base message class of the sent and received messages. This can
     *                      be a subclass of APSMessage.
     */
    public static interface MessageGroup<MsgBaseType> {

        /**
         * Sends a message.
         *
         * @param message The message to send.
         *
         * @throws APSMessageException On failure to send message.
         */
        void sendMessage(MsgBaseType message) throws APSMessageException;

        /**
         * @param timeout The timeout in seconds to wait for a message.
         *
         * @return The read message or null on timeout.
         *
         * @throws APSMessageException on any read failure.
         */
        MsgBaseType readMessage(int timeout) throws APSMessageException;

        /**
         * Adds a listener for received messages. This is mutually exclusive
         * with readMessage(...)! If you call this method then any call to
         * readMessage(...) will throw an APSMessageException!
         *
         * @param listener The listener to call with messages.
         */
        void addMessageListener(APSMessageListener<MsgBaseType> listener);

        /**
         * Removes a message listener.
         * <p/>
         * If there are no more listeners after the call then readMessage(...) should be
         * possible again.
         *
         * @param listener The listener to remove.
         */
        void removeMessageListener(APSMessageListener<MsgBaseType> listener);

        /**
         * Disconnects from the group.
         *
         * @throws APSMessageException on any failure.
         */
        void disconnect() throws APSMessageException;
    }
}
