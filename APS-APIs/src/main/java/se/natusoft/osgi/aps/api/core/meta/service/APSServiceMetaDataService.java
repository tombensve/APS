package se.natusoft.osgi.aps.api.core.meta.service;

import se.natusoft.osgi.aps.api.misc.json.model.JSONObject;

/**
 * This is a service that bundles can register to provide meta data for a service.
 * This is of course entirely optional, but can be useful for debugging.
 *
 * There will be an admin web that displays all these found.
 *
 * The information provided is read-only.
 */
public interface APSServiceMetaDataService {

    /**
     * Returns meta data about the service as a JSON object. Any structure is allowed
     * as long as the top is a JSON object.
     *
     * The returned JSON object must contain "serviceName": "name" as a minimum and preferably also
     * "serviceVersion": "1.2.3".
     */
    JSONObject getServiceMetaData();
}
