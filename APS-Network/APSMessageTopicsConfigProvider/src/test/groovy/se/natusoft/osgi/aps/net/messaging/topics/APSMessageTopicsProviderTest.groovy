package se.natusoft.osgi.aps.net.messaging.topics

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.junit.Test
import org.osgi.framework.BundleContext
import se.natusoft.osgi.aps.api.net.messaging.service.APSMessageTopics
import se.natusoft.osgi.aps.json.APSJSONServiceActivator
import se.natusoft.osgi.aps.net.messaging.models.config.TestConfigValue
import se.natusoft.osgi.aps.net.messaging.topics.config.TopicConfig
import se.natusoft.osgi.aps.test.tools.OSGIServiceTestTools
import se.natusoft.osgi.aps.tools.APSActivator
import se.natusoft.osgi.aps.tools.APSServiceTracker

import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertTrue

@CompileStatic
@TypeChecked
class APSMessageTopicsProviderTest extends OSGIServiceTestTools {
    private static final String jsonConf = """
 {
     "version": "2.4",
     "topics": [
         {
             "comment": [
                 "This defines all communication for one topic. This example is a bit extreme since",
                 "it does a lot of protocols in one topic, but it just serves to demonstrate",
                 "how things can be setup, and I was a bit lazy and had bad imagination, OK!"
             ],
             "topic": "discovery",
             "protocols": [
                 "%% These are URIs specifying protocol, source/destination and direction.",

                 "multicast://all-systems.mcast.net:1234#out",
                 "multicast://all-systems.mcast.net:1234#in",
                 "tcp://host1:5678#out",
                 "tcp://host2:5678#out",
                 "tcp://host0:5678#in",
                 "udp://host3:9012#out",
                 "udp://host0:9012#in",
                 "amqp://rabbit1:1234/discovery#out",
                 "amqp://rabbit1:1234/discovery#in",
                 "jms://hornet1:1234#out",
                 "jms://hornet1:1234#in",
                 "pigeon://stockholm?cage=1#out",
                 "pigeon://visby?cage=3#in"
             ]
         },
     ]
 }
    """

    @Test
    void runTest() throws Exception {
        // ---- Deploy some bundles (Using Groovy DSL:ish goodness :-)) ---- //

        deploy 'aps-json-service' with new APSJSONServiceActivator() from 'se.natusoft.osgi.aps', 'aps-json-service-provider', '1.0.0'

        deploy 'aps-message-topics-provider' with new APSActivator() with {
            TopicConfig topicConfig = new TopicConfig()

            println "${jsonConf.toString()}"

            topicConfig.topicConfigJson = new TestConfigValue(value: jsonConf.toString())

            topicConfig
        } from 'APS-Network/APSMessageTopicsConfigProvider/target/classes'

        APSServiceTracker<APSMessageTopics> topicsServiceTracker = null

        try {
            with_new_bundle 'test-exec-bundle', { BundleContext context ->

                topicsServiceTracker = new APSServiceTracker<>(context, APSMessageTopics.class, "10 sec")
                topicsServiceTracker.start()

                APSMessageTopics topics = topicsServiceTracker.allocateService()

                //println "#topics: ${topics.topics.size()}"
                assertTrue("Wrong number of topics!", topics.topics.size() == 1)
                assertNotNull("'discovery' topic should not be null!", topics.getTopic("discovery"))
                assertTrue("There should be 13 protocols!", topics.getTopic("discovery").protocols.size() == 13)
                assertTrue("URI does not match!",
                        topics.getTopic("discovery").protocols[0].toString() == "multicast://all-systems.mcast.net:1234#out")
                assertTrue("URI does not match!",
                        topics.getTopic("discovery").protocols[4].toString() == "tcp://host0:5678#in")

                topicsServiceTracker.releaseService()
            }
        }
        finally {
            topicsServiceTracker.stop()
            shutdown()
        }
    }

}
