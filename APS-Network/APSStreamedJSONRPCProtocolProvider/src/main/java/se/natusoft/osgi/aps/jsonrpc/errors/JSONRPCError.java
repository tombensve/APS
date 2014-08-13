/* 
 * 
 * PROJECT
 *     Name
 *         APS Streamed JSONRPC Protocol Provider
 *     
 *     Code Version
 *         1.0.0
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
import se.natusoft.osgi.aps.api.net.rpc.errors.RPCError;
import se.natusoft.osgi.aps.exceptions.APSRuntimeException;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * This is a base class for JSONRPC protocol errors.
 */
public class JSONRPCError extends APSRuntimeException implements RPCError {
    //
    // Private Members
    //

    /** The type of the error. */
    private ErrorType errorType = null;
    
    /** Optional data. */
    private String data = null;

    /** This should only be provided when type is SERVER_ERROR. */
    private Integer svcImplErrorCodeOffset = null;
    
    //
    // Constructors
    //

    /**
     * Creates a new JSONRPCError instance.
     *
     * @param errorType The type of error.
     * @param message The error message.
     * @param data Optional data.
     * @oaram svcImplErrorCodeOffset The error code for type SERVER_ERROR. Valid offset range are 0 - 99. This will be modified on the way back
     *                               depending on the JSONRPC implementation.
     */
    public JSONRPCError(ErrorType errorType, String message, String data, Integer svcImplErrorCodeOffset) {
        super(message);
        this.errorType = errorType;
        this.data = data;
        this.svcImplErrorCodeOffset = svcImplErrorCodeOffset;
    }

    /**
     * Creates a new JSONRPCError instance.
     *
     * @param errorType The type of error.
     * @param message The error message.
     * @param cause The cause of the error.
     * @oaram svcImplErrorCodeOffset The error code for type SERVER_ERROR. Valid offset range are 0 - 99. This will be modified on the way back
     *                               depending on the JSONRPC implementation.
     */
    public JSONRPCError(ErrorType errorType, String message, Throwable cause, Integer svcImplErrorCodeOffset) {
        super(message);
        this.errorType = errorType;
        this.svcImplErrorCodeOffset = svcImplErrorCodeOffset;
        if (cause != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            cause.printStackTrace(pw);
            this.data = sw.toString();
            pw.close();
        }
    }

    //
    // Methods
    //

    /**
     * @return The type of the error.
     */
    public ErrorType getErrorType() {
        return this.errorType;
    }

    /**
     * A potential error code.
     */
    @Override
    public String getErrorCode() {
        return "" + this.svcImplErrorCodeOffset;
    }

    /**
     * @return true if there is optional data available.
     */
    public boolean hasOptionalData() {
        return this.data != null;
    }

    /**
     * @return The optional data.
     */
    public String getOptionalData() {
        return this.data;
    }
}
