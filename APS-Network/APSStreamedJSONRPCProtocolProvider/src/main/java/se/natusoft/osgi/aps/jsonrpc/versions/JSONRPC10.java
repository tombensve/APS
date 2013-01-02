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
package se.natusoft.osgi.aps.jsonrpc.versions;

import se.natusoft.osgi.aps.api.misc.json.JSONErrorHandler;
import se.natusoft.osgi.aps.api.misc.json.service.APSJSONExtendedService;
import se.natusoft.osgi.aps.api.model.json.JSONArray;
import se.natusoft.osgi.aps.api.model.json.JSONObject;
import se.natusoft.osgi.aps.api.model.json.JSONString;
import se.natusoft.osgi.aps.api.model.json.JSONValue;
import se.natusoft.osgi.aps.api.net.rpc.errors.ErrorType;
import se.natusoft.osgi.aps.api.net.rpc.errors.RPCError;
import se.natusoft.osgi.aps.api.net.rpc.model.RPCRequest;
import se.natusoft.osgi.aps.api.net.rpc.streamed.service.StreamedRPCProtocol;
import se.natusoft.osgi.aps.exceptions.APSRuntimeException;
import se.natusoft.osgi.aps.jsonrpc.errors.*;
import se.natusoft.osgi.aps.jsonrpc.model.JSONRPCRequest;
import se.natusoft.osgi.aps.tools.APSLogger;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

/**
 * JSONRPC 1.0 implementation of JSONRPC as described on http://json-rpc.org/wiki/specification.
 */
public class JSONRPC10 implements StreamedRPCProtocol {
    private static final boolean debug = true;

    //
    // Private Members
    //

    /** The logger to log to. */
    private APSLogger logger = null;

    /** An APSServiceTracker wrapping of the APSJSONService. */
    private APSJSONExtendedService jsonService = null;


    //
    // Constructors
    //

    /**
     * Creates a new JSONRPC10 instance.
     *
     * @param logger A logger to log to.
     * @param jsonService An APSServiceTracker wrapping of the APSJSONService that will automatically handle the getting and releasing of the service upon calls.
     */
    public JSONRPC10(APSLogger logger, APSJSONExtendedService jsonService) {
        this.logger = logger;
        this.jsonService = jsonService;
    }

    //
    // Methods
    //

    /**
     * Checks of the specified request object is a valid request object for the implemented version of
     * the protocol.
     *
     * @param reqObject The request object to test.
     *
     * @return true or false.
     */
    private boolean isValidRequest(JSONObject reqObject) {
        // They unfortunately didn't think about a version specification in 1.0 so we identify it from not having one.
        JSONValue version = reqObject.getValue("jsonrpc");
        if (version != null && (version instanceof  JSONString)) {
            // This is not officially correct, but I do allow it.
            if (!((JSONString)version).equals("1.0")) {
                return false;
            }
        }
        if (reqObject.getValue("method") == null) {
            return false;
        }
        if (reqObject.getValue("params") == null) {
            return false;
        }
        if (reqObject.getValue("id") == null) {
            return false;
        }

        return true;
    }

    /**
     * Returns true if the protocol is a REST protocol.
     */
    @Override
    public boolean isREST() {
        return false;
    }

