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
 *         2013-03-24: Created!
 *
 */
package se.natusoft.osgi.aps.jsonrpc.protocols;

import se.natusoft.osgi.aps.api.misc.json.JSONEOFException;
import se.natusoft.osgi.aps.api.misc.json.model.JSONArray;
import se.natusoft.osgi.aps.api.misc.json.model.JSONValue;
import se.natusoft.osgi.aps.api.misc.json.service.APSJSONExtendedService;
import se.natusoft.osgi.aps.api.net.rpc.errors.ErrorType;
import se.natusoft.osgi.aps.api.net.rpc.model.RPCRequest;
import se.natusoft.osgi.aps.api.net.rpc.model.RequestIntention;
import se.natusoft.osgi.aps.jsonrpc.errors.JSONRPCError;
import se.natusoft.osgi.aps.tools.APSLogger;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;


/**
 * Provides a HTTP REST protocol.
 */
public class JSONREST  extends JSONHTTP {

    //
    // Constructors
    //

    /**
     * Creates a new JSONREST instance.
     *
     * @param logger A logger to log to.
     * @param jsonService An APSServiceTracker wrapping of the APSJSONService that will automatically handle the getting
     *                    and releasing of the service upon calls.
     */
    public JSONREST(APSLogger logger, APSJSONExtendedService jsonService) {
        super(logger, jsonService);
    }

    //
    // Methods
    //

    /**
     * @return The name of the provided protocol.
     */
    @Override
    public String getServiceProtocolName() {
        return "JSONREST";
    }

    /**
     * @return The version of the implemented protocol.
     */
    @Override
    public String getServiceProtocolVersion() {
        return "1.0";
    }

    /**
     * @return A short description of the provided service. This should be in plain text.
     */
    @Override
    public String getRPCProtocolDescription() {
        return "This provides an HTTP REST protocol that talks JSON. Requests should specify service to call in " +
                "URL path and method parameters as either HTTP URL parameters or within a JSON array on the stream. " +
                "URL parameters are required on GET while a JSON array on the stream is required on POST or PUT. " +
                "Whatever the method call returns are converted to JSON and written on the response OutputStream. " +
                "For this REST protocol the called service must have methods annotated with se.natusoft.osgi.aps.api." +
                "net.rpc.annotations.APSWebService(httpMethod=GET/PUT/POST/DELETE). If the call does not match the " +
                "appropriate action for the method then an error in in the 400 range is returned.";
    }

    /**
     * Validates the call.
     *
     * @param serviceClass The class of the service to validate.
     * @param method The method of the service to call.
     * @param requestIntention The type of operation for the call.
     */
    private void validateCall(Class serviceClass, String method, RequestIntention requestIntention) {

        // If called method starts with get/put/post/delete then the HTTP method must match.
        // If not, we allow anything.
        for (Method m : serviceClass.getMethods()) {
            if (m.getName().equals(method)) {

                String compare = method.toLowerCase();

                if (compare.startsWith("get") && requestIntention != RequestIntention.READ) {
                    throw new JSONHTTPError(ErrorType.INVALID_REQUEST, SC_BAD_REQUEST,
                            "Bad request! HTTP GET method must be used for this call!", null, null);
                }

                if ((compare.startsWith("post") || compare.startsWith("put")) && requestIntention != RequestIntention.UPDATE &&
                        requestIntention != RequestIntention.CREATE) {
                    throw new JSONHTTPError(ErrorType.INVALID_REQUEST, SC_BAD_REQUEST,
                            "Bad request! HTTP method POST or PUT must be used for this call!", null, null);
                }

                if (compare.startsWith("delete") && requestIntention != RequestIntention.DELETE) {
                    throw new JSONHTTPError(ErrorType.INVALID_REQUEST, SC_BAD_REQUEST,
                            "Bad request! HTTP DELETE method must be used for this call!", null, null);
                }
            }
        }
    }

    /**
     * Finds a method that starts with one of the specified methods. If found the full method name is returned,
     * otherwise null is returned.
     *
     * @param serviceClass The service class to check.
     * @param methods The methods to look for.
     *
     * @return The found method or null if not found.
     */
    private String findMethod(Class serviceClass, String... methods) {
        for (Method m : serviceClass.getMethods()) {
            for (String method : methods) {
                if (m.getName().startsWith(method)) {
                    return m.getName();
                }
            }
        }

        return null;
    }

