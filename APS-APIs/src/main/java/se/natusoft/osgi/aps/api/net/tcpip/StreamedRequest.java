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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

/**
 * This is used to make a streamed request.
 */
public interface StreamedRequest {

    /**
     * This must be implemented by clients that want to make a request.
     *
     * @param sendPoint This is only received as information. It is the send-point that will be sent to.
     * @param requestStream All data to send in the request should be written to this stream. DO NOT CLOSE THE STREAM!
     * @param responseStream If the send-point is async then this will be null otherwise a response to the request can
     *                       be read from this stream.
     */
    void sendRequest(URI sendPoint, OutputStream requestStream, InputStream responseStream) throws IOException;
}
