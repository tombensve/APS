/* 
 * 
 * PROJECT
 *     Name
 *         APS External Protocol Extender
 *     
 *     Code Version
 *         0.9.2
 *     
 *     Description
 *         This does two things:
 *         
 *         1) Looks for "APS-Externalizable: true" MANIFEST.MF entry in deployed bundles and if found and bundle status is
 *         ACTIVE, analyzes the service API and creates an APSExternallyCallable wrapper for each service method and
 *         keeps them in memory until bundle state is no longer ACTIVE. In addition to the MANIFEST.MF entry it has
 *         a configuration of fully qualified service names that are matched against the bundles registered services
 *         for which an APSExternallyCallable wrapper will be created.
 *         
 *         2) Registers an APSExternalProtocolExtenderService making the APSExternallyCallable objects handled available
 *         to be called. Note that APSExternallyCallable is an interface extending java.util.concurrent.Callable.
 *         This service is used by other bundles making the service available remotely trough some protocol like
 *         JSON for example.
 *         
 *         This extender is a middleman making access to services very easy to expose using whatever protocol you want.
 *         Multiple protocol bundles using the APSExternalProtocolExtenderService can be deployed at the same time making
 *         services available through more than one protocol.
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
 *         2012-02-02: Created!
 *         
 */
package se.natusoft.osgi.aps.externalprotocolextender.model;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import se.natusoft.osgi.aps.api.net.rpc.errors.RPCError;
import se.natusoft.osgi.aps.api.net.rpc.model.RPCRequest;
import se.natusoft.osgi.aps.api.net.rpc.service.StreamedRPCProtocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * This wraps a ServiceReference whose service instance implements the StreamedRPCProtocol, extracting
 * and caching static information and wraps service get and release on protocol implementation calls.
 */
public class StreamedRPCProtocolWrapper extends RPCProtocolWrapper implements StreamedRPCProtocol {
    //
    // Constructors
    //

    /**
     * Creates a new StreamedRPCProtocolWrapper.
     *
     * @param context Our bundle context.
     * @param protocolReference A reference to the protocol.
     * @param protocol An instance of the protocol the protocolReference references.
     */
    public StreamedRPCProtocolWrapper(BundleContext context, ServiceReference protocolReference, StreamedRPCProtocol protocol) {
        super(context, protocolReference, protocol);
    }

    //
    // Methods
    //


    /**
     * Gets and returns the protocol provider instance from the context.
     */
    protected StreamedRPCProtocol getInstance() {
        return (StreamedRPCProtocol)super.getInstance();
    }

    /**
     * Parses a request from the provided InputStream and returns 1 or more RPCRequest objects.
     *
     * @param serviceQName  A fully qualified name to the service to call. This can be null if service name is provided on the stream.
     * @param method        The method to call. This can be null if method name is provided on the stream.
     * @param requestStream The stream to parse request from.
     * @return The parsed requests.
     * @throws java.io.IOException on IO failure.
     */
    @Override
    public List<RPCRequest> parseRequests(String serviceQName, String method, InputStream requestStream) throws IOException {
        try {
            return getInstance().parseRequests(serviceQName, method, requestStream);
        }
        finally {
            ungetInstance();
        }
    }

    /**
     * Provides an RPCRequest based on in-parameters. This variant supports HTTP transports.
     *
     * @param serviceQName A fully qualified name to the service to call. This can be null if service name is provided on the stream.
     * @param method       The method to call. This can be null if method name is provided on the stream.
     * @param parameters   parameters passed as a
     * @return The parsed requests.
     * @throws java.io.IOException on IO failure.
     */
    @Override
    public RPCRequest parseRequest(String serviceQName, String method, Map<String, String> parameters) throws IOException {
        try {
            return getInstance().parseRequest(serviceQName, method, parameters);
        }
        finally {
            ungetInstance();
        }
    }

    /**
     * Writes a successful response to the specified OutputStream.
     *
     * @param result         The resulting object of the RPC call or null if void return. If is possible a non void method also returns null!
     * @param request        The request this is a response to.
     * @param responseStream The OutputStream to write the response to.
     *
     * @throws java.io.IOException on IO failure.
     */
    @Override
    public void writeResponse(Object result, RPCRequest request, OutputStream responseStream) throws IOException {
        try {
            getInstance().writeResponse(result, request, responseStream);
        }
        finally {
            ungetInstance();
        }
    }

    /**
     * Writes an error response.
     *
     * @param error          The error to pass back.
     * @param request        The request that this is a response to.
     * @param responseStream The OutputStream to write the response to.
     *
     * @throws java.io.IOException on IO failure.
     */
    @Override
    public boolean writeErrorResponse(RPCError error, RPCRequest request, OutputStream responseStream) throws IOException {
        try {
            return getInstance().writeErrorResponse(error, request, responseStream);
        }
        finally {
            ungetInstance();
        }
    }
}