    /**
     * Parses a request from the provided InputStream and returns 1 or more RPCRequest objects.
     *
     * @param serviceQName A fully qualified name to the service to call. This can be null if service name is provided on the stream.
     * @param requestStream The stream to parse request from.
     *
     * @return The parsed requests.
     */
    @Override
    public List<RPCRequest> parseRequests(String serviceQName, InputStream requestStream) throws IOException {
        List<RPCRequest> requests = new LinkedList<RPCRequest>();

        try {
            // Read the JSON request
            ReqJSONErrorHandler errorHandler = new ReqJSONErrorHandler();
            JSONValue jsonReq = null;
            try {
                jsonReq = this.jsonService.readJSON(requestStream, errorHandler);
            }
            catch (JSONParseException jpe) {
                throw new JSONRPCParseError("Failed parsing request!", jpe);
            }

            if (jsonReq instanceof JSONArray) {
                for (JSONValue reqValue : ((JSONArray)jsonReq).getAsList()) {
                    if (reqValue instanceof JSONObject) {
                        JSONRPCRequest rpcReq = jsonObjectToRPCRequest((JSONObject)reqValue);
                        requests.add(rpcReq);
                    }
                    else {
                        throw new JSONRPCInvalidRequestError("Non JSON object value in request array! If the request starts with an " +
                        "array then the contents of the array has to be objects!");
                    }
                }
            }
            else if (jsonReq instanceof JSONObject) {
                JSONRPCRequest rpcReq = jsonObjectToRPCRequest((JSONObject)jsonReq);
                requests.add(rpcReq);
            }
            else {
                throw new JSONRPCInvalidRequestError("Received a request that is not a JSON object nor a JSON array!");
            }
        }
        catch (JSONRPCError error) {
            JSONRPCRequest req = new JSONRPCRequest(error);
            requests.add(req);
        }

        return requests;
    }

    /**
     * Converts a JSONObject to an RPCRequest.
     *
     * @param jsonObj The JSONObject to convert.
     *
     * @return The converted RPCRequest.
     */
    private JSONRPCRequest jsonObjectToRPCRequest(JSONObject jsonObj) {
        if (!isValidRequest(jsonObj)) {
            throw new JSONRPCInvalidRequestError("This request does not fulfill the minimum requirement for JSONRPC 1.0!");
        }

        String method = jsonObj.getValue("method").toString();
        JSONValue id = jsonObj.getValue("id");
        JSONValue params = jsonObj.getValue("params");

        JSONRPCRequest rpcReq = new JSONRPCRequest(method, id, this.jsonService);
        if (params instanceof JSONArray) {
            for (JSONValue param : ((JSONArray)params).getAsList()) {
                rpcReq.addParameter(param);
            }
        }
        else if (params instanceof JSONObject) {
            throw new JSONRPCInvalidRequestError("Named parameters are not supported by JSONRPC 1.0!");
        }
        else {
            throw new JSONRPCInvalidRequestError("The 'params' value must be an array!");
        }

        return rpcReq;
    }

    /**
     * Writes a successful response to the specified OutputStream.
     *
     * @param result         The resulting object of the RPC call or null if void return. If is possible a non void method also returns null!
     * @param request        The request this is a response to.
     * @param responseStream The OutputStream to write the response to.
     */
    @Override
    public void writeResponse(Object result, RPCRequest request, OutputStream responseStream) throws IOException {
        JSONObject resp = this.jsonService.createJSONObject();
        resp.addValue("result", this.jsonService.javaToJSON(result));
        resp.addValue("error", this.jsonService.createJSONNull());
        resp.addValue("id", (JSONValue)request.getCallId());
        this.jsonService.writeJSON(responseStream, resp, !debug);
    }

    /**
     * Writes an error response.
     *
     * @param error          The error to pass back.
     * @param request        The request that this is a response to.
     * @param responseStream The OutputStream to write the response to.
     */
    @Override
    public void writeErrorResponse(RPCError error, RPCRequest request, OutputStream responseStream) throws IOException {
        // If the parse of the request fails then we wont have any id!
        JSONValue id = (JSONValue)request.getCallId();
        if (id == null) {
            id = this.jsonService.createJSONNull();
        }

        JSONObject resp = this.jsonService.createJSONObject();
        resp.addValue("result", this.jsonService.createJSONNull());
        // Since the 1.0 specification does not specify what an error object is we just pass an error message as a string.
        resp.addValue("error", this.jsonService.createJSONString(error.getMessage()));
        resp.addValue("id", id);

        this.jsonService.writeJSON(responseStream, resp, !debug);
    }

