package se.natusoft.osgi.aps.api.net.rpc.model;

import se.natusoft.osgi.aps.api.net.rpc.errors.RPCError;

/**
 * An instance of this can be passed to RPCRequest to convert the cauth exception to an RPCError.
 */
public interface RPCExceptionConverter {

    /**
     * This should be called on any service exception to convert the exception to an RPCError.
     *
     * @param e The exception to convert.
     */
    RPCError convertException(Exception e);
}
