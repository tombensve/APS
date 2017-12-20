/*
 *
 * PROJECT
 *     Name
 *         APS External Protocol HTTP Transport Provider
 *
 *     Code Version
 *         1.0.0
 *
 *     Description
 *         This uses aps-external-protocol-extender to provide remote calls over HTTP. It makes
 *         any published service implementing se.natusoft.osgi.aps.net.rpc.streamed.service.StreamedRPCProtocolService
 *         available for calling services over HTTP.
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
 *         2012-01-06: Created!
 *
 */
package se.natusoft.osgi.aps.rpchttpextender.servlet;

import org.apache.commons.codec.binary.Base64;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import se.natusoft.osgi.aps.api.external.extprotocolsvc.APSExternalProtocolService;
import se.natusoft.osgi.aps.api.external.extprotocolsvc.model.APSExternalProtocolListener;
import se.natusoft.osgi.aps.api.external.extprotocolsvc.model.APSExternallyCallable;
import se.natusoft.osgi.aps.api.external.model.type.DataType;
import se.natusoft.osgi.aps.api.external.model.type.DataTypeDescription;
import se.natusoft.osgi.aps.api.external.model.type.ParameterDataTypeDescription;
import se.natusoft.osgi.aps.api.misc.json.model.JSONValue;
import se.natusoft.osgi.aps.api.misc.json.service.APSJSONExtendedService;
import se.natusoft.osgi.aps.api.net.discovery.ServiceDescription;
import se.natusoft.osgi.aps.api.net.discovery.service.APSSimpleDiscoveryService;
import se.natusoft.osgi.aps.api.net.rpc.errors.ErrorType;
import se.natusoft.osgi.aps.api.net.rpc.errors.HTTPError;
import se.natusoft.osgi.aps.api.net.rpc.errors.RPCError;
import se.natusoft.osgi.aps.api.net.rpc.model.RPCExceptionConverter;
import se.natusoft.osgi.aps.api.net.rpc.model.RPCRequest;
import se.natusoft.osgi.aps.api.net.rpc.model.RequestIntention;
import se.natusoft.osgi.aps.api.net.rpc.service.StreamedRPCProtocol;
import se.natusoft.osgi.aps.exceptions.APSException;
import se.natusoft.osgi.aps.rpchttpextender.config.RPCServletConfig;
import se.natusoft.osgi.aps.tools.APSLogger;
import se.natusoft.osgi.aps.tools.APSServiceTracker;
import se.natusoft.osgi.aps.tools.exceptions.APSNoServiceAvailableException;
import se.natusoft.osgi.aps.tools.tracker.OnServiceAvailable;
import se.natusoft.osgi.aps.tools.tracker.WithService;
import se.natusoft.osgi.aps.tools.web.APSAdminWebLoginHandler;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.*;

/**
 * The servlet we make the RPC http transport available on.
 * <p/>
 * The http transport will be available on "http://host:port/apsrpc/protocolname/protocolversion/protocoldata/[service]/..." on http put.
 * <p/>
 * If you bring up "http://host:port/apsrpc/" in a browser you will get information about available protocols and services.
 */
@SuppressWarnings("WeakerAccess")
public class RPCServlet extends HttpServlet implements APSExternalProtocolListener, OnServiceAvailable<APSSimpleDiscoveryService> {
    //
    // Cosntants
    //

    private static final String BG_COLOR = "bgcolor=\"#f5f5f5\"";

    private static final int AUTH_FAILED = -1;

    //
    // Private Members
    //

    /**
     * The logger to log to.
     */
    private APSLogger logger = null;

    /**
     * This is used to access the externally available services.
     */
    private APSServiceTracker<APSExternalProtocolService> externalProtocolServiceTracker = null;

    /**
     * The tracked service.
     */
    private APSExternalProtocolService externalProtocolService = null;

    /**
     * Used for displaying result of text executions of no args methods on method display.
     */
    private APSServiceTracker<APSJSONExtendedService> jsonServiceTracker = null;

    /**
     * The tracked service.
     */
    private APSJSONExtendedService jsonService = null;

    /**
     * Used for registering services that are remotely available.
     */
    private APSServiceTracker<APSSimpleDiscoveryService> discoveryServiceTracker = null;

    /**
     * The context of the bundle we belong to.
     */
    private BundleContext bundleContext = null;

    /**
     * The host name of the server we are being served on.
     */
    private String serverHost = null;

    /**
     * The port of the server we are being served on.
     */
    private int serverPort = 0;

    /**
     * The base url for RPC calls.
     */
    private String rpcBaseUrl = null;

    /**
     * The admin web login handler.
     */
    private APSAdminWebLoginHandler loginHandler = null;

    //
    // Constructors
    //

    /**
     * Creates a new JSONRPCServlet instance.
     */
    public RPCServlet() {
    }

    //
    // Setup and shutdown.
    //

    /**
     * First time setup.
     *
     * @param servletConfig The configuration for the servlet.
     * @throws ServletException on failure.
     */
    @Override
    public void init(javax.servlet.ServletConfig servletConfig) throws javax.servlet.ServletException {
        servletConfig.getServletContext().getServerInfo();
        if (this.bundleContext == null) {
            this.bundleContext = (BundleContext) servletConfig.getServletContext().getAttribute("osgi-bundlecontext");

            if (this.bundleContext == null) {
                throw new ServletException("BundleContext not found! This war must be deployed in an OSGi compatible web container!");
            }

            this.loginHandler = new APSAdminWebLoginHandler(this.bundleContext);

            try {
                this.logger = new APSLogger(System.out);
                this.logger.setLoggingFor("aps-rpc-http-transport-provider");
                this.logger.start(this.bundleContext);

                this.externalProtocolServiceTracker =
                        new APSServiceTracker<>(this.bundleContext, APSExternalProtocolService.class,
                                APSServiceTracker.SHORT_TIMEOUT);
                this.externalProtocolServiceTracker.start();
                this.externalProtocolService = this.externalProtocolServiceTracker.getWrappedService();
                this.externalProtocolService.addExternalProtocolListener(this);

                this.jsonServiceTracker =
                        new APSServiceTracker<>(this.bundleContext, APSJSONExtendedService.class,
                                APSServiceTracker.SHORT_TIMEOUT);
                this.jsonServiceTracker.start();
                this.jsonService = this.jsonServiceTracker.getWrappedService();

                this.discoveryServiceTracker =
                        new APSServiceTracker<>(this.bundleContext, APSSimpleDiscoveryService.class,
                                APSServiceTracker.SHORT_TIMEOUT);
                this.discoveryServiceTracker.start();
                this.discoveryServiceTracker.onServiceAvailable(this);
            } catch (APSNoServiceAvailableException nsae) {
                throw new ServletException(nsae.getMessage(), nsae);
            }
        }
    }

