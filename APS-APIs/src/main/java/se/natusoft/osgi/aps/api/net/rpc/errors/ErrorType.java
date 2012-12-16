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
 *     Tommy Svensson (tommy@natusoft.se)
 *         Changes:
 *         2012-01-30: Created!
 *         
 */
package se.natusoft.osgi.aps.api.net.rpc.errors;

/**
 * This defines what I think is a rather well though through set of error types applicable
 * for an RPC call. No they are not mine, they come from Matt Morley in his JSONRPC 2.0 specification
 * at http://jsonrpc.org/spec.html.
 */
public enum ErrorType {
    /**
     * Invalid input was received by the server. An error occurred on the server while parsing request data.
     */
    PARSE_ERROR,

    /**
     * The request data sent is not a valid.
     */
    INVALID_REQUEST,

    /**
     * The called method does not exist / is not available.
     */
    METHOD_NOT_FOUND,

    /**
     * The parameters to the method are invalid.
     */
    INVALID_PARAMS,

    /**
     * Internal protocol error.
     */
    INTERNAL_ERROR,

    /**
     * Server related errors.
     */
    SERVER_ERROR

}
