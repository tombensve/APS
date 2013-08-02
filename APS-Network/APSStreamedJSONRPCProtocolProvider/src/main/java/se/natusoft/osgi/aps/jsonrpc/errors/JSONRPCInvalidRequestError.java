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
 *         2012-01-08: Created!
 *         
 */
package se.natusoft.osgi.aps.jsonrpc.errors;

import se.natusoft.osgi.aps.api.net.rpc.errors.ErrorType;

/**
 * From http://jsonrpc.org/spec.html:
 * <p/>
 * The JSON sent is not a valid Request object.
 */
public class JSONRPCInvalidRequestError extends JSONRPCError {

    public JSONRPCInvalidRequestError(String message, Throwable cause) {
        super(ErrorType.INVALID_REQUEST, message, cause, null);
    }
    
    public JSONRPCInvalidRequestError(String message) {
        super(ErrorType.INVALID_REQUEST, message, "Invalid request!", null);
    }
}
