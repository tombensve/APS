/* 
 * 
 * PROJECT
 *     Name
 *         APS Streamed JSONRPC Protocol Provider
 *     
 *     Code Version
 *         0.9.0
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
 * This represents a parse error.
 * <p/>
 * From specification at http://jsonrpc.org/spec.html:
 * <p/>
 * Invalid JSON was received by the server.
 * An error occurred on the server while parsing the JSON text.
 */
public class JSONRPCServerError extends JSONRPCError {

    public JSONRPCServerError(String message, int errorCode, Throwable cause) {
        super(ErrorType.SERVER_ERROR, message, cause, errorCode);
    }

    public JSONRPCServerError(String message, int errorCode) {
        super(ErrorType.SERVER_ERROR, message, "Failed parsing request!", errorCode);
    }
}
