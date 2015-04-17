package se.natusoft.osgi.aps.net.messaging.service

import se.natusoft.osgi.aps.net.messaging.apis.MemberDiscovery

/**
 * Tracks members using multicast and TCP connections from other members.
 */
class MulticastMemberDiscovery implements MemberDiscovery {

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
