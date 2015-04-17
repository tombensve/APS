package se.natusoft.osgi.aps.net.messaging.service

import se.natusoft.osgi.aps.net.messaging.apis.MemberDiscovery

/**
 * Created by tommy on 2015-02-22.
 */
class Sender {

    // Use ExecutorService for each member to continuously try to send message to each member. If the member
    // is not responding then the message sits until the member is responding.

    MemberDiscovery memberDiscovery
}
