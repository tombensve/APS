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
package se.natusoft.osgi.aps.api.net.message.messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * A manager that writes message id before message is written and reads this id and creates
 * a correct message instance to read actual message.
 */
public interface APSMessageTypeManager {

    /**
     * Creates a new APSMessage subclass based on type.
     *
     * @param dataStream A DataInputStream to read information about what message to create from.
     *
     * @return A correct APSMessage subclass instance.
     *
     * @throws java.io.IOException on failure to read.
     */
    APSMessage createMessage(DataInputStream dataStream) throws IOException;

    /**
     * Writes message type identification before message.
     *
     * @param message The message that is about to be written.
     * @param dataStream The stream to write message type identification on.
     *
     * @throws IOException on failure to write.
     */
    void writeMessageType(APSMessage message, DataOutputStream dataStream) throws IOException;
}
