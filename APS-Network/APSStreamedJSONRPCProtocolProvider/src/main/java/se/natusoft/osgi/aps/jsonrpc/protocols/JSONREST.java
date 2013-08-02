/* 
 * 
 * PROJECT
 *     Name
 *         APS Streamed JSONRPC Protocol Provider
 *     
 *     Code Version
 *         0.9.2
 *     
 *     Description
 *         Provides JSONRPC implementations for version 1.0 and 2.0.
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
 *         2013-03-24: Created!
 *         
 */
package se.natusoft.osgi.aps.jsonrpc.protocols;

import se.natusoft.osgi.aps.api.misc.json.service.APSJSONExtendedService;
import se.natusoft.osgi.aps.tools.APSLogger;

/**
 * Provides a HTTP REST protocol.
 */
public class JSONREST  extends JSONHTTP {

    //
    // Constructors
    //

    /**
     * Creates a new JSONRPC20 instance.
     *
     * @param logger A logger to log to.
     * @param jsonService An APSServiceTracker wrapping of the APSJSONService that will automatically handle the getting
     *                    and releasing of the service upon calls.
     */
    public JSONREST(APSLogger logger, APSJSONExtendedService jsonService) {
        super(logger, jsonService);
    }

    //
    // Methods
    //

    /**
     * @return The name of the provided protocol.
     */
    @Override
    public String getServiceProtocolName() {
        return "JSONREST";
    }

    /**
     * @return The version of the implemented protocol.
     */
    @Override
    public String getServiceProtocolVersion() {
        return "1.0";
    }

    /**
     * @return A short description of the provided service. This should be in plain text.
     */
    @Override
    public String getRPCProtocolDescription() {
        return "This provides an HTTP REST protocol that talks JSON. Requests should specify both service and method to call in " +
                "URL path and method parameters as either HTTP URL parameters or within a JSON array on the stream. " +
                "URL parameters are required on GET while a JSON array on the stream is required on POST or PUT. " +
                "Whatever the method call returns are converted to JSON and written on the response OutputStream. " +
                "This protocol variant returns true for 'supportsREST()' and APSExternalProtocolService used by " +
                "transports provides 'isRESTCallable(String serviceName)' and 'getRESTCallable(String serviceName)' " +
                "and should in that case (if they are an http transport) use the REST callable which will map " +
                "POST, PUT, GET, and DELETE to methods starting with 'post', 'put', 'get', and 'delete' in the " +
                "service.";
    }

    /**
     * @return true if the protocol supports REST.
     */
    @Override
    public boolean supportsREST() {
        return true;
    }

}
