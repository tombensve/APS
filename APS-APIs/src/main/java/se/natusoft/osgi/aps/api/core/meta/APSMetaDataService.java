package se.natusoft.osgi.aps.api.core.meta;

/**
 * This is a service that bundles can register to provide meta data for a service.
 * This is of course entirely optional, but can be useful for debugging.
 *
 * Yes, this could also be accomplished with JMX beans. This aims at being a very
 * simplistic provider of meta data / statistics information, doing that and only that.
 *
 * The information provided is read-only.
 */
public interface APSMetaDataService {

    /**
     * Returns meta data about the service as a JSON object..
     */
    APSMetaDataBean getMetaDataBean();

}
