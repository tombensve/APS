/* 
 * 
 * PROJECT
 *     Name
 *         APS Default Message Router
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         An implementation of APSMessageService that registers itself with protocol "ROUTER", and tracks other
 *         APSMessageService instances not using "ROUTER" as protocol name. It uses configuration to map between
 *         topics and protocol name, and then forwards calls to tracked services of that protocol name.
 *         
 * COPYRIGHTS
 *     Copyright (C) 2012 by Natusoft AB All rights reserved.
 *     
 * LICENSE
 *     Apache 2.0 (Open Source)
 *     
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *     
 *       http://www.apache.org/licenses/LICENSE-2.0
 *     
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *     
 * AUTHORS
 *     tommy ()
 *         Changes:
 *         2017-01-01: Created!
 *         
 */
package se.natusoft.osgi.aps.net.messaging.router

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.osgi.framework.BundleContext
import se.natusoft.docutations.NotNull
import se.natusoft.docutations.Nullable
import se.natusoft.osgi.aps.api.net.messaging.exception.APSMessagingException
import se.natusoft.osgi.aps.api.net.messaging.service.APSMessageService
import se.natusoft.osgi.aps.api.net.messaging.service.APSMessageTopicsService
import se.natusoft.osgi.aps.api.net.messaging.service.APSSubscriber
import se.natusoft.osgi.aps.constants.APS
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.APSServiceTracker
import se.natusoft.osgi.aps.tools.annotation.activator.*
import se.natusoft.osgi.aps.tools.tracker.WithService

/**
 * Provides an implementation of APSMessageService that acts as a router to other implementations of the same service.
 */
@SuppressWarnings("GroovyUnusedDeclaration") // Is only called through the interface and APSActivator will create this instance.
@CompileStatic                               // So the IDE cannot se that this is used.
@TypeChecked
@OSGiServiceProvider(
        properties = [
                @OSGiProperty(name = APS.Service.Provider, value = "aps-default-message-router"),
                @OSGiProperty(name = APS.Service.Category, value = APS.Value.Service.Category.Network),
                @OSGiProperty(name = APS.Service.Function, value = APS.Value.Service.Function.Messaging),
                @OSGiProperty(name = APS.Messaging.Protocol.Name, value = APS.Value.Messaging.Protocol.ROUTER)
        ]
)
class APSDefaultMessageRouter
        extends APSMessageService.AbstractAPSMessageService
        implements APSMessageService, APSMessageTopicsService.TopicsUpdatedListener {

    //
    // Private Members
    //

    @Managed
    private BundleContext context

    @Managed(loggingFor = "aps-default-message-router")
    private APSLogger logger

    private Map<String /*Topic*/, APSServiceTracker<APSMessageService>> trackers =
            (Map<String, APSServiceTracker<APSMessageService>>)Collections.synchronizedMap(new HashMap<>())

    @OSGiService(timeout = "20 sec")
    private APSMessageTopicsService apsTopicsService

    //
    // Management
    //

    @Initializer
    void init() {
        Thread.start {
            loadTrackers()
            this.apsTopicsService.addTopicsUpdatedListener(this)
        }
    }

    void loadTrackers() {
        List<String> validTopics = new LinkedList<>()
        this.apsTopicsService.topics.each { APSMessageTopicsService.APSTopic topic ->
            if (!this.trackers.containsKey(topic.name)) {
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
            validTopics << topic.name
        }

        // Remove any previously created trackers for services that have been removed in config.
        this.trackers.findAll { String key, APSServiceTracker<APSMessageService> value -> !validTopics.contains(key) }.each {
            String topic, APSServiceTracker<APSMessageService>  value ->
            value.stop()
            this.trackers.remove(topic)
        }
    }

    @BundleStop
    void shutdown() {
        this.apsTopicsService.removeTopicsUpdatedListener(this)

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
                String message = "The topic '${topic}' has no tracked services!"
                this.logger.error(message)
                throw new APSMessagingException(message)
            }
            tracker.withAllAvailableServices(new WithService<APSMessageService>() {
                void withService(APSMessageService service) throws Exception {
                    action.call(service)
                }
            })
        }
        else {
            String message = "The topic '${topic}' has no trackers! This is probably due to bad configuration!"
            this.logger.error(message)
            throw new APSMessagingException(message)
        }
    }

    /**
     * Sends a message to the destination.
     *
     * @param topic The destination to send message.
     * @param message The message to send.
     * @param properties Implementation specific properties. Can be null.
     */
    @Override
    void publish(@NotNull String topic, @NotNull Object message, @Nullable Properties properties) {
        callAllServices(topic, { APSMessageService service ->
            service.publish(topic, message, properties)
        })
    }

    /**
     * Adds a listener for messages arriving on a specific source.
     *
     * @param topic The endpoint to listen to.
     * @param listener The listener to call with received messages.
     * @param properties Implementation specific properties. Can be null.
     */
    @Override
    void subscribe(@NotNull String topic, @NotNull APSSubscriber listener, @Nullable Properties properties) {
        callAllServices(topic, { APSMessageService service ->
            service.subscribe(topic, listener, properties)
        })
    }

    /**
     * Removes a listener for a source.
     *
     * @param topic The endpoint to remove listener for.
     * @param listener The listener to remove.
     */
    @Override
    void unsubscribe(@NotNull String topic, @NotNull APSSubscriber listener) {
        callAllServices(topic, { APSMessageService service ->
            service.unsubscribe(topic, listener)
        })
    }

    /**
     * Indicates that the list of topics have been updaetd.
     */
    @Override
    void topicsUpdated() {
        loadTrackers()
        logger.info("Updated service trackers due to config change!")
    }
}
