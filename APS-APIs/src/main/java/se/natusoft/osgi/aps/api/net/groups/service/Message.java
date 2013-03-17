/* 
 * 
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         0.9.1
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
 *         2012-12-28: Created!
 *         
 */
package se.natusoft.osgi.aps.api.net.groups.service;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * This represents a complete message containing any data you want to send to the group. You provide the message
 * with data using the OutputStream, and read message data using the InputStream.
 */
public interface Message {
    /**
     * Returns an OutputStream to write message on. Multiple calls to this will return the same OutputStream!
     */
    OutputStream getOutputStream();

    /**
     * Returns an InputStream for reading the message. Multiple calls to this will return new InputStream:s starting
     * from the beginning!
     */
    InputStream getInputStream();

    /**
     * Returns the id of this message.
     */
    UUID getId();

    /**
     * @return id of member as a string.
     */
    String getMemberId();

    /**
     * @return The name of the group this message belongs to.
     */
    String getGroupName();
}
