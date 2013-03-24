package se.natusoft.osgi.aps.jsonrpc.protocols;

import se.natusoft.osgi.aps.api.misc.json.service.APSJSONExtendedService;
import se.natusoft.osgi.aps.tools.APSLogger;

/**
 * Provides a HTTP REST protocol.
 */
public class JSONREST  extends JSONHTTP {

    //
    // Constructors
    //

    /**
     * Creates a new JSONRPC20 instance.
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
        return "This provides an HTTP REST protocol that talks JSON. Requests should specify both service and method to call in " +
                "URL path and method parameters as either HTTP URL parameters or within a JSON array on the stream. " +
                "URL parameters are required on GET while a JSON array on the stream is required on POST or PUT. " +
                "Whatever the method call returns are converted to JSON and written on the response OutputStream. " +
                "This protocol variant returns true for 'supportsREST()' and APSExternalProtocolService used by " +
                "transports provides 'isRESTCallable(String serviceName)' and 'getRESTCallable(String serviceName)' " +
                "and should in that case (if they are an http transport) use the REST callable which will map " +
                "POST, PUT, GET, and DELETE to methods starting with 'post', 'put', 'get', and 'delete' in the " +
                "service.";
    }

    /**
     * @return true if the protocol supports REST.
     */
    @Override
    public boolean supportsREST() {
        return true;
    }

}
