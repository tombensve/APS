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
package se.natusoft.osgi.aps.jsonrpc.protocols;

import se.natusoft.osgi.aps.api.misc.json.JSONEOFException;
import se.natusoft.osgi.aps.api.misc.json.JSONErrorHandler;
import se.natusoft.osgi.aps.api.misc.json.model.JSONArray;
import se.natusoft.osgi.aps.api.misc.json.model.JSONValue;
import se.natusoft.osgi.aps.api.misc.json.service.APSJSONExtendedService;
import se.natusoft.osgi.aps.api.net.rpc.errors.ErrorType;
import se.natusoft.osgi.aps.api.net.rpc.errors.HTTPError;
import se.natusoft.osgi.aps.api.net.rpc.errors.RPCError;
import se.natusoft.osgi.aps.api.net.rpc.exceptions.RequestedParamNotAvailableException;
import se.natusoft.osgi.aps.api.net.rpc.model.RPCRequest;
import se.natusoft.osgi.aps.api.net.rpc.model.RequestIntention;
import se.natusoft.osgi.aps.api.net.rpc.service.StreamedRPCProtocol;
import se.natusoft.osgi.aps.exceptions.APSRuntimeException;
import se.natusoft.osgi.aps.tools.APSLogger;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Implements a plain REST protocol.
 */
public class JSONHTTP implements StreamedRPCProtocol {

    private static final boolean debug = false;

    //
    // Private Members
    //

    /** The logger to log to. */
    protected APSLogger logger = null;

    /** An APSServiceTracker wrapping of the APSJSONService. */
    protected APSJSONExtendedService jsonService = null;


    //
    // Constructors
    //

    /**
     * Creates a new JSONHTTP instance.
     *
     * @param logger A logger to log to.
     * @param jsonService An APSServiceTracker wrapping of the APSJSONService that will automatically handle the getting and
     *                    releasing of the service upon calls.
     */
    public JSONHTTP(APSLogger logger, APSJSONExtendedService jsonService) {
        this.logger = logger;
        this.jsonService = jsonService;
    }

    //
    // Methods
    //

    /**
     * Parses a request from the provided InputStream and returns 1 or more RPCRequest objects.
     *
     * @param serviceQName  A fully qualified name to the service to call. This can be null if service name is provided on the stream.
     * @param serviceClass The class of the service to call.
     * @param method        The method to call. This can be null if method name is provided on the stream.
     * @param requestStream The stream to parse request from.
     * @param requestIntention The intention of the request (CRUD)
     *
     * @return The parsed requests.
     * @throws java.io.IOException on IO failure.
     */
    @Override
    public List<RPCRequest> parseRequests(String serviceQName, Class serviceClass, String method, InputStream requestStream,
                                          RequestIntention requestIntention) throws IOException {
        List<RPCRequest> requests = new LinkedList<>();

        while (true) {
            try {
                // Read the JSON request
                ReqJSONRESTErrorHandler errorHandler = new ReqJSONRESTErrorHandler();
                JSONArray jsonReq = null;
                JSONValue jsonReqValue = null;
                try {
                    jsonReqValue = this.jsonService.readJSON(requestStream, errorHandler);
                    jsonReq = (JSONArray)jsonReqValue;
                }
                catch (JSONEOFException eofe) {
                    break;
                }
                catch (JSONParseException jpe) {
                    throw new JSONRESTError(ErrorType.PARSE_ERROR, 400, "Bad JSON passed!", null, jpe);
                }
                catch (ClassCastException cce) {
                    throw new JSONRESTError(ErrorType.INVALID_PARAMS, 400, "An array of JSON values/objects are required!", null, cce);
                }

                JSONRESTRequest req = new JSONRESTRequest(serviceQName, method, jsonReq.getAsList(), null);
                requests.add(req);
            }
            catch (JSONRESTError error) {
                JSONRESTRequest req = new JSONRESTRequest(serviceQName, method, error);
                requests.add(req);
            }
        }

        return requests;
    }

