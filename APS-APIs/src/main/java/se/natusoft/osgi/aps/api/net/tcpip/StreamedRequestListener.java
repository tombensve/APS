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
 * This defines a listener of streamed requests.
 */
public interface StreamedRequestListener {

    /**
     * Listeners of requests should implement this.
     *
     * @param receivePoint The receive-point the listener was registered with.
     * @param requestStream This contains the request data. DO NOT CLOSE THIS STREAM!
     * @param responseStream If receive-point is marked as async then this will be null, otherwise a
     *                       response should be written to this. DO NOT CLOSE THIS STREAM.
     */
    void requestReceived(URI receivePoint, InputStream requestStream, OutputStream responseStream) throws IOException;
}
