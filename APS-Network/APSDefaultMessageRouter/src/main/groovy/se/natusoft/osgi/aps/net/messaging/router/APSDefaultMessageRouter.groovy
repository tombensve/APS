package se.natusoft.osgi.aps.net.messaging.router

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.osgi.framework.BundleContext
import se.natusoft.docutations.NotNull
import se.natusoft.docutations.Nullable
import se.natusoft.osgi.aps.api.constants.APS
import se.natusoft.osgi.aps.api.net.messaging.exception.APSMessagingException
import se.natusoft.osgi.aps.api.net.messaging.model.APSMessage
import se.natusoft.osgi.aps.api.net.messaging.service.APSMessageService
import se.natusoft.osgi.aps.api.net.messaging.service.APSMessageTopicsService
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.APSServiceTracker
import se.natusoft.osgi.aps.tools.annotation.activator.BundleStop
import se.natusoft.osgi.aps.tools.annotation.activator.Initializer
import se.natusoft.osgi.aps.tools.annotation.activator.Managed
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiProperty
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiService
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiServiceProvider
import se.natusoft.osgi.aps.tools.tracker.WithService

/**
 * Provides an implementation of APSMessageService that acts as a router to other implementations of the same service.
 */
@SuppressWarnings("GroovyUnusedDeclaration") // Is only called through the interface and APSActivator will create this instance.
@CompileStatic                               // So the IDE cannot se that this is used.
@TypeChecked
@OSGiServiceProvider(
        properties = [
                @OSGiProperty(name = APS.Messaging.Protocol.Name, value = APS.Value.Messaging.Protocol.ROUTER),
                @OSGiProperty(name = APS.Service.Provider, value = "aps-default-message-router"),
                @OSGiProperty(name = APS.Service.Category, value = APS.Value.Service.Category.Network),
                @OSGiProperty(name = APS.Service.Function, value = APS.Value.Service.Function.Messaging)
        ]
)
class APSDefaultMessageRouter implements APSMessageService {

    //
    // Private Members
    //

    @Managed
    private BundleContext context

    @Managed(loggingFor = "aps-default-message-router")
    private APSLogger logger

    private Map<String, APSServiceTracker<APSMessageService>> trackers = new HashMap<>()

    @OSGiService(timeout = "20 sec")
    private APSMessageTopicsService apsTopicsService

    //
    // Management
    //

    @Initializer
    void init() {
        this.apsTopicsService.topics.each { APSMessageTopicsService.APSTopic topic ->
            if (!this.trackers.containsKey(topic.protocol)) {
                APSServiceTracker<APSMessageService> tracker =
                        new APSServiceTracker<>(
                                this.context,
                                APSMessageService.class,
                                "(${APS.Messaging.Protocol.Name}=${topic.protocol})",
                                "20 sec"
                        )
                tracker.start()
                this.trackers.put(topic.name, tracker)
            }
        }
    }

    @BundleStop
    void shutdown() {
        this.trackers.each { String key, APSServiceTracker<APSMessageService> tracker ->
            tracker.stop()
        }
    }

    //
    // Providing service
    //

    /**
     * Calls *all* tracked services and executes the action with the service.
     *
     * @param topic The topic to get service(s) for.
     * @param action The action to execute, will get an APSMessageService as parameter.
     */
    private void callAllServices(@NotNull String topic, @NotNull Closure action) {
        APSServiceTracker<APSMessageService> tracker = this.trackers.get(topic)
        if (tracker != null) {
            if (!tracker.hasTrackedService()) {
                this.logger.error("The topic '${topic}' has no tracked services!")
            }
            tracker.withAllAvailableServices(new WithService<APSMessageService>() {
                void withService(APSMessageService service) throws Exception {
                    action.call(service)
                }
            })
        }
        else {
            String message = "The topic '${topic}' has no trackers! This is a bug!"
            this.logger.error(message)
            throw new APSMessagingException(message)
        }
    }

    /**
     * Sends a message to the destination.
     *
     * @param topic The destination to send message.
     * @param message The message to send.
     * @param reply If the underlying message mechanism supports replies to specific messages such will be delivered to
     *              this listener. Can be null.
     */
    @Override
    void sendMessage(@NotNull String topic, @NotNull APSMessage message, @Nullable APSMessageService.Listener reply) {
        callAllServices(topic, { APSMessageService service ->
            service.sendMessage(topic, message, reply)
        })
    }

    /**
     * Adds a listener for messages arriving on a specific source.
     *
     * @param topic The endpoint to listen to.
     * @param listener The listener to call with received messages.
     */
    @Override
    void addMessageListener(@NotNull String topic, @NotNull APSMessageService.Listener listener) {
        callAllServices(topic, { APSMessageService service ->
            service.addMessageListener(topic, listener)
        })
    }

    /**
     * Removes a listener for a source.
     *
     * @param topic The endpoint to remove listener for.
     * @param listener The listener to remove.
     */
    @Override
    void removeMessageListener(@NotNull String topic, @NotNull APSMessageService.Listener listener) {
        callAllServices(topic, { APSMessageService service ->
            service.removeMessageListener(topic, listener)
        })
    }
}
