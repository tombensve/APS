package se.natusoft.aps.core.api

import groovy.transform.CompileStatic
import se.natusoft.aps.core.providers.APSJsonProvider

/**
 * Internally we treat JSON as Map<String, Object>. Jackson Jr can translate between Map<String, Object> and
 * actual JSON. Groovys JSONSlurper can also handle this format, but I've heard that JSONSlurper has some
 * bugs, which is why I stick to Jackson Jr.
 */
@CompileStatic
interface APSJson extends Map<String, Object>{

    //
    // Convenience Methods
    //

    default APSJson from(Map<String, Object> json) {
        new APSJsonProvider(json)
    }

}