    /**
     * @return The name of the provided protocol.
     */
    @Override
    public String getServiceProtocolName() {
        return "JSONRPC";
    }

    /**
     * @return The version of the implemented protocol.
     */
    @Override
    public String getServiceProtocolVersion() {
        return "1.0";
    }

    /**
     * @return The expected content type of a request. This should be verified by the transport if it has content type availability.
     */
    @Override
    public String getRequestContentType() {
        return "text/json";
    }

    /**
     * @return The content type of the response for when such can be provided.
     */
    @Override
    public String getResponseContentType() {
        return "text/json";
    }

    /**
     * @return A short description of the provided service. This should be in plain text.
     */
    @Override
    public String getRPCProtocolDescription() {
        return "This provides JSONRPC version 1.0 as described on http://json-rpc.org/wiki/specification over any streamed transport. " +
                "According to the 1.0 specification it is peer-to-peer. This implementation only provides the protocol, not the " +
                "transport so it has nothing to say about how data is sent back and forth. Please " +
                "note that due to JSON being less typed than Java, depending on the argument types and return type of the service " +
                "method being called the call can fail due to not being able to correctly convert call arguments to target arguments. " +
                "As long as the arguments and return data are primitives, Strings, or JavaBeans it should be OK. List, Maps and " +
                "Properties are also handled, but for List and Map it not possible to determine types when source is JSON.";
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
        RPCError error = null;

        switch (errorType) {
            case SERVER_ERROR:
                error = new JSONRPCServerError(message, 1);
                break;

            case INTERNAL_ERROR:
                error = new JSONRPCInternalError(message);
                break;

            case INVALID_PARAMS:
                error = new JSONRPCInvalidMethodParametersError(message);
                break;

            case INVALID_REQUEST:
                error = new JSONRPCInvalidRequestError(message);
                break;

            case METHOD_NOT_FOUND:
                error = new JSONRPCMethodNotFoundError(message);
                break;

            case PARSE_ERROR:
                error = new JSONRPCParseError(message);
        }

        return error;
    }

    /**
     * Returns an RPCError for a REST protocol with a http status code.
     *
     * @param httpStatusCode The http status code to return.
     */
    @Override
    public RPCError createRESTError(int httpStatusCode) {
        return null;
    }

    /**
     * Returns an RPCError for a REST protocol with a http status code.
     *
     * @param httpStatusCode The http status code to return.
     * @param message        An error message.
     */
    @Override
    public RPCError createRESTError(int httpStatusCode, String message) {
        return null;
    }

    //
    // Inner Classes
    //
    
    /**
     * This is thrown by the ReqSJONErrorHandler on failure.
     */
    private class JSONParseException extends APSRuntimeException {
        public JSONParseException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * This should be passed to a JSONObject that is going to be read from a stream.
     */
    private class ReqJSONErrorHandler implements JSONErrorHandler {
        //
        // Constructors
        //

        /**
         * Creates a new ReqJSONErrorHandler.
         */
        public ReqJSONErrorHandler() {}

        //
        // Methods
        //

        /**
         * Warns about something.
         *
         * @param message The warning message.
         */
        @Override
        public void warning(String message) {
            if (JSONRPC10.this.logger != null) {
                JSONRPC10.this.logger.warn(message);
            }
            else {
                System.err.println(message);
            }
        }

        /**
         * Indicate failure.
         *
         * @param message The failure message.
         * @param cause   The cause of the failure. Can be null!
         *
         * @throws RuntimeException This method must throw a RuntimeException.
         */
        @Override
        public void fail(String message, Throwable cause) throws RuntimeException {
            String failMessage = message;

            if (cause != null) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                cause.printStackTrace(pw);
                message += "\n" + sw.toString();
                pw.close();
            }

            throw new JSONParseException(message, cause);
        }

    }

}