    /**
     * Checks and possibly modifies the method. A blank, or '-', or '@' method will result in a 'create', 'read', 'update', or
     * 'delete' method based on the request intention.
     *
     * @param serviceClass The service class to inspect.
     * @param method The method to check.
     * @param requestIntention The intention of the request.
     *
     * @return A possibly updated method.
     *
     * @throws IOException on any failure.
     */
    private String checkMethod(Class serviceClass, String method, RequestIntention requestIntention) throws IOException {
        if (method == null || method.equals("-") || method.equals("@") || method.trim().length() == 0) {
            String findMethod = null;
            switch (requestIntention) {
                case CREATE:
                    findMethod = findMethod(serviceClass, "create", "post", "new");
                    if (findMethod != null) {
                        method = findMethod;
                    }
                    break;
                case READ:
                    findMethod = findMethod(serviceClass, "read", "get");
                    if (findMethod != null) {
                        method = findMethod;
                    }
                    break;
                case UPDATE:
                    findMethod = findMethod(serviceClass, "update", "put", "set", "write");
                    if (findMethod != null) {
                        method = findMethod;
                    }
                    break;
                case DELETE:
                    findMethod = findMethod(serviceClass, "delete", "remove");
                    if (findMethod != null) {
                        method = findMethod;
                    }
                case UNKNOWN:
                    throw new JSONHTTPError(ErrorType.METHOD_NOT_FOUND, SC_BAD_REQUEST, "Failed to map HTTP method to a service method!",
                            null, null);
            }
        }

        return method;
    }

    /**
     * Parses a request from the provided InputStream and returns 1 or more RPCRequest objects.
     *
     * @param serviceQName  A fully qualified name to the service to call. This can be null if service name is provided on the stream.
     * @param serviceClass The class of the service to call.
     * @param method        This is ignored! Method is resolved by service annotations and RequestIntention.
     * @param requestStream The stream to parse request from.
     * @param requestIntention The intention of the request (CRUD)
     *
     * @return The parsed requests.
     * @throws java.io.IOException on IO failure.
     */
    @SuppressWarnings("Duplicates")
    @Override
    public List<RPCRequest> parseRequests(String serviceQName, Class serviceClass, String method, InputStream requestStream,
                                          RequestIntention requestIntention) throws IOException {

        validateCall(serviceClass, method, requestIntention);

        method = checkMethod(serviceClass, method, requestIntention);

        List<RPCRequest> requests = new LinkedList<>();

        while (true) {
            try {
                // Read the JSON request
                ReqJSONRESTErrorHandler errorHandler = new ReqJSONRESTErrorHandler();
                JSONArray jsonReq;
                JSONValue jsonReqValue;
                try {
                    jsonReqValue = super.jsonService.readJSON(requestStream, errorHandler);
                    jsonReq = (JSONArray)jsonReqValue;
                }
                catch (JSONEOFException eofe) {
                    break; // <-- Breaks out of loop!
                }
                catch (JSONParseException jpe) {
                    throw new JSONHTTPError(ErrorType.PARSE_ERROR, SC_BAD_REQUEST, "Bad JSON passed!", null, jpe);
                }
                catch (ClassCastException cce) {
                    throw new JSONHTTPError(ErrorType.INVALID_PARAMS, SC_BAD_REQUEST, "An array of JSON values/objects are required!",
                            null, cce);
                }

                JSONRPCRequest req = new JSONRPCRequest(serviceQName, method, jsonReq.getAsList(), null);
                requests.add(req);
            }
            catch (JSONHTTPError error) {
                // This is a way to provide invalid requests so that a transport can choose to execute at least the valid ones
                // and then report the invalid ones.
                JSONRPCRequest req = new JSONRPCRequest(serviceQName, method, error);
                requests.add(req);
                break; // <-- Breaks out of loop!
            }
        }

        return requests;
    }

    /**
     * Provides an RPCRequest based on in-parameters. This variant supports HTTP transports.
     *
     * @param serviceQName A fully qualified name to the service to call. This can be null if service name is provided on the stream.
     * @param serviceClass The class of the service to call.
     * @param method       this is ignored! Method is resolved by service annotations and RequestIntention.
     * @param parameters   parameters passed as a
     * @param requestIntention The intention of the request (CRUD)
     *
     * @return The parsed requests.
     * @throws java.io.IOException on IO failure.
     */
    @SuppressWarnings("Duplicates")
    @Override
    public RPCRequest parseRequest(String serviceQName, Class serviceClass, String method, Map<String, String> parameters,
                                   RequestIntention requestIntention) throws IOException {

        validateCall(serviceClass, method, requestIntention);

        method = checkMethod(serviceClass, method, requestIntention);

        RPCRequest request;

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
                    throw new JSONHTTPError(ErrorType.INVALID_PARAMS, SC_BAD_REQUEST, "parameter 'params' where not provided but the " +
                            "following were: " + sb.toString() + "!", null, null);
                }
            }

            request = new JSONRPCRequest(serviceQName, method, null, params);
        }
        catch (JSONHTTPError error) {
            request = new JSONRPCRequest(serviceQName, method, error);
        }

        return request;
    }

}
