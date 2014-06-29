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
 *         2012-01-30: Created!
 *         2013-03-15: Removed 2 brain-dead methods!
 *         
 */
package se.natusoft.osgi.aps.api.net.rpc.model;

import se.natusoft.osgi.aps.api.net.rpc.errors.RPCError;
import se.natusoft.osgi.aps.api.net.rpc.exceptions.RequestedParamNotAvailableException;

/**
 * This represents a request returned by protocol implementations.
 */
public interface RPCRequest {
    /**
     * Returns true if this request is valid. If this returns false all information except _getError()_ is **invalid**, and
     * _getError()_ should return a valid _RPCError_ object.
     */
    boolean isValid();

    /**
     * Returns an _RPCError_ object if `isValid() == false`, _null_ otherwise.
     */
    RPCError getError();

    /**
     * Returns a fully qualified name of service to call. This will be null for protocols where service name is
     * not provided this way. So this cannot be taken for given!
     */
    String getServiceQName();
    
    /**
     * Returns the method to call. This can return _null_ if the method is provided by other means, for example a
     * REST protocol where it will be part of the URL.
     */
    String getMethod();

    /**
     * Returns true if there is a call id available in the request.
     *
     * A call id is something that is received with a request and passed back with the
     * response to the request. Some RPC implementations will require this and some wont.
     */
    boolean hasCallId();

    /**
     * Returns the method call call Id.
     *
     * A call id is something that is received with a request and passed back with the
     * response to the request. Some RPC implementations will require this and some wont.
     */
    Object getCallId();

    /**
     * Return the number of parameters available.
     */
    int getNumberOfParameters();

    /**
     * Returns the parameter at the specified index.
     *
     * @param index The index of the parameter to get.
     * @param paramClass The expected class of the parameter.              
     *
     * @return The parameter object or null if indexed parameters cannot be delivered.
     *
     * @throws RequestedParamNotAvailableException if requested parameter is not available.
     */
    <T> T getIndexedParameter(int index, Class<T> paramClass) throws RequestedParamNotAvailableException;
}
