/* 
 * 
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         0.9.0
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
package se.natusoft.osgi.aps.api.net.rpc.model;

import se.natusoft.osgi.aps.api.net.rpc.errors.RPCError;

import java.util.*;

/**
 * This contains a parsed JSONRPC request.
 */
public abstract class AbstractRPCRequest implements RPCRequest {
    //
    // Private Members
    //

    /** The method to call. */
    private String method = null;
    
    /** The fully qualified name of the service to call if available. */
    private String serviceQName = null;

    /** The methods parameters as named parameters. */
    private Map<String, Object> namedParameters = new HashMap<String, Object>();

    /** The method parameters. */
    private List<Object> parameters = new LinkedList<Object>();

    /** The callId of the request */
    private Object callId = null;

    /** If valid is false then this should contain an error. */
    private RPCError error = null;

    //
    // Constructors
    //

    /**
     * Creates a new AbstractRPCRequest.
     *
     * @param method The method to call.
     */
    public AbstractRPCRequest(String method) {
        this.method = method;
    }

    /**
     * Creates a new AbstractRPCRequest.
     *
     * @param error An RPCError indicating a request problem, most probably of ErrorType.PARSE_ERROR type.
     */
    public AbstractRPCRequest(RPCError error) {
        this.error = error;
    }

    /**
     * Creates a new AbstractRPCRequest.
     *
     * @param method The method to call.
     * @param callId The callId of the call.
     */
    public AbstractRPCRequest(String method, Object callId) {
        this.method = method;
        this.callId = callId;
    }
    
    //
    // Methods
    //

    /**
     * @return The named parameters.
     */
    protected Map<String, Object> getNamedParameters() {
        return this.namedParameters;
    }

    /**
     * @return The sequential parameters.
     */
    protected List<Object> getParameters() {
        return this.parameters;
    }
    
    /**
     * Returns true if this request is valid. If this returns false all information except getError() is invalid, and
     * getError() should return a valid RPCError object.
     */
    @Override
    public boolean isValid() {
        return this.error == null;
    }

    /**
     * Returns an RPCError object if isValid() == false, null otherwise.
     */
    @Override
    public RPCError getError() {
        return this.error;
    }
    
    /**
     * Returns a fully qualified name of service to call. This will be null for protocols where service name is
     * not provided this way. So this cannot be taken for given!
     */
    @Override
    public String getServiceQName() {
        return this.serviceQName;   
    }

    /**
     * Sets the fully qualified name of the service to call. This is optional since not all protocol delivers a service name this way.
     *
     * @param serviceQName The service name to set.
     */
    public void setServiceQName(String serviceQName) {
        this.serviceQName = serviceQName;
    }

    /**
     * @return The method to call
     */
    @Override
    public String getMethod() {
        return this.method;
    }

    /**
     * Returns true if there is a call id available in the request.
     * <p/>
     * A call id is something that is received with a request and passed back with the
     * response to the request. Some RPC implementations will require this and some wont.
     */
    @Override
    public boolean hasCallId() {
        return this.callId != null;
    }
    
    /**
     * Returns the method call call Id.
     * <p/>
     * A call id is something that is received with a request and passed back with the
     * response to the request. Some RPC implementations will require this and some wont.
     */
    @Override
    public Object getCallId() {
        return this.callId;
    }
    
    /**
     * Adds a parameter. This is mutually exclusive with addParameter(name, parameter)!
     * 
     * @param parameter The parameter to add.
     */
    @Override
    public void addParameter(Object parameter) {
        this.parameters.add(parameter);
    }

    /**
     * Adds a named parameter. This is mutually exclusive with addParameter(parameter)!
     * 
     * @param name The name of the parameter.
     * @param parameter The parameter to add.
     */
    @Override
    public void addNamedParameter(String name, Object parameter) {
        this.namedParameters.put(name, parameter);
    }

    /**
     * @return The number of parameters available.
     */
    @Override
    public int getNumberOfParameters() {
        return this.parameters.size();
    }

    /**
     * @return true if there are named parameters available. If false the plain parameter list should be used.
     */
    @Override
    public boolean hasNamedParameters() {
        return !this.namedParameters.isEmpty();
    }

    /**
     * @return The available parameter names.
     */
    @Override
    public Set<String> getParameterNames() {
        return this.namedParameters.keySet();
    }
}
