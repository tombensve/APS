package se.natusoft.osgi.aps.net.messaging.router

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.junit.Test
import static org.junit.Assert.*;
import org.osgi.framework.BundleContext
import se.natusoft.docutations.NotNull
import se.natusoft.docutations.Nullable
import se.natusoft.osgi.aps.api.net.messaging.exception.APSMessagingException
import se.natusoft.osgi.aps.api.net.messaging.model.APSMessage
import se.natusoft.osgi.aps.api.net.messaging.service.APSMessageService
import se.natusoft.osgi.aps.constants.APS
import se.natusoft.osgi.aps.net.messaging.models.config.TestConfigList
import se.natusoft.osgi.aps.net.messaging.models.config.TestConfigValue
import se.natusoft.osgi.aps.net.messaging.topics.config.TopicConfig
import se.natusoft.osgi.aps.test.tools.OSGIServiceTestTools
import se.natusoft.osgi.aps.tools.APSActivator
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.APSServiceTracker
import se.natusoft.osgi.aps.tools.annotation.activator.Managed
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiProperty
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiServiceProvider

@CompileStatic
@TypeChecked
class APSDefaultMessageRouterTest extends OSGIServiceTestTools {

    public static boolean send1 = false
    public static boolean send2 = false

    @Test
    void test() throws Exception {

        // Deploy dependent services
        deploy 'msg-svc-one' with new APSActivator() using '/se/natusoft/osgi/aps/net/messaging/router/MsgSvc1.class'

        deploy 'msg-svc-two' with new APSActivator() using '/se/natusoft/osgi/aps/net/messaging/router/MsgSvc2.class'

        TopicConfig topicConfig = new TopicConfig()
        deploy 'aps-message-topics-config-provider' with new APSActivator() with {

            topicConfig.topics = new TestConfigList<TopicConfig.Topic>()

            TopicConfig.Topic cv = new TopicConfig.Topic()
            cv.name = new TestConfigValue(value: "dest-one")
            cv.protocol = new TestConfigValue(value: "pigeon")
            ((TestConfigList)topicConfig.topics).getConfigs().add(cv)

            cv = new TopicConfig.Topic()
            cv.name = new TestConfigValue(value: "dest-two")
            cv.protocol = new TestConfigValue(value: "bottle")
            ((TestConfigList)topicConfig.topics).getConfigs().add(cv)

            topicConfig
        } from 'se.natusoft.osgi.aps', 'aps-message-topics-config-provider', '1.0.0'

        // Deploy the service we are testing!
        deploy 'aps-default-message-router' with new APSActivator() from 'APS-Network/APSDefaultMessageRouter/target/classes'

        // Do test
        try {
            with_new_bundle 'test-exec-bundle', { BundleContext context ->

                APSServiceTracker<APSMessageService> routerTracker =
                        new APSServiceTracker<>(
                                context,
                                APSMessageService.class,
                                "(${APS.Messaging.Protocol.Name}=${APS.Value.Messaging.Protocol.ROUTER})",
                                "5 sec"
                        )
                routerTracker.start()

                APSMessageService router = routerTracker.allocateService()

                    // For those that does not understand testing nor the correlation between the code tested and
                    // the test (have unfortunately worked with such people): We only need to test one of the methods
                    // of this service since all service methods are delegated to the same "function" which does
                    // the delegation to the correct service depending on topic. Either they all succeed or all
                    // fail. Testing the other 2 methods also will give you a bit more code, and a bit more to
                    // execute when run, but will not in any way make the test better!

                    router.sendMessage("dest-one", new APSMessage.Provider(message: "qwerty".getBytes()), null)
                    assert send1
                    assert !send2

                    router.sendMessage("dest-two", new APSMessage.Provider(message: "qwerty".getBytes()), null)
                    assert send2

                    // This will leave the first service tracker without any services.
                    undeploy 'msg-svc-one'
                    try {
                        router.sendMessage("dest-one", new APSMessage.Provider(message: "qwerty".getBytes()), null)
                        fail("This should have thrown an APSMessagingException!")
                    }
                    catch (APSMessagingException ame) {
                        assert ame.message.contains("has no tracked services!")
                    }

                    // The service will update its trackers when the config is changed. So in this case we should
                    // enter the else of the first if in callAllServices(...).
                    ((TestConfigList)topicConfig.topics).configs.remove(0)
                    topicConfig.triggerConfigChangedEvent("dest-one")
                    Thread.sleep(300) // Wait for service to have time to update itself.
                    try {
                        router.sendMessage("dest-one", new APSMessage.Provider(message: "qwerty".getBytes()), null)
                    }
                    catch (APSMessagingException ame) {
                        assert ame.message.contains("has no trackers! This is probably due to bad configuration!")
                    }

                routerTracker.releaseService()
                routerTracker.stop()
            }
        }
        finally {
            shutdown()
        }
    }

}

@OSGiServiceProvider(
        properties = [
                @OSGiProperty(name = APS.Messaging.Protocol.Name, value = "pigeon"),
                @OSGiProperty(name = APS.Service.Provider, value = "aps-test-message-provider-one"),
                @OSGiProperty(name = APS.Service.Category, value = APS.Value.Service.Category.Network),
                @OSGiProperty(name = APS.Service.Function, value = APS.Value.Service.Function.Messaging)
        ]
)
class MsgSvc1 implements APSMessageService {

    @Managed(loggingFor = "msg-svc-one")
    private APSLogger logger

    @Override
    void sendMessage(@NotNull String topic, @NotNull APSMessage message, @Nullable APSMessageService.Listener reply) {
        this.logger.info("In sendMessage(...)!")
        APSDefaultMessageRouterTest.send1 = true
    }

    @Override
    void addMessageListener(@NotNull String topic, @NotNull APSMessageService.Listener listener) {

    }

    @Override
    void removeMessageListener(@NotNull String topic, @NotNull APSMessageService.Listener listener) {

    }
}

@OSGiServiceProvider(
        properties = [
                @OSGiProperty(name = APS.Messaging.Protocol.Name, value = "bottle"),
                @OSGiProperty(name = APS.Service.Provider, value = "aps-test-message-provider-two"),
                @OSGiProperty(name = APS.Service.Category, value = APS.Value.Service.Category.Network),
                @OSGiProperty(name = APS.Service.Function, value = APS.Value.Service.Function.Messaging)
        ]
)
class MsgSvc2 implements APSMessageService {

    @Managed(loggingFor = "msg-svc-two")
    private APSLogger logger

    @Override
    void sendMessage(@NotNull String topic, @NotNull APSMessage message, @Nullable APSMessageService.Listener reply) {
        this.logger.info("In sendMessage(...)!")
        APSDefaultMessageRouterTest.send2 = true
    }

    @Override
    void addMessageListener(@NotNull String topic, @NotNull APSMessageService.Listener listener) {

    }

    @Override
    void removeMessageListener(@NotNull String topic, @NotNull APSMessageService.Listener listener) {

    }
}

