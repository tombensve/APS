package se.natusoft.osgi.aps.api.core.memory;

/**
 * This is a very simple service providing memory storage of data. This allows a service for example to store in memory information
 * outside of its own bundle, and can thus be redeployed without loosing this information!
 *
 * __All stored information can however be lost when the service goes down depending on implementation!__
 */
public interface APSMemoryService {


}
