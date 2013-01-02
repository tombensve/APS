/* 
 * 
 * PROJECT
 *     Name
 *         APS External Protocol Extender
 *     
 *     Code Version
 *         1.0.0
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
import se.natusoft.osgi.aps.api.net.rpc.errors.ErrorType;
import se.natusoft.osgi.aps.api.net.rpc.errors.RPCError;
import se.natusoft.osgi.aps.api.net.rpc.service.RPCProtocol;

/**
 * This wraps a ServiceReference whose service instance implements the RPCProtocolService, extracting
 * and cacheing static information and wraps service get and release on protocol implementation calls.
 */
public class RPCProtocolWrapper implements RPCProtocol {
    //
    // Private Members
    //
    
    /** The context of the bundle this belongs to. */
    private BundleContext context = null;
    
    /** The reference to the protocol provider. */
    private ServiceReference protocolReference = null;

    /** The name of the protocol. */
    private String protocolName = null;

    /** The version of the protocol. */
    private String protocolVersion = null;

    /** The content type of the request. */
    private String requestContentType = null;

    /** The content type of the response. */
    private String responseContentType = null;

    /** A description of the protocol. */
    private String protocolDescripion = null;
    
    //
    // Constructors
    //

    /**
     * Creates a new RPCProtocolWrapper.
     * 
     * @param context Our bundle context.
     * @param protocolReference A reference to the protocol.
     * @param protocol An instance of the protocol the protocolReference references.
     */
    public RPCProtocolWrapper(BundleContext context, ServiceReference protocolReference, RPCProtocol protocol) {
        this.context = context;
        this.protocolReference = protocolReference;
        this.protocolName = protocol.getServiceProtocolName();
        this.protocolVersion = protocol.getServiceProtocolVersion();
        this.requestContentType = protocol.getRequestContentType();
        this.responseContentType = protocol.getResponseContentType();
        this.protocolDescripion = protocol.getRPCProtocolDescription();
    }
    
    //
    // Methods
    //

    /**
     * @return The reference to the protocol.
     */
    public ServiceReference getProtocolReference() {
        return this.protocolReference;
    }

    /**
     * Gets and returns the protocol provider instance from the context.
     */
    protected RPCProtocol getInstance() {
        return (RPCProtocol)this.context.getService(this.protocolReference);
    }

    /**
     * Ungets (releases) the protocol provider instance again.
     */
    protected void ungetInstance() {
        this.context.ungetService(this.protocolReference);
    }

    /**
     * @return The name of the provided protocol.
     */
    @Override
    public String getServiceProtocolName() {
        return this.protocolName;
    }

    /**
     * @return The version of the implemented protocol.
     */
    @Override
    public String getServiceProtocolVersion() {
        return this.protocolVersion;
    }

    /**
     * @return The expected content type of a request. This should be verified by the transport if it has content type availability.
     */
    @Override
    public String getRequestContentType() {
        return this.requestContentType;
    }

    /**
     * @return The content type of the response for when such can be provided.
     */
    @Override
    public String getResponseContentType() {
        return this.responseContentType;
    }

    /**
     * @return A short description of the provided service. This should be in plain text.
     */
    @Override
    public String getRPCProtocolDescription() {
        return this.protocolDescripion;
    }

    /**
     * Factory method to create an error object.
     *
     * @param errorType    The type of the error.
     * @param message      An error message.
     * @param optionalData Whatever optional data you want to pass along or null.
     *
     * @return An RPCError implementation.
     */
    @Override
    public RPCError createRPCError(ErrorType errorType, String message, String optionalData) {
        try {
            return getInstance().createRPCError(errorType, message, optionalData);
        }
        finally {
            ungetInstance();
        }
    }

    /**
     * Returns an RPCError for a REST protocol with a http status code.
     *
     * @param httpStatusCode The http status code to return.
     */
    @Override
    public RPCError createRESTError(int httpStatusCode) {
        RPCProtocol protocol = getInstance();
        RPCError error = protocol.createRESTError(httpStatusCode);
        ungetInstance();
        return error;
    }

    /**
     * Returns an RPCError for a REST protocol with a http status code.
     *
     * @param httpStatusCode The http status code to return.
     * @param message        An error message.
     */
    @Override
    public RPCError createRESTError(int httpStatusCode, String message) {
        RPCProtocol protocol = getInstance();
        RPCError error = protocol.createRESTError(httpStatusCode, message);
        ungetInstance();
        return error;
    }
}