    /**
     * Provides an RPCRequest based on in-parameters. This variant supports HTTP transports.
     *
     * @param serviceQName A fully qualified name to the service to call. This can be null if service name is provided on the stream.
     * @param serviceClass The class of the service to call.
     * @param method       The method to call. This can be null if method name is provided on the stream.
     * @param parameters   parameters passed as a
     * @param requestIntention The intention of the request (CRUD)
     *
     * @return The parsed requests.
     * @throws java.io.IOException on IO failure.
     */
    @Override
    public RPCRequest parseRequest(String serviceQName, Class serviceClass, String method, Map<String, String> parameters,
                                   RequestIntention requestIntention) throws IOException {
        RPCRequest request = null;

        try {
            List<String> params = new LinkedList<>();
            if (parameters.containsKey("params")) {
                for (String param : parameters.get("params").split(":")) {
                    params.add(param.trim());
                }
            }
            else {
                if (parameters.size() > 0) {
                    StringBuilder sb = new StringBuilder();
                    String comma = "";
                    for (String providedParam : parameters.keySet()) {
                        sb.append(comma);
                        sb.append(providedParam);
                        comma = ", ";
                    }
                    throw new JSONRESTError(ErrorType.INVALID_PARAMS, 400, "parameter 'params' where not provided but the " +
                            "following were: " + sb.toString() + "!", null, null);
                }
            }

            request = new JSONRESTRequest(serviceQName, method, null, params);
        }
        catch (JSONRESTError error) {
            request = new JSONRESTRequest(serviceQName, method, error);
        }

        return request;
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
        this.jsonService.writeJSON(responseStream, this.jsonService.javaToJSON(result), !debug);
    }

    /**
     * Writes an error response.
     *
     * @param error          The error to pass back.
     * @param request        The request that this is a response to.
     * @param responseStream The OutputStream to write the response to.
     */
    @Override
    public boolean writeErrorResponse(RPCError error, RPCRequest request, OutputStream responseStream) throws IOException {
        return false;
    }

    /**
     * @return The name of the provided protocol.
     */
    @Override
    public String getServiceProtocolName() {
        return "JSONHTTP";
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
        return "application/json";
    }

    /**
     * @return The content type of the response for when such can be provided.
     */
    @Override
    public String getResponseContentType() {
        return "application/json";
    }

    /**
     * @return A short description of the provided service. This should be in plain text.
     */
    @Override
    public String getRPCProtocolDescription() {
        return "This provides an HTTP protocol that talks JSON. Requests should specify both service and method to call in " +
                "URL path and method parameters as either HTTP URL parameters or within a JSON array on the stream. " +
                "URL parameters are required on GET while a JSON array on the stream is required on POST or PUT. " +
                "Whatever the method call returns are converted to JSON and written on the response OutputStream. " +
                "This not a REST protocol and requires a method specification on the URL. It will however respond with " +
                "http status codes on error, and a 200 on success.";
    }

    /**
     * Factory method to create an error object.
     *
     * @param errorType    The type of the error.
     * @param message      An error message.
     * @param optionalData Whatever optional data you want to pass along or null.
     * @param cause        The cause of the error.
     * @return An RPCError implementation or null if not handled by the protocol implementation.
     */
    @Override
    public RPCError createRPCError(ErrorType errorType, String message, String optionalData, Throwable cause) {
        int httpStatusCode;

        switch (errorType) {
            case INTERNAL_ERROR:
                httpStatusCode = HttpServletResponse.SC_BAD_REQUEST;
                message = "[Protocol error!] " + message;
                break;

            case INVALID_PARAMS:
                httpStatusCode = HttpServletResponse.SC_BAD_REQUEST;
                message = "[Invalid parameters!] " + message;
                break;

            case INVALID_REQUEST:
                httpStatusCode = HttpServletResponse.SC_BAD_REQUEST;
                break;

            case METHOD_NOT_FOUND:
                httpStatusCode = HttpServletResponse.SC_NOT_FOUND;
                break;

            case PARSE_ERROR:
                httpStatusCode = HttpServletResponse.SC_BAD_REQUEST;
                break;

            case SERVICE_NOT_FOUND:
                httpStatusCode = HttpServletResponse.SC_SERVICE_UNAVAILABLE;
                break;

            case SERVER_ERROR:
                httpStatusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                break;

            case AUTHORIZATION_REQUIRED:
                httpStatusCode = HttpServletResponse.SC_UNAUTHORIZED;
                message = "[Authorization is required!] " + message;
                break;

            case BAD_AUTHORIZATION:
                httpStatusCode = HttpServletResponse.SC_UNAUTHORIZED;
                break;

            default:
                httpStatusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        }

        return new JSONRESTError(errorType, httpStatusCode, message, optionalData, cause);
    }

