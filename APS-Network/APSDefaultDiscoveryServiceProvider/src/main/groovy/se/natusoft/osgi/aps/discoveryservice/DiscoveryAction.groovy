package se.natusoft.osgi.aps.discoveryservice

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

/**
 * This represents the actions that can be sent for the discovery service.
 */
@CompileStatic
@TypeChecked
enum DiscoveryAction {

    ADD,
    REMOVE;

    public static DiscoveryAction from(Object source) {
        return valueOf(source.toString())
    }
}
