package se.natusoft.osgi.aps.net.messaging.service

import se.natusoft.osgi.aps.net.messaging.apis.MemberDiscovery

/**
 * Uses configuration to determine members. If a member that is not configured connects then
 * it is rejected.
 */
class ConfiguredMemberDiscovery implements MemberDiscovery {

    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @Override
    Iterator<InetSocketAddress> iterator() {
        return null
    }
}
