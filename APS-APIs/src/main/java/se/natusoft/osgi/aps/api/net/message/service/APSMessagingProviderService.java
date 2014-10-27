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

import java.util.List;

/**
 * This is a basic low level message API that should be implementable using almost any messaging provider.
 */
public interface APSMessagingProviderService {

    /**
     * The defined groups made available by this service.
     */
    List<String> providedGroups();

    /**
     * Sends a message.
     *
     * @param group The message group.
     * @param message The actual message to send.
     * @param sent If true then this message have already been sent somewhere by another provider.
     *
     * @throws APSMessageException on failure.
     *
     * @return true if the message was sent.
     */
    boolean sendMessage(String group, byte[] message, boolean sent) throws APSMessageException;

    /**
     * Reads a message.
     *
     * @param group The message group to read from.
     * @param timeout The amount of milliseconds to wait for message to become available.
     *
     * @return The message bytes or null on timeout.
     *
     * @throws APSMessageException on any failure.
     */
    byte[] readMessage(String group, int timeout) throws APSMessageException;

}