    /**
     * This means the servlet is going away and thus we cleanup.
     */
    @Override
    public void destroy() {
        if (this.bundleContext != null) {

            if (this.externalProtocolServiceTracker != null) {
                this.externalProtocolService.removeExternalProtocolListener(this);
                this.externalProtocolServiceTracker.stop(this.bundleContext);
                this.externalProtocolServiceTracker = null;
                this.externalProtocolService = null;
            }

            if (this.jsonServiceTracker != null) {
                this.jsonServiceTracker.stop(this.bundleContext);
                this.jsonServiceTracker = null;
                this.jsonService = null;
            }

            if (this.discoveryServiceTracker != null) {
                this.discoveryServiceTracker.onServiceAvailable(null);
                this.discoveryServiceTracker.onServiceLeaving(null);
                this.discoveryServiceTracker.stop(this.bundleContext);
                this.discoveryServiceTracker = null;
            }

            this.loginHandler.shutdown();

            if (this.logger != null) {
                this.logger.stop(this.bundleContext);
            }
            this.bundleContext = null;
        }
    }

    //
    // RPC call handling
    //

    /**
     * Handles a get/put/post request.
     * <p/>
     * The format of requests are:
     * @code {http://host:port/apsrpc/<i>protocol</i>/<i>version</i>[/<i>service</i>][/<i>method</i>]}
     * <p/>
     * <i>protocol</i> - This is the name of the protocol to use. For example JSONRPC. For this to work there must be
     * a registered service available that implements StreamedRPCProtocolService and whose getServiceProtocolName()
     * method matches the specified protocol name.
     * <p/>
     * <i>version</i> - This is the version of the named protocol to use. It is possible for more than one version of
     * a protocol to be available at the same time. Again, for this to work there must be a registered service available
     * that implements StreamedRPCProtocolService whose getServiceProtocolVersion() matches the specified version in
     * addition to matching the protocol name.
     * <p/>
     * <i>service</i> - Depending on the RPC protocol implementation a service is specified on the URL or in the data
     * on the PUT stream. This case is when it is specified on the URL. The service always have to be a fully qualified
     * name to the service.
     * <p/>
     * <i>method</i> - Depending on the RPC protocol implementation a method can be specified on the URL. When specified
     * on the URL that will override any method passed in the request.
     * <p/>
     * The rest of the request data is read from the input stream, which should be passed on to the matching
     * StreamedRPCProtocolService.
     *
     * @param req  The request
     * @param resp The response
     * @throws ServletException    on servlet related failures.
     * @throws java.io.IOException on IO failure.
     */
    protected void doReq(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.loginHandler.setSessionIdFromRequestCookie(req);

        String pathInfo = req.getPathInfo();
        if (pathInfo.startsWith("/")) {
            pathInfo = pathInfo.substring(1);
        }
        if (pathInfo.startsWith("_help")) {
            if (RPCServletConfig.mc.get().enableHelpWeb.getBoolean()) {
                // If the first page help does not end in '/' then we want to redirect so that it does. Otherwise the
                // relative links generated for services will be incorrect.
                if (pathInfo.equals("_help")) {
                    resp.sendRedirect(pathInfo + "/");
                }
                else {
                    if (this.loginHandler.hasValidLogin()) {
                        doHelp(req, resp);
                    }
                    else {

                        String auth = req.getHeader("Authorization");
                        if (auth != null) {
                            if (auth.startsWith("Basic")) {
                                String encoded = auth.substring(6);
                                byte[] userPwBytes = Base64.decodeBase64(encoded.getBytes());
                                String[] userPw = new String(userPwBytes).split(":");
                                if (userPw.length != 2) {
                                    resp.setHeader("WWW-Authenticate", "Basic");
                                    resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Bad authorization!");
                                    return;
                                }

                                String user = userPw[0];
                                String password = userPw[1];
                                if (this.loginHandler.login(user, password)) {
                                    doHelp(req, resp);
                                }
                                else {
                                    resp.addHeader("WWW-Authenticate", "Basic");
                                    resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Bad authorization!");
                                }
                            }
                        }
                        else {
                            resp.addHeader("WWW-Authenticate", "Basic");
                            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication is required! Please login.");
                        }
                    }
                }
            }
            else {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "The help web has been disabled!");
            }
        }
        else {
            doService(req, resp);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doReq(req, resp);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doReq(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doReq(req, resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doReq(req, resp);
    }

    /**
     * Copies the request parameters providing only the first value of multivalues since we currently only support one value per name.
     *
     * @param req The http request to get parameters from.
     */
    private Map<String, String> getParameters(HttpServletRequest req) {
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> reqParams = req.getParameterMap();

        for (String name : reqParams.keySet()) {
            String[] value = reqParams.get(name);
            String rvalue = "";
            if (value != null) {
                rvalue = value[0];
            }
            params.put(name, rvalue);
        }

        return params;
    }

    /**
     * Handles a service call.
     *
     * @param req The servlet request.
     * @param resp The servlet response.
     * @throws ServletException
     * @throws IOException
     */
    @SuppressWarnings("unchecked,UnusedAssignment")
    private void doService(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String pathInfo = req.getPathInfo();
        if (pathInfo.startsWith("/")) {
            pathInfo = pathInfo.substring(1);
        }
        String[] pathParts = pathInfo.split("/");

        if (pathParts.length < 2) {
            String urlstart = "http://" + req.getServerName() + ":" + req.getServerPort();
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Too short path in URL! The URL should look like this: " +
                    "'" + urlstart + "/apsrpc/[auth:<user>:<password>/]<protocol>/<version>[/<service>]', or " +
                    "'" + urlstart + "/apsrpc/_help/' to get a bit of help as HTML.");
            return;
        }

        String version = null;
        String service = null;
        String method = null;
        String protocolName = null;
        int part = 0;

        if (RPCServletConfig.mc.get().requireAuthentication.getBoolean()) {
            part = checkAuth(pathParts, part, req, resp);
            if (part == AUTH_FAILED) {
                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        protocolName = pathParts[part++];
        version = pathParts[part++];

        StringBuilder svcPath = new StringBuilder();

        if (pathParts.length > part) {
            service = pathParts[part++];
            svcPath.append(service);
        }
        if (pathParts.length > part) {
            method = pathParts[part++];
            svcPath.append('/');
            svcPath.append(method);
        }
        while (pathParts.length > part) {
            svcPath.append('/');
            svcPath.append(pathParts[part++]);
        }

        List<APSExternallyCallable> serviceCallables = this.externalProtocolService.getCallables(service);
        if (serviceCallables == null || serviceCallables.isEmpty()) {
            resp.sendError(404, "Service '" + service + "' ('" + svcPath.toString() + "') was not found!!!");
            return;
        }

        Class serviceClass = serviceCallables.get(0).getServiceClass();

        StreamedRPCProtocol protocol = this.externalProtocolService.getStreamedProtocolByNameAndVersion(protocolName, version);
        if (protocol != null) {

            String reqMethod = req.getMethod().toUpperCase();
            GetReqParams getReqParams = new GetReqParams();
            getReqParams.method = method;
            getReqParams.protocol = protocol;
            getReqParams.req = req;
            getReqParams.reqMethod = reqMethod;
            getReqParams.service = service;
            getReqParams.serviceClass = serviceClass;
            List<RPCRequest> requests = getRequests(getReqParams);

            HandleRequestsParams handleRequestsParams = new HandleRequestsParams();
            handleRequestsParams.method = method;
            handleRequestsParams.protocol = protocol;
            handleRequestsParams.requests = requests;
            handleRequestsParams.service = service;
            handleRequestsParams.resp = resp;
            handleRequests(handleRequestsParams);

        }
        else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No RPC protocol provider found for protocol '" + protocolName +
                    "' with version '" + version + "'!");
        }
    }

    private static class GetReqParams {
        public String reqMethod;
        public StreamedRPCProtocol protocol;
        public String service;
        public Class serviceClass;
        public String method;
        public HttpServletRequest req;
    }

    private List<RPCRequest> getRequests(GetReqParams params) throws IOException {
        List<RPCRequest> requests;

        switch (params.reqMethod) {
            case "GET":
                requests = new LinkedList<>();
                requests.add(params.protocol.parseRequest(params.service, params.serviceClass, params.method, getParameters(params.req), RequestIntention.READ));
                break;
            case "PUT":
                requests = params.protocol.parseRequests(params.service, params.serviceClass, params.method, params.req.getInputStream(), RequestIntention.UPDATE);
                break;
            case "POST":
                requests = params.protocol.parseRequests(params.service, params.serviceClass, params.method, params.req.getInputStream(), RequestIntention.CREATE);
                break;
            case "DELETE":
                requests = new LinkedList<>();
                requests.add(params.protocol.parseRequest(params.service, params.serviceClass, params.method, getParameters(params.req), RequestIntention.DELETE));
                break;
            default:
                // We fallback on READ if we get something unexpected here!
                requests = new LinkedList<>();
                requests.add(params.protocol.parseRequest(params.service, params.serviceClass, params.method, getParameters(params.req), RequestIntention.READ));
                break;
        }

        return requests;
    }

    private static class HandleRequestsParams {
        public String method;
        public StreamedRPCProtocol protocol;
        public String service;
        public List<RPCRequest> requests;
        public HttpServletResponse resp;
    }

    private void handleRequests(HandleRequestsParams hrparams) throws IOException {
        // It is possible to send multiple requests on the stream!
        for (RPCRequest rpcRequest : hrparams.requests) {

            try {
                if (rpcRequest.isValid()) {
                    if (hrparams.method == null || hrparams.method.equals("@") || hrparams.method.equals("-")) {
                        hrparams.method = rpcRequest.getMethod();
                    }

                    @SuppressWarnings("unchecked")
                    APSExternallyCallable<Object> callable = this.externalProtocolService.getCallable(hrparams.service, hrparams.method);

                    if (callable != null) {
                        // Handle parrameters
                        List<Object> params = new LinkedList<>();
                        int param = 0;
                        for (ParameterDataTypeDescription paramDesc : callable.getParameterDataDescriptions()) {
                            Class paramClass = Void.class;
                            try {
                                // Since we don't have a dependency to any of the services we will be calling, our bundle
                                // will not have the correct classpath required for creating arguments to the service.
                                // Therefore we need to let the bundle of the service we are about to call load argument
                                // classes for us.
                                if (paramDesc.getObjectQName() != null) {
                                    paramClass = callable.getServiceBundle().loadClass(paramDesc.getObjectQName());
                                }
                            } catch (ClassNotFoundException cnfe) {
                                throw new RPCErrorException(
                                        hrparams.protocol.createRPCError(
                                                ErrorType.SERVICE_NOT_FOUND,
                                                cnfe.getMessage(),
                                                null,
                                                cnfe
                                        )
                                );
                            }

                            params.add(rpcRequest.getIndexedParameter(param++, paramClass));
                        }

                        Object[] paramsArray = new Object[params.size()];
                        paramsArray = params.toArray(paramsArray);

                        // Call service
                        Object result;
                        try {
                            result = callable.call(paramsArray);
                        } catch (APSNoServiceAvailableException nsae) {
                            throw new RPCErrorException(
                                    hrparams.protocol.createRPCError(
                                            ErrorType.SERVICE_NOT_FOUND,
                                            "Service '" + hrparams.service + "' is not available!",
                                            null,
                                            nsae
                                    )
                            );
                        } catch (Exception e) {
                            throw new RPCErrorException(
                                    hrparams.protocol.createRPCError(
                                            ErrorType.SERVER_ERROR,
                                            e.getMessage(),
                                            null,
                                            e
                                    )
                            );
                        }

                        // Write the normal OK response.
                        hrparams.protocol.writeResponse(result, rpcRequest, hrparams.resp.getOutputStream());

                    } else {
                        throw new RPCErrorException(
                                hrparams.protocol.createRPCError(
                                        ErrorType.METHOD_NOT_FOUND,
                                        "Method '" + hrparams.method + "' is not available!",
                                        null,
                                        null
                                )
                        );
                    }
                } else {
                    this.logger.error(rpcRequest.getServiceQName() + ":" + rpcRequest.getMethod() + " - " +
                            rpcRequest.getError().getErrorType().name() + ":" + rpcRequest.getError().getMessage());
                }
            }
            // Write error responses.
            catch (RPCErrorException ree) {
                if (ree.getError() instanceof HTTPError ) {
                    hrparams.resp.sendError(((HTTPError) ree.getError()).getHttpStatusCode(), ree.getError().getMessage());
                } else {
                    hrparams.protocol.writeErrorResponse(ree.getError(), rpcRequest, hrparams.resp.getOutputStream());
                }
            } catch (Exception e) {
                RPCError error;
                RPCExceptionConverter exceptionConverter = rpcRequest.getExceptionConverter();
                if (exceptionConverter != null) {
                    error = exceptionConverter.convertException(e);
                }
                else {
                    error = hrparams.protocol.createRPCError(
                            ErrorType.SERVER_ERROR,
                            e.getMessage(),
                            null,
                            e
                    );
                }
                if (error instanceof HTTPError) {
                    hrparams.resp.sendError(((HTTPError) error).getHttpStatusCode(), error.getMessage());
                } else {
                    hrparams.protocol.writeErrorResponse(
                            error,
                            rpcRequest,
                            hrparams.resp.getOutputStream()
                    );
                }
            }
        }
    }

    /**
     * Handles the authentication part of the request.
     *
     * @param pathParts The split parts of the path to potentially extract credentials from.
     * @param part      the current index in the pathParts array.
     * @param req       The http request.
     * @param resp      The http response.
     * @return The new current index in the pathParts array.
     * @throws IOException On failure to set header or error on response.
     */
    private int checkAuth(String[] pathParts, int part, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String user = null;
        String password = null;

        if (pathParts[0].startsWith("auth:")) {
            String auth = pathParts[part++];
            String[] authParts = auth.split(":");
            if (authParts.length != 3) {
                resp.setHeader("WWW-Authenticate", "Basic realm=\"aps\"");
                resp.sendError(401, "Bad authorisation!");
                return AUTH_FAILED;
            }

            user = authParts[1];
            password = authParts[2];
        }
        // Check for basic http auth as an alternative.
        if (user == null) {
            String auth = req.getHeader("Authorization");
            if (auth != null) {
                if (auth.startsWith("Basic")) {
                    String encoded = auth.substring(6);
                    byte[] userPwBytes = Base64.decodeBase64(encoded.getBytes());
                    String[] userPw = new String(userPwBytes).split(":");
                    if (userPw.length != 2) {
                        resp.setHeader("WWW-Authenticate", "Basic realm=\"aps\"");
                        resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Bad authorisation!");
                        return AUTH_FAILED;
                    }
                    user = userPw[0];
                    password = userPw[1];
                }
            }
        }

        if (user != null && password != null) {
            String role = null; // To make clear that the null value is a role since just passing null won't say shit about what is null!
            //noinspection ConstantConditions
            if (!this.loginHandler.login(user, password, role)) {
                resp.setHeader("WWW-Authenticate", "Basic realm=\"aps\"");
                resp.sendError(401, "Authorisation failed!");
                return AUTH_FAILED;
            }
        } else {
            if (RPCServletConfig.mc.get().requireAuthentication.getBoolean()) {
                resp.setHeader("WWW-Authenticate", "Basic realm=\"aps\"");
                resp.sendError(401, "Authorisation required!");
                return AUTH_FAILED;
            }
        }

        return part;
    }

    /**
     * This is an exception used internally to handle errors and write them to the response in the end.
     * This is never thrown outside of a method!
     */
    private static class RPCErrorException extends APSException {
        //
        // Private Members
        //

        /**
         * A passed along RPCError.
         */
        private RPCError error = null;

        //
        // Constructors
        //

        /**
         * Creates a new RPCErrorException.
         *
         * @param error The RPCError to pass along.
         */
        public RPCErrorException(RPCError error) {
            super("");
            this.error = error;
        }

        //
        // Methods
        //

        /**
         * @return The RPCError supplied in constructor.
         */
        public RPCError getError() {
            return this.error;
        }
    }

    //
    // APSDiscoveryService registration and removal.
    //

    /**
     * Catch our host and port information which as far as I can determine is only possible to get from a request.
     *
     * @param req Http servlet request.
     * @param resp Http servlet response.
     * @throws ServletException
     * @throws IOException
     */
    public void service(ServletRequest req, ServletResponse resp) throws ServletException, IOException {
        if (this.rpcBaseUrl == null) {
            String protocol = req.getProtocol().split("/")[0].toLowerCase();
            if (req.getServerName() != null) {
                this.serverHost = req.getServerName();
            } else if (req.getLocalName() != null) {
                this.serverHost = req.getLocalName();
            }
            if (this.serverHost.equals("localhost")) {
                this.serverHost = InetAddress.getLocalHost().getHostName();
            }
            this.serverPort = req.getServerPort();
            this.rpcBaseUrl = protocol + "://" + this.serverHost + ":" + this.serverPort + "/apsrpc/";

            try {
                onServiceAvailable(this.discoveryServiceTracker.allocateService(), null);
                this.discoveryServiceTracker.releaseService();
            } catch (Exception e) {/*OK*/}
        }
        super.service(req, resp);
    }

    /**
     * Creates a new Properties instance with properties describing the service to publish or unpublish.
     *
     * @param protocol The protocol of the service.
     * @param service The name of the service.
     * @param version The version of the service.
     */
    private Properties createExternalServiceDescription(StreamedRPCProtocol protocol, String service, String version) {
        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setDescription("Published by aps-rpc-http-transport-provider.");
        serviceDescription.setHost(this.serverHost);
        serviceDescription.setPort("" + this.serverPort);
        serviceDescription.setUrl(this.rpcBaseUrl + protocol.getServiceProtocolName() + "/" +
                protocol.getServiceProtocolVersion() + "/" + service);
        serviceDescription.setVersion(version);
        serviceDescription.setName(service);

        return serviceDescription;
    }

    /**
     * This gets called when a new externally available service becomes available.
     *
     * @param service The fully qualified name of the newly available service.
     */
    @Override
    public void externalServiceAvailable(String service, String version) {
        if (this.rpcBaseUrl != null) {
            for (StreamedRPCProtocol protocol : this.externalProtocolService.getAllStreamedProtocols()) {
                this.discoveryServiceTracker.withAllAvailableServices(new WithService<APSSimpleDiscoveryService>() {

                    @SuppressWarnings({"notused", "unused"})
                    public void withService(APSSimpleDiscoveryService discoverySvc, Properties serviceDescription) throws Exception {
                        discoverySvc.publishService(serviceDescription);//                              ^
                    }                   //                                                              |
                                        //                                                              |
                }, createExternalServiceDescription(protocol, service, version)); //--------------------+
            }
        }
    }

    /**
     * This gets called when an externally available service no longer is available.
     *
     * @param service The fully qualified name of the service leaving.
     */
    @Override
    public void externalServiceLeaving(String service, String version) {
        if (this.rpcBaseUrl != null) {
            for (StreamedRPCProtocol protocol : this.externalProtocolService.getAllStreamedProtocols()) {
                this.discoveryServiceTracker.withAllAvailableServices(new WithService<APSSimpleDiscoveryService>() {
                    @SuppressWarnings({"notused", "unused"})
                    public void withService(APSSimpleDiscoveryService discoverySvc, Properties serviceDescription) throws Exception {
                        discoverySvc.unpublishService(serviceDescription);
                    }

                }, createExternalServiceDescription(protocol, service, version));
            }
        }
    }

    /**
     * Creates a service description properties instance for each protocol and service.
     *
     * @param protocolName Then name of the protocol.
     * @param protocolVersion The version of the protocol.
     * @param service The service callable via the protocol.
     */
    private Properties createServiceDescriptionForProtocol(String protocolName, String protocolVersion, String service) {
        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setDescription("Published by aps-rpc-http-extender.");
        serviceDescription.setHost(this.serverHost);
        serviceDescription.setPort("" + this.serverPort);
        serviceDescription.setUrl(this.rpcBaseUrl + protocolName + "/" + protocolVersion +
                "/" + service);
        serviceDescription.setVersion(this.externalProtocolService.getCallables(service).get(0).getServiceBundle().getVersion().toString());
        serviceDescription.setName(service);

        return serviceDescription;
    }

    /**
     * This gets called when a new protocol becomes available.
     *
     * @param protocolName    The name of the protocol.
     * @param protocolVersion The version of the protocol.
     */
    @Override
    public void protocolAvailable(String protocolName, String protocolVersion) {
        for (String service : this.externalProtocolService.getAvailableServices()) {
            this.discoveryServiceTracker.withAllAvailableServices(new WithService<APSSimpleDiscoveryService>() {
                /**
                 * Receives a service to do something with.
                 *
                 * @param discoverySvc The received service.
                 *
                 * @throws Exception Implementation can throw any exception. How it is handled depends on the APSServiceTracker method this
                 *                   gets passed to.
                 */
                @SuppressWarnings("unused")
                public void withService(APSSimpleDiscoveryService discoverySvc, Properties serviceDescription) throws Exception {
                    discoverySvc.publishService(serviceDescription);
                }

            }, createServiceDescriptionForProtocol(protocolName, protocolVersion, service));
        }
    }

    /**
     * This gets called when a new protocol is leaving.
     *
     * @param protocolName    The name of the protocol.
     * @param protocolVersion The version of the protocol.
     */
    @Override
    public void protocolLeaving(String protocolName, String protocolVersion) {
        for (String service : this.externalProtocolService.getAvailableServices()) {
            this.discoveryServiceTracker.withAllAvailableServices(new WithService<APSSimpleDiscoveryService>() {
                @SuppressWarnings({"notused", "unused"})
                public void withService(APSSimpleDiscoveryService discoverySvc, Properties serviceDescription) throws Exception {
                    discoverySvc.unpublishService(serviceDescription);
                }

            }, createServiceDescriptionForProtocol(protocolName, protocolVersion, service));
        }
    }

    /**
     * This gets called whenever a new APSSimpleDiscoveryService becomes available. Thereby we handle restart of the discovery service
     * or new implementations of it, or the case where this bundle is up and running before any discovery service.
     *
     * @param discoverySvc     The received service.
     * @param serviceReference The reference to the received service.
     * @throws Exception Implementation can throw any exception. How it is handled depends on the APSServiceTracker method this
     *                   gets passed to.
     */
    @Override
    public void onServiceAvailable(APSSimpleDiscoveryService discoverySvc, ServiceReference serviceReference) throws Exception {
        if (this.rpcBaseUrl != null) {
            for (StreamedRPCProtocol protocol : this.externalProtocolService.getAllStreamedProtocols()) {
                for (String service : this.externalProtocolService.getAvailableServices()) {
                    discoverySvc.publishService(
                            createServiceDescriptionForProtocol(
                                protocol.getServiceProtocolName(),
                                protocol.getServiceProtocolVersion(),
                                service
                            )
                    );
                }
            }
        }
    }

    //
    // RPC service information handling on HTTP GET.
    //

    /**
     * Handles help information.
     * <p/>
     * Since all service calls are done using HTTP PUT we provide information about the service and available services and methods
     * on HTTP GET.
     *
     * @param req  The request
     * @param resp The response
     * @throws ServletException
     * @throws java.io.IOException
     */
    protected void doHelp(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String queryStr = req.getPathInfo();
        if (queryStr.startsWith("/")) {
            queryStr = queryStr.substring(1);
        }
        String[] query = queryStr.split("/");

        HTMLWriter html = new HTMLWriter(resp.getOutputStream());

        if (query.length == 1 || query[1].equals("")) {
            handleFirstPage(html, req);
        } else if (query.length == 2 && query[1].length() > 0) {
            String service = query[1];
            handleServicePage(html, service, req);
        } else if (query.length == 3 && query[1].length() > 0 && query[2].length() > 0) {
            String service = query[1];
            String method = query[2];
            handleMethodPage(html, service, method, req);
        } else {
            resp.sendError(401, "Invalid path!");
        }
    }

    /**
     * Handles the first page with general information inlcuding protocols and services.
     *
     * @param html The HTMLWriter to write to.
     * @param req  The HttpServletRequest.
     * @throws IOException
     */
    private void handleFirstPage(HTMLWriter html, HttpServletRequest req) throws IOException {
        html.tag("html");
        {
            html.tag("body", "", BG_COLOR);
            {
                html.tagc("h1", "ApplicationPlatformServices (APS) Remote service call over HTTP transport provider");
                html.tagc("p",
                        "This provides an http transport for simple remote requests to OSGi services that have the \"APS-Externalizable: " +
                                "true\" in their META-INF/MANIFEST.MF. This follows the OSGi extender pattern and makes any registered " +
                                "OSGi services of bundles having the above manifest entry available for remote calls over HTTP. This " +
                                "transport makes use of the aps-external-protocol-extender which exposes services with the above " +
                                "mentioned manifest entry with each service method available as an APSExternallyCallable." +
                                "The aps-ext-protocol-http-transport acts as a mediator between the protocol implementations and " +
                                "aps-external-protocol-extender for requests over HTTP."
                );
                html.tagc("p", "<b>Please note</b> that depending on protocol not every service method will be callable. It depends on " +
                        "its arguments and return value. It mostly depends on how well the protocol handles types and can convert " +
                        "between the caller and the service. Also note that bundles can specify \"APS-Externalizable: false\" in their " +
                        "META-INF/MANIFEST.MF. In that case none of the bundles services will be callable this way!"
                );
                html.tagc("p",
                        "This does not provide any protocol, only transport! For services " +
                                "to be able to be called at least one protocol is needed. Protocols are provided by providing an " +
                                "implementation of se.natusoft.osgi.aps.api.net.rpc.service.StreamedRPCProtocolService and registering " +
                                "it as an OSGi service. The StreamedRPCProtocolService API provides a protocol name and protocol " +
                                "version getter which is used to identify it. A call to an RPC service looks like this:"
                );
                html.text("<ul><code>http://host:port/apsrpc/<i>protocol</i>/<i>version</i>[/<i>service</i>][/<i>method</i>]</code></ul>");
                html.text(
                        "<ul>" +
                                "<i>protocol</i>" +
                                "<ul>" +
                                "This is the name of the protocol to use. An implementation of that protocol must of course be available " +
                                "for this to work. If it isn't you will get a 404 back!" +
                                "</ul>" +
                                "</ul>"
                );
                html.text(
                        "<ul>" +
                                "<i>version</i>" +
                                "<ul>" +
                                "This is the version of the protocol. If this doesn't match any protocols available you will also get a " +
                                "404 back." +
                                "</ul>" +
                                "</ul>"
                );
                html.text(
                        "<ul>" +
                                "<i>service</i>" +
                                "<ul>" +
                                "This is the service to call. Depending on the protocol you might not need this. But for protocols that " +
                                "only provide method in the stream data like JSONRPC for example, then this is needed. When provided it " +
                                "has to be a fully qualified service interface class name." +
                                "</ul>" +
                                "</ul>"
                );
                html.text(
                        "<ul>" +
                                "<i>method</i>" +
                                "<ul>" +
                                "This is a method of the service to call. The requirement for this also depends on the protocol. " +
                                "The JSONRPC protocols does not need this since they provide the method in the request. A REST " +
                                "protocol however would need this." +
                                "</ul>" +
                                "</ul>"
                );

                html.tagc("h2", "Security");
                html.tagc("p",
                        "This help page always require authentication. This is because it register itself with the APSAdminWeb and " +
                                "is available as a tab there and thereby joins in the admin web authentication. For service calls however " +
                                "authentication is only required if you enable it in the configuration (network/rpc-http-transport). " +
                                "There are 2 variants of authentication for services:" +
                                "<ul>" +
                                "<li>http://.../apsrpc/<b>auth:user:password</b>/protocol/...</li>" +
                                "<li>Basic HTTP authentication using header: 'Authorization: Basic {base 64 encoded user:password}'.</li>" +
                                "</ul>"
                );
                html.tagc("p",
                        "Note that this is only a transport (over http)! It has nothing to say about protocols which is why the " +
                                "above auth methods are outside of the protocol, only part of this transport. If you make services that you " +
                                "expose this way it is also possible to leave the authentication configold at false and provide authentication " +
                                "in your service by using the APSSimpleUserService or something else."
                );

                html.tagc("h2", "Found Protocols");

                for (StreamedRPCProtocol protocol : this.externalProtocolService.getAllStreamedProtocols()) {
                    html.tagc("h3", protocol.getServiceProtocolName() + " : " + protocol.getServiceProtocolVersion());
                    html.tagc("p", protocol.getRPCProtocolDescription());

                    html.tagc("p", "<b>Request URL:</b>&nbsp;http://" + req.getLocalName() + ":" + req.getLocalPort() + "/apsrpc/" +
                            protocol.getServiceProtocolName() + "/" + protocol.getServiceProtocolVersion() + "[/&lt;service&gt;][/&lt;method&gt;]");

                    String reqContentType = protocol.getRequestContentType();
                    String respContentType = protocol.getResponseContentType();
                    if (reqContentType != null && reqContentType.trim().length() > 0) {
                        html.tagc("p", "<b>Request Content-type:</b> " + reqContentType);
                    }
                    if (respContentType != null && respContentType.trim().length() > 0) {
                        html.tagc("p", "<b>Response Content-type:</b> " + respContentType);
                    }
                }

                html.tagc("h2", "Found Services");
                for (String service : this.externalProtocolService.getAvailableServices()) {
                    ServiceReference sref = this.bundleContext.getServiceReference(service);
                    if (sref != null) {
                        html.tagc("p", "<a href=\"" + service + "\">" + service + "</a> <i>Bundle version:</i> " +
                                sref.getBundle().getVersion() +
                                ", <i>Bundle symbolic name:</i> " + sref.getBundle().getSymbolicName() + ", " +
                                "<i>Bundle id:</i> " + sref.getBundle().getBundleId());
                    }
                }
            }
            html.tage("body");
        }
        html.tage("html");
    }

    /**
     * Handles the service page showing information about a specific service, like all its methods.
     *
     * @param html    The HTMLWriter to write to.
     * @param service The service to show information about.
     * @param req     The HTTPServletRequest.
     * @throws IOException
     */
    private void handleServicePage(HTMLWriter html, String service, HttpServletRequest req) throws IOException {
        Set<String> methodNames = this.externalProtocolService.getAvailableServiceFunctionNames(service);

        html.tag("html");
        {
            html.tag("body", "", BG_COLOR);
            {
                html.tagc("h1", "ApplicationPlatformServices (APS) Remote service call over HTTP transport provider");
                html.tagc("p", "Here the service and all its methods are displayed. Each method is clickable for details on the method.");

                html.tagc("h2", "Service");
                if (!methodNames.isEmpty()) {
                    html.tagc("h3", service + " {");
                    html.tag("ul");
                    for (String method : methodNames) {
                        @SuppressWarnings("unchecked")
                        APSExternallyCallable<Object> callable = this.externalProtocolService.getCallable(service, method);

//                        String params = "";
//                        String comma = "";
//                        for (DataTypeDescription parameter : callable.getParameterDataDescriptions()) {
//                            params = params + comma + toTypeName(parameter);
//                            comma = ", ";
//                        }

                        html.tagc("h4", toMethodDecl(callable, service, method));
                    }
                    html.tage("ul");
                    html.tagc("h3", "}");

                    html.tagc("h2", "Protocol URLs");
                    html.tagc("p", "Please note that even though these urls include the service, not all protocols require the service in " +
                            "the URL!");

                    for (StreamedRPCProtocol protocol : this.externalProtocolService.getAllStreamedProtocols()) {
                        html.tagc("h3", protocol.getServiceProtocolName() + " : " + protocol.getServiceProtocolVersion());

                        html.tagc("p", "http://" + req.getLocalName() + ":" + req.getLocalPort() + "/apsrpc/" +
                                protocol.getServiceProtocolName() + "/" + protocol.getServiceProtocolVersion() + "/" + service + "/");
                    }

                } else {
                    html.tagc("h2", "Service '" + service + "' not found!");
                }
            }
            html.tage("body");
        }
        html.tage("html");
    }

    /**
     * Handles the method page showing details about a method including parameters, return type, and if no args method the
     * result of the execution of the method.
     *
     * @param html    The HTMLWriter to write to.
     * @param service The service the method belongs to.
     * @param method  The method to show information for.
     * @throws IOException
     */
    private void handleMethodPage(HTMLWriter html, String service, String method, HttpServletRequest request) throws IOException {
        boolean execute = false;
        int paramPos = 0;
        if (method.indexOf("-") > 0) {
            String[] parts = method.split("-");
            if (parts.length >= 2) {
                if (parts[1].equals("exec")) {
                    execute = true;
                    method = parts[0];
                }
            }
        }
        @SuppressWarnings("unchecked")
        APSExternallyCallable<Object> callable = this.externalProtocolService.getCallable(service, method);
        html.tag("html");
        {
            html.tag("body", "", BG_COLOR);
            {
                if (callable != null) {
                    html.tagc("h1", "ApplicationPlatformServices (APS) Remote service call over HTTP transport provider");
                    html.tagc("p",
                            "This page provides details about a method. "
                    );
                    html.tagc("p",
                            "The method can be executed by providing the arguments and pressing 'Execute'. For boolean values " +
                                    "specify 'true' or 'false', for floating point numbers specify 'n.n', for long and ints, etc specify " +
                                    "'n', for string values specify \"<i>value</i>\", and for objects specify the object in JSON format " +
                                    "starting with { and ending with }. This is very useful for testing/debugging services."
                    );
                    html.tagc("h2", toMethodDecl(callable, null, method));

                    html.tagc("h3", "Parameters");
                    html.text("<ul>");
                    html.text("<form name=\"input\" action=\"" + method + "-exec\" method=\"post\">");
                    String comma = "";
                    paramPos = 0;
                    for (DataTypeDescription parameter : callable.getParameterDataDescriptions()) {
                        html.text(comma);
                        displayDataType(html, parameter);
                        String value = request.getParameter("param" + paramPos);
                        if (value == null) value = "";
                        html.text("&nbsp;&nbsp;&nbsp;&nbsp;<textarea cols=\"60\" rows=\"2\" name=\"param" + paramPos + "\">" + value +
                                "</textarea>");
                        comma = ",<br/>";
                        ++paramPos;
                    }
                    html.text("</ul>");

                    html.tagc("h3", "Returntype");
                    html.text("<ul>");
                    displayDataType(html, callable.getReturnDataDescription());
                } else {
                    html.tagc("h2", "Method '" + method + "' was not found!");
                }
                html.text("</ul>");

                html.tagc("h2", "Execution");
                html.text("<ul>");
                if (execute) {
                    html.tagc("p", execute(callable, paramPos, request));
                } else {
                    html.text("<input type=\"submit\" value=\"Execute\"");
                }
                html.text("</ul>");
                html.text("</form>");
            }
            html.tage("body");
        }
        html.tage("html");
    }

    private String execute(APSExternallyCallable<Object> callable, int noParams, HttpServletRequest request) throws IOException {
        try {
            String paramFail = null;
            Object[] args = new Object[noParams];
            int p = 0;
            for (DataTypeDescription parameter : callable.getParameterDataDescriptions()) {
                String paramValue = request.getParameter("param" + p);
                if (paramValue != null) {
                    Object paramJavaValue = null;
                    if (paramValue.toLowerCase().equals("true") || paramValue.toLowerCase().equals("false")) {
                        paramJavaValue = Boolean.valueOf(paramValue);
                    } else if (paramValue.trim().startsWith("\"")) {
                        paramValue = paramValue.trim().substring(1);
                        paramValue = paramValue.substring(0, paramValue.length() - 1);
                        paramJavaValue = paramValue;
                    } else if (paramValue.trim().startsWith("{")) {
                        ByteArrayInputStream bais = new ByteArrayInputStream(paramValue.getBytes());
                        JSONValue jsonObj = this.jsonService.readJSON(bais, null);
                        bais.close();
                        Class javaType = callable.getServiceBundle().loadClass(parameter.getObjectQName());
                        paramJavaValue = this.jsonService.jsonToJava(jsonObj, javaType);
                    } else {
                        if (paramValue.contains(".")) {
                            if (parameter.getDataType() == DataType.DOUBLE)
                                paramJavaValue = Double.valueOf(paramValue);
                            else if (parameter.getDataType() == DataType.FLOAT)
                                paramJavaValue = Float.valueOf(paramValue);
                        } else if (parameter.getDataType() == DataType.LONG)
                            paramJavaValue = Long.valueOf(paramValue);
                        else if (parameter.getDataType() == DataType.INT)
                            paramJavaValue = Integer.valueOf(paramValue);
                        else if (parameter.getDataType() == DataType.SHORT)
                            paramJavaValue = Short.valueOf(paramValue);
                        else if (parameter.getDataType() == DataType.BYTE)
                            paramJavaValue = Byte.valueOf(paramValue);
                        else {
                            paramFail = "Did you forget to quote a string value ?";
                        }

                    }
                    args[p] = paramJavaValue;
                } else {
                    paramFail = "Parameter #" + p + " was null!";
                }
                ++p;
            }
            if (paramFail == null) {
                Object result = callable.call(args);
                JSONValue jsonValue = this.jsonService.javaToJSON(result);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                this.jsonService.writeJSON(baos, jsonValue, false);
                baos.close();
                return "<pre>" + baos.toString() + "</pre>";
            } else {
                return "Bad parameter: " + paramFail;
            }
        } catch (Exception e) {
            return "Failed: " + e.getMessage();
        }
    }

    /**
     * Displays the specified data type.
     *
     * @param html     The HMTLWriter to display on.
     * @param dataType The data type to display.
     * @throws IOException
     */
    private void displayDataType(HTMLWriter html, DataTypeDescription dataType) throws IOException {
        if (!dataType.getDataType().isStructured()) {
            html.text(toTypeName(dataType));
        } else {
            if (dataType.hasMembers()) {
                html.text(toTypeName(dataType) + " {");
                html.text("<ul>");
                for (String memberName : dataType.getMemberNames()) {
                    displayDataType(html, dataType.getMemberDataDescriptionByName(memberName));
                    html.text(" " + memberName + ";<br/>");
                }
                html.text("</ul>");
                html.text("}");
            } else {
                if (dataType.getDataType() == DataType.LIST) {
                    html.text(dataType.getDataType().getTypeName() + "&lt;?&gt;");
                } else if (dataType.getDataType() == DataType.MAP) {
                    html.text(dataType.getDataType().getTypeName() + "&lt;?,?&gt;");
                } else {
                    html.text(toTypeName(dataType));
                }
            }
        }
    }

    /**
     * Creates a displayable method declaration.
     *
     * @param callable The callable to create the method declaration from.
     * @param service  The service the method belongs to. If this is provided the method name will be a link. If null no link will be created.
     * @param method   The name of the method.
     * @return A String with a full method declaration.
     */
    private String toMethodDecl(APSExternallyCallable<Object> callable, String service, String method) {
        if (callable == null) {
            return "";
        }
        String params = "";
        String comma = "";
        for (DataTypeDescription parameter : callable.getParameterDataDescriptions()) {
            String typeName = toTypeName(parameter);
            if (typeName.indexOf('.') > 0) {
                String[] parts = typeName.split("\\.");
                typeName = parts[parts.length - 1];
            }
            params = params + comma + typeName;
            comma = ", ";
        }

        if (service != null) {
            return toTypeName(callable.getReturnDataDescription()) + " <a href=\"" + service + "/" + method + "\">" + method +
                    "</a>;";
        } else {
            return toTypeName(callable.getReturnDataDescription()) + " " + method + ";";
        }
    }

    /**
     * Builds a displayable type name from a DataTypeDescription.
     *
     * @param dataTypeDescription The DataTypeDescripion to build a display name from.
     * @return A type name.
     */
    private String toTypeName(DataTypeDescription dataTypeDescription) {
        String retData = dataTypeDescription.getObjectQName();
        if (retData == null) {
            retData = dataTypeDescription.getDataType().getTypeName();
        }
        return retData;
    }

    /**
     * Small utility class to produce HTML output.
     */
    private class HTMLWriter {
        //
        // Private Members
        //

        /**
         * The stream to write to.
         */
        private OutputStream out = null;

        //
        // Constructors
        //

        /**
         * Creates a new HTMLWriter instance.
         *
         * @param out The OutputStream to write to.
         */
        public HTMLWriter(OutputStream out) {
            this.out = out;
        }

        //
        // Methods
        //

        /**
         * Writes to output stream with some conversions.
         *
         * @param text The text to write.
         * @throws IOException
         */
        private void write(String text) throws IOException {
            String[] parts = text.split(" ");

            for (String part : parts) {
                if (part.startsWith("http://") || part.startsWith("https://")) {
                    String link = "<a href=\"" + part + "\">" + part + "</a>";
                    text = text.replace(part, link);
                }
            }

            this.out.write(text.getBytes());
        }

        /**
         * Writes text.
         *
         * @param content The text to write.
         * @throws IOException
         */
        public void text(String content) throws IOException {
            write(content);
        }

        /**
         * Writes an html tag.
         *
         * @param tag        The tag to write.
         * @param content    The content of the tag.
         * @param attributes The attributes of the tag. Example: "color=#ffffff".
         * @throws IOException The one and only!
         */
        public void tag(String tag, String content, String... attributes) throws IOException {
            String attrs = "";
            if (attributes != null) {
                String comma = "";
                attrs = " ";
                for (String attribute : attributes) {
                    attrs += comma;
                    attrs += attribute;
                    comma = ", ";
                }
            }
            write(("<" + tag + attrs + ">" + content));
        }

        /**
         * Writes an html tag.
         *
         * @param tag     The tag to write.
         * @param content The content of the tag.
         * @throws IOException The one and only!
         */
        @SuppressWarnings("UnusedDeclaration")
        public void tag(String tag, String content) throws IOException {
            tag(tag, content, (String[])null);
        }

        /**
         * Writes a complete ending tag.
         *
         * @param tag        The tag to write.
         * @param content    The content of the tag.
         * @param attributes The attributes of the tag.
         * @throws IOException
         */
        @SuppressWarnings("UnusedDeclaration")
        public void tagc(String tag, String content, String... attributes) throws IOException {
            tag(tag, content, attributes);
            tage(tag);
        }

        /**
         * Writes a complete ending tag.
         *
         * @param tag     The tag to write.
         * @param content The content of the tag.
         * @throws IOException
         */
        public void tagc(String tag, String content) throws IOException {
            tag(tag, content, (String[])null);
            tage(tag);
        }

        /**
         * Writes an html tag.
         *
         * @param tag The tag to write.
         * @throws IOException The one and only!
         */
        public void tag(String tag) throws IOException {
            tag(tag, "", (String[])null);
        }

        /**
         * Writes an end tag.
         *
         * @param tag The tag to end.
         * @throws IOException
         */
        public void tage(String tag) throws IOException {
            write(("</" + tag + ">"));
        }
    }
}
