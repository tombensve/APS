package se.natusoft.osgi.aps.discoveryservice

/**
 * This represents the actions that can be sent for the discovery service.
 */
enum DiscoveryAction {

    ADD,
    REMOVE;

    public static DiscoveryAction from(Object source) {
        return valueOf(source.toString())
    }
}
