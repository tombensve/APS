package se.natusoft.aps.core.providers

import groovy.transform.CompileStatic
import se.natusoft.aps.core.api.APSJson

/**
 * Provides an implementation of APSJson.
 */
@CompileStatic
class APSJsonProvider extends LinkedHashMap<String, Object> implements APSJson {

    /**
     * Creates a new APSJsonProvider.
     */
    APSJsonProvider() {}

    /**
     * Creates a new APSJsonProvider with JSON data.
     *
     * @param json The JSON Map to create it with.
     */
    APSJsonProvider(Map<String, Object> json) {
        addJson( json )
    }

    /**
     * Adds a JSON Map to this object.
     *
     * @param json The JSON Map tp add.
     */
    void addJson(Map<String, Object> json) {
        super.putAll( json )
    }

    //
    // Convenience Methods
    //

}
