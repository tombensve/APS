/* 
 * 
 * PROJECT
 *     Name
 *         APS Streamed JSONRPC Protocol Provider
 *     
 *     Code Version
 *         0.11.0
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
 *         2012-01-30: Created!
 *         
 */
package se.natusoft.osgi.aps.jsonrpc.model;

import se.natusoft.osgi.aps.api.misc.json.model.JSONValue;
import se.natusoft.osgi.aps.api.misc.json.service.APSJSONExtendedService;
import se.natusoft.osgi.aps.api.net.rpc.errors.RPCError;
import se.natusoft.osgi.aps.api.net.rpc.model.AbstractRPCRequest;
import se.natusoft.osgi.aps.api.net.rpc.model.RPCRequest;

/**
 * Implementation of RPCRequest that holds JSONValue objects and converts them to Java on get.
 */
public class JSONRPCRequest extends AbstractRPCRequest implements RPCRequest {
    //
    // Private Members
    //

    /** We need this to convert between JSON and Java. */
    private APSJSONExtendedService jsonService = null;

    //
    // Constructors
    //

    /**
     * Creates a new JSONRPCRequest.
     *
     * @param method The method to call.
     * @param jsonService Needed for JSON<-->Java conversions.
     */
    public JSONRPCRequest(String method, APSJSONExtendedService jsonService) {
        super(method);
        this.jsonService = jsonService;
    }

    /**
     * Creates a new JSONRPCRequest.
     *
     * @param method The method to call.
     * @param callId The callId of the call.
     * @param jsonService Needed for JSON<-->Java conversions.
     */
    public JSONRPCRequest(String method, Object callId, APSJSONExtendedService jsonService) {
        super(method, callId);
        this.jsonService = jsonService;
    }

    /**
     * Creates a new JSONRPCRequest.
     *
     * @param error An RPCError indicating a request problem, most probably of ErrorType.PARSE_ERROR type.
     */
    public JSONRPCRequest(RPCError error) {
        super(error);
    }

    //
    // Methods
    //

    /**
     * Returns the parameter at the specified index.
     *
     * @param index The index of the parameter to get.
     * @param paramClass The expected class of the parameter.
     *
     * @return The parameter object.
     */
    @Override
    public <T> T getIndexedParameter(int index, Class<T> paramClass) {
        JSONValue jval = (JSONValue)getParameters().get(index);
        return this.jsonService.jsonToJava(jval, paramClass);
    }
}