    //
    // Inner Classes
    //

    /**
     * This is thrown by the ReqJSONRESTErrorHandler on failure.
     */
    protected class JSONParseException extends APSRuntimeException {
        public JSONParseException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * This should be passed to a JSONObject that is going to be read from a stream.
     */
    protected class ReqJSONRESTErrorHandler implements JSONErrorHandler {
        //
        // Constructors
        //

        /**
         * Creates a new ReqJSONRESTErrorHandler.
         */
        public ReqJSONRESTErrorHandler() {}

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
            if (JSONHTTP.this.logger != null) {
                JSONHTTP.this.logger.warn(message);
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
            throw new JSONParseException(message, cause);
        }

    }

    /**
     * The RPCRequest implementation to return from parseRequests.
     */
    protected class JSONRESTRequest implements RPCRequest {
        //
        // Private Members
        //

        /** The service to call. */
        private String setServiceQName;

        /** The method of the service to call. */
        private String method;


        /** JSONObject parameters. */
        private List<JSONValue> jsonParams;

        /** String parameters. */
        private List<String> stringParams;

        /** An error object when request parsing fails. */
        private HTTPError error;

        //
        // Constructors
        //

        /**
         * Creates a new JSONRESTRequest.
         *
         * @param serviceQName The fully qualified name of the service.
         * @param method The method to call.
         * @param jsonParams Any JSON parameters.
         * @param stringParams Any String parameters.
         */
        public JSONRESTRequest(String serviceQName, String method, List<JSONValue> jsonParams, List<String> stringParams) {
            this.setServiceQName = serviceQName;
            this.method = method;
            this.jsonParams = jsonParams;
            this.stringParams = stringParams;
        }

        /**
         * Creates a new JSONRESTRequest representing a failed request.
         *
         * @param setServiceQName The fully qualified name of the service.
         * @param method The method to call.
         * @param error The error describing the failure.
         */
        public JSONRESTRequest(String setServiceQName, String method, HTTPError error) {
            this.setServiceQName = setServiceQName;
            this.method = method;
            this.error = error;
        }

        //
        // Methods
        //

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
            return this.setServiceQName;
        }

        /**
         * @return The method to call. This can return null if the method is provided by other means, for example a
         *         REST protocol where it will be part of the URL.
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
            return false;
        }

        /**
         * Returns the method call call Id.
         * <p/>
         * A call id is something that is received with a request and passed back with the
         * response to the request. Some RPC implementations will require this and some wont.
         */
        @Override
        public Object getCallId() {
            return null;
        }

        /**
         * @return The number of parameters available.
         */
        @Override
        public int getNumberOfParameters() {
            return this.jsonParams != null ? this.jsonParams.size() : this.stringParams.size();
        }


        /**
         * Returns the parameter at the specified index.
         *
         * @param index      The index of the parameter to get.
         * @param paramClass The expected class of the parameter.
         * @return The parameter object or null if indexed parameters cannot be delivered.
         * @throws RequestedParamNotAvailableException if requested parameter is not available.
         */
        @Override
        public <T> T getIndexedParameter(int index, Class<T> paramClass) throws RequestedParamNotAvailableException  {
            if (this.jsonParams != null) {
                return getIndexedJSONParameter(index, paramClass);
            }
            else {
                return getIndexedStringParameter(index, paramClass);
            }
        }

        private <T> T getIndexedJSONParameter(int index, Class<T> paramClass) throws RequestedParamNotAvailableException  {
            try {
                JSONValue jsonParam = this.jsonParams.get(index);
                T paramObject = JSONHTTP.this.jsonService.jsonToJava(jsonParam, paramClass);
                if (paramObject == null) throw new RequestedParamNotAvailableException("A qualifying parameter:" + index +
                        " resolvable to a '" + paramClass + "' has not been provided!");

                return paramObject;
            }
            catch (IndexOutOfBoundsException iobe) {
                throw new RequestedParamNotAvailableException("Expected parameter (#" + index + ") have not been provided!", iobe);
            }
        }

