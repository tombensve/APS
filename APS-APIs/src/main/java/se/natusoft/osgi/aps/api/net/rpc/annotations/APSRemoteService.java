package se.natusoft.osgi.aps.api.net.rpc.annotations;

import se.natusoft.osgi.aps.api.external.extprotocolsvc.model.APSRESTCallable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is specified on methods that can be called remotely. It can be used purely for documentation,
 * but in case of a REST API using the JSONREST protocol part of the APSStreamedJSONRPCProtocolProvider
 * bundle this is needed and the REST method that applies must be specified.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface APSRemoteService {
    /**
     * This needs to be provided if you are providing a REST API using JSONREST protocol of the
     * APSStreamedJSONRPCProtocolProvider bundle.
     */
    APSRESTCallable.HttpMethod httpMethod() default APSRESTCallable.HttpMethod.NONE;
}
