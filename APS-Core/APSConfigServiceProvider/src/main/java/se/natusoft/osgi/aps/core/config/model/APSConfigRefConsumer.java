package se.natusoft.osgi.aps.core.config.model;

import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigReference;

/**
 * Takes APSConfigReference instances.
 */
public interface APSConfigRefConsumer {

    /**
     * Receives a config value reference.
     *
     * @param ref The reference received.
     */
    public void setConfigReference(APSConfigReference ref);
}