        private <T> T getIndexedStringParameter(int index, Class<T> paramClass) throws RequestedParamNotAvailableException  {
            try {
                String strParam = this.stringParams.get(index);
                T paramObject = stringToType(strParam, paramClass);
                if (paramObject == null) throw new RequestedParamNotAvailableException("A qualifying parameter:" + index +
                        " resolvable to a '" + paramClass + "' has not been provided!");

                return paramObject;
            }
            catch (NumberFormatException nfe) {
                throw new RequestedParamNotAvailableException("Parameter #" + index + " does not match the expected type of '" +
                        paramClass + "'!", nfe);
            }
            catch (IndexOutOfBoundsException iobe) {
                throw new RequestedParamNotAvailableException("Expected parameter (#" + index + ") have not been provided!", iobe);
            }
        }

        /**
         * Returns a value in the specified type (String or primitive).
         *
         * @param value The value to convert.
         * @param type The type to convert to.
         * @param <T> The type to return.
         *
         * @return A converted value.
         */
        @SuppressWarnings("unchecked")
        private <T> T stringToType(String value, Class<T> type) {
            if (type.isAssignableFrom(String.class)) {
                return (T)value;
            }
            else if (type.isAssignableFrom(Character.class) || type.isAssignableFrom(char.class)) {
                return (T)new Character(value.trim().charAt(0));
            }
            else if (type.isAssignableFrom(Byte.class) || type.isAssignableFrom(byte.class)) {
                return (T)Byte.valueOf(value);
            }
            else if (type.isAssignableFrom(Short.class) || type.isAssignableFrom(short.class)) {
                return (T)Short.valueOf(value);
            }
            else if (type.isAssignableFrom(Integer.class) || type.isAssignableFrom(int.class)) {
                return (T)Integer.valueOf(value);
            }
            else if (type.isAssignableFrom(Long.class) || type.isAssignableFrom(long.class)) {
                return (T)Long.valueOf(value);
            }
            else if (type.isAssignableFrom(Float.class) || type.isAssignableFrom(float.class)) {
                return (T)Float.valueOf(value);
            }
            else if (type.isAssignableFrom(Double.class) || type.isAssignableFrom(double.class)) {
                return (T)Double.valueOf(value);
            }
            else if (type.isAssignableFrom(Boolean.class) || type.isAssignableFrom(boolean.class)) {
                return (T)Boolean.valueOf(value);
            }

            return null;
        }
    }

    /**
     * A REST error object supporting http status code.
     */
    protected static class JSONRESTError extends APSRuntimeException implements HTTPError {
        //
        // Private Members
        //

        private int httpStatusCode = -1;

        private ErrorType errorType = null;

        private String optionalData = null;

        //
        // Constructors
        //

        /**
         * Creates a new JSONRESTError instance.
         *
         * @param errorType         The type of error.
         * @param httpStatusCode    The http status code.
         * @param message           The error message.
         * @param optionalData      Any optional data.
         * @param cause             The cause of the error.
         */
        public JSONRESTError(ErrorType errorType, int httpStatusCode, String message, String optionalData, Throwable cause) {
            super(message, cause);
            this.errorType = errorType;
            this.httpStatusCode = httpStatusCode;
            this.optionalData = optionalData;

            if (httpStatusCode >= 400 && httpStatusCode <= 499) {
                this.errorType = ErrorType.INVALID_REQUEST;
            }
            else if (httpStatusCode >= 500 && httpStatusCode <= 599) {
                this.errorType = ErrorType.SERVER_ERROR;
            }
        }

        //
        // Methods
        //

        /**
         * @return Returns an http status code.
         */
        @Override
        public int getHttpStatusCode() {
            return this.httpStatusCode;
        }

        /**
         * The type of the error.
         */
        @Override
        public ErrorType getErrorType() {
            return this.errorType;
        }

        /**
         * A potential error code.
         */
        @Override
        public String getErrorCode() {
            return "" + this.httpStatusCode;
        }

        /**
         * True if there is optional data available. An example of optional data would be a stack trace for example.
         */
        @Override
        public boolean hasOptionalData() {
            return getCause() != null && getCause() != this;
        }

        /**
         * The optional data.
         */
        @Override
        public String getOptionalData() {
            StringBuilder sb = new StringBuilder();

            if (this.optionalData != null && getCause() != this) {
                sb.append(this.optionalData);
                if (getCause() != null) {
                    sb.append("\n----\n");
                }
            }

            if (getCause() != null && getCause() != this) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                getCause().printStackTrace(pw);
                pw.close();
                sb.append(sw.toString());
            }

            return sb.toString();
        }
    }
}
