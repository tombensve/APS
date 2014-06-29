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
 *         2013-03-24: Created!
 *         
 */
package se.natusoft.osgi.aps.jsonrpc.protocols;

import se.natusoft.osgi.aps.api.misc.json.JSONEOFException;
import se.natusoft.osgi.aps.api.misc.json.model.JSONArray;
import se.natusoft.osgi.aps.api.misc.json.model.JSONValue;
import se.natusoft.osgi.aps.api.misc.json.service.APSJSONExtendedService;
import se.natusoft.osgi.aps.api.net.rpc.annotations.APSRemoteService;
import se.natusoft.osgi.aps.api.net.rpc.errors.ErrorType;
import se.natusoft.osgi.aps.api.net.rpc.model.RPCRequest;
import se.natusoft.osgi.aps.api.net.rpc.model.RequestIntention;
import se.natusoft.osgi.aps.tools.APSLogger;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;


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
                "net.rpc.annotations.APSRemoteService(httpMethod=GET/PUT/POST/DELETE). If an appropriate method is not found a 404 " +
                "will be returned!";
    }

    /**
     * Resolves which method to call for different REST operations.
     *
     * @param serviceClass The service class whose methods to scan.
     *
     * @return A Map of found methods.
     */
    private Map<RequestIntention, String> resolveMethods(Class serviceClass) {
        Map<RequestIntention, String> methods = new HashMap<>();

        for (Method method : serviceClass.getMethods()) {
            APSRemoteService apsRemoteService = method.getAnnotation(APSRemoteService.class);

            if (apsRemoteService != null) {
                switch(apsRemoteService.httpMethod()) {
                    case GET:
                        methods.put(RequestIntention.READ, method.getName());
                        break;

                    case PUT:
                        methods.put(RequestIntention.UPDATE, method.getName());
                        break;

                    case POST:
                        methods.put(RequestIntention.CREATE, method.getName());
                        break;

                    case DELETE:
                        methods.put(RequestIntention.DELETE, method.getName());

                }
            }
        }

        return methods;
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
    @Override
    public List<RPCRequest> parseRequests(String serviceQName, Class serviceClass, String method, InputStream requestStream,
                                          RequestIntention requestIntention) throws IOException {
        Map<RequestIntention, String> methods = resolveMethods(serviceClass);

        List<RPCRequest> requests = new LinkedList<>();

        while (true) {
            try {
                // Read the JSON request
                ReqJSONRESTErrorHandler errorHandler = new ReqJSONRESTErrorHandler();
                JSONArray jsonReq = null;
                JSONValue jsonReqValue = null;
                try {
                    jsonReqValue = super.jsonService.readJSON(requestStream, errorHandler);
                    jsonReq = (JSONArray)jsonReqValue;
                }
                catch (JSONEOFException eofe) {
                    break;
                }
                catch (JSONParseException jpe) {
                    throw new JSONRESTError(ErrorType.PARSE_ERROR, SC_BAD_REQUEST, "Bad JSON passed!", null, jpe);
                }
                catch (ClassCastException cce) {
                    throw new JSONRESTError(ErrorType.INVALID_PARAMS, SC_BAD_REQUEST, "An array of JSON values/objects are required! The following " +
                            "'" + jsonReqValue + "' was received!", null, cce);
                }

                method = methods.get(requestIntention);
                if (method == null) {
                    throw new JSONRESTError(ErrorType.METHOD_NOT_FOUND, SC_NOT_FOUND,
                            "No REST method found matching request verb!", null, null);
                }

                JSONRESTRequest req = new JSONRESTRequest(serviceQName, method, jsonReq.getAsList(), null);
                requests.add(req);
            }
            catch (JSONRESTError error) {
                // This is a way to provide invalid requests so that a transport can choose to execute at least the valid ones
                // and then report the invalid ones.
                JSONRESTRequest req = new JSONRESTRequest(serviceQName, method, error);
                requests.add(req);
                break;
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
    @Override
    public RPCRequest parseRequest(String serviceQName, Class serviceClass, String method, Map<String, String> parameters,
                                   RequestIntention requestIntention) throws IOException {
        Map<RequestIntention, String> methods = resolveMethods(serviceClass);

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

            method = methods.get(requestIntention);
            if (method == null) {
                throw new JSONRESTError(ErrorType.METHOD_NOT_FOUND, SC_NOT_FOUND,
                        "No REST method found matching request verb!", null, null);
            }

            request = new JSONRESTRequest(serviceQName, method, null, params);
        }
        catch (JSONRESTError error) {
            request = new JSONRESTRequest(serviceQName, method, error);
        }

        return request;
    }

}
