/* 
 * 
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         0.11.0
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
 *         2012-01-08: Created!
 *         
 */
package se.natusoft.osgi.aps.api.net.rpc.service;

import se.natusoft.osgi.aps.api.net.rpc.errors.RPCError;
import se.natusoft.osgi.aps.api.net.rpc.model.RPCRequest;
import se.natusoft.osgi.aps.api.net.rpc.model.RequestIntention;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * This represents an RPC protocol provider that provide client/service calls with requests read from an InputStream
 * or having parameters passes as strings and responses written to an OutputStream.
 *
 * HTTP transports can support both _parseRequests(...)_ and _parseRequest(...)_ while other transports probably can
 * handle only _parseRequests(...)_. __A protocol provider can return null for either of these!__ Most protocol
 * providers will support _parseRequests(...)_ and some also _parseRequest(...)_.
 */
public interface StreamedRPCProtocol extends RPCProtocol {

    /**
     * Parses a request from the provided InputStream and returns 1 or more RPCRequest objects.
     *
     * @param serviceQName A fully qualified name to the service to call. This can be null if service name is provided on the stream.
     * @param serviceClass The class of the service to call. Intended for looking for method annotations! Don't try to be "smart" here!
     * @param method The method to call. This can be null if method name is provided on the stream.
     * @param requestStream The stream to parse request from.
     * @param requestIntention The intention of the request (CRUD + UNKNOWN).
     *
     * @return The parsed requests.
     *
     * @throws IOException on IO failure.
     */
    List<RPCRequest> parseRequests(String serviceQName, Class serviceClass, String method, InputStream requestStream,
                                   RequestIntention requestIntention) throws IOException;

    /**
     * Provides an RPCRequest based on in-parameters. This variant supports HTTP transports.
     *
     * Return null for this if the protocol does not support this!
     *
     * @param serviceQName A fully qualified name to the service to call. This can be null if service name is provided on the stream.
     * @param serviceClass The class of the service to call. Intended for looking for method annotations! Don't try to be "smart" here!
     * @param method The method to call. This can be null if method name is provided on the stream.
     * @param parameters parameters passed as a
     * @param requestIntention The intention of the request (CRUD + UNKNOWN).
     *
     * @return The parsed requests.
     *
     * @throws IOException on IO failure.
     */
    RPCRequest parseRequest(String serviceQName, Class serviceClass, String method,
                            Map<String, String> parameters, RequestIntention requestIntention) throws IOException;

    /**
     * Writes a successful response to the specified OutputStream.
     *
     * @param result The resulting object of the RPC call or null if void return. If is possible a non void method also returns null!
     * @param request The request this is a response to.
     * @param responseStream The OutputStream to write the response to.
     *                       
     * @throws IOException on IO failure.
     */
    void writeResponse(Object result, RPCRequest request, OutputStream responseStream) throws IOException ;

    /**
     * Writes an error response.
     *
     * @param error The error to pass back.
     * @param request The request that this is a response to.
     * @param responseStream The OutputStream to write the response to.
     *
     * @return true if this call was handled and an error response was written. It returns false otherwise.
     *              If false the protocol is saying that it does not handle error responses.
     *
     *
     * @throws IOException on IO failure.
     */
    boolean writeErrorResponse(RPCError error, RPCRequest request, OutputStream responseStream) throws IOException;
}
