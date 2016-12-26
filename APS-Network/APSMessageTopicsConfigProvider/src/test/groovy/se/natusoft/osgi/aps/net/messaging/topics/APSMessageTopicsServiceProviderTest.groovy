package se.natusoft.osgi.aps.net.messaging.topics

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.junit.Test
import org.osgi.framework.BundleContext
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue
import se.natusoft.osgi.aps.api.net.messaging.service.APSMessageTopicsService
import se.natusoft.osgi.aps.json.APSJSONServiceActivator
import se.natusoft.osgi.aps.net.messaging.models.config.TestConfigList
import se.natusoft.osgi.aps.net.messaging.models.config.TestConfigValue
import se.natusoft.osgi.aps.net.messaging.topics.config.TopicConfig
import se.natusoft.osgi.aps.test.tools.OSGIServiceTestTools
import se.natusoft.osgi.aps.tools.APSActivator
import se.natusoft.osgi.aps.tools.APSServiceTracker

import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertTrue

@CompileStatic
@TypeChecked
class APSMessageTopicsServiceProviderTest extends OSGIServiceTestTools {

    @Test
    void runTest() throws Exception {
        // ---- Deploy some bundles (Using Groovy DSL:ish goodness :-)) ---- //

        // This is on the outside just so that we can manipulate it later. Note that we are setting config values
        // rather than running config service!
        TopicConfig topicConfig = new TopicConfig()

        deploy 'aps-message-topics-provider' with new APSActivator() with {

            topicConfig.topics = new TestConfigList<TopicConfig.Topic>()

            TopicConfig.Topic cv = new TopicConfig.Topic()
            cv.name = new TestConfigValue(value: "config")
            cv.protocol = new TestConfigValue(value: "vert.x")
            ((TestConfigList)topicConfig.topics).getConfigs().add(cv)

            cv = new TopicConfig.Topic()
            cv.name = new TestConfigValue(value: "some-topic")
            cv.protocol = new TestConfigValue(value: "basic-java-tcp")
            ((TestConfigList)topicConfig.topics).getConfigs().add(cv)

            cv = new TopicConfig.Topic()
            cv.name = new TestConfigValue(value: "myTopic")
            cv.protocol = new TestConfigValue(value: "amqp")
            ((TestConfigList)topicConfig.topics).getConfigs().add(cv)

            topicConfig
        } from 'APS-Network/APSMessageTopicsConfigProvider/target/classes'

        APSServiceTracker<APSMessageTopicsService> topicsServiceTracker = null

        try {
            with_new_bundle 'test-exec-bundle', { BundleContext context ->

                topicsServiceTracker = new APSServiceTracker<>(context, APSMessageTopicsService.class, "10 sec")
                topicsServiceTracker.start()

                APSMessageTopicsService topics = topicsServiceTracker.allocateService()

                assertTrue("Should be 3 topics!", topics.topics.size() == 3)
                assertTrue("Expected 'vert.x'!", topics.getTopic("config").protocol == "vert.x")
                assertTrue("Expected 'basic-java-tcp'!", topics.getTopic("some-topic").protocol == "basic-java-tcp")
                assertTrue("Expected 'amqp'!", topics.getTopic("myTopic").protocol == "amqp")

                // Change config value
                topicConfig.topics.get(0).protocol = new TestConfigValue(value: "netty")

                // Should still have old value:
                assertTrue("Expected 'vert.x'!", topics.getTopic("config").protocol == "vert.x")

                // Trigger ConfigChangedEvent.
                topicConfig.triggerConfigChangedEvent("topic-entry")

                // Now expects new value.
                assertTrue("Expected 'netty'!", topics.getTopic("config").protocol == "netty")

                topicsServiceTracker.releaseService()
            }
        }
        finally {
            topicsServiceTracker.stop()
            shutdown()
        }
    }

}
