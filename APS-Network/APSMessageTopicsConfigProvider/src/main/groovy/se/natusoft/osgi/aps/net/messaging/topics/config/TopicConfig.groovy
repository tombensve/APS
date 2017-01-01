/* 
 * 
 * PROJECT
 *     Name
 *         APSMessageTopicsConfigProvider
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         A service that provides mappings between topics and protocol names to an APSMessageService router.
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
package se.natusoft.osgi.aps.net.messaging.topics.config

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.osgi.aps.api.core.config.APSConfig
import se.natusoft.osgi.aps.api.core.config.ManagedConfig
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigDescription
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigItemDescription
import se.natusoft.osgi.aps.api.core.config.model.APSConfigList
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValueList

@CompileStatic
@TypeChecked
@APSConfigDescription(
        configId = "se.natusoft.osgi.aps.net.messaging.topics",
        description = "Configuration for messaging topics.",
        version =  "1.0.0",
        group = "network"
)
class TopicConfig extends APSConfig {

    /**
     * Provides an auto managed instance of this config when this class is specified with APS-Configs: in MANIFEST.MF.
     * This also allows us to wait for the config to become managed before we try to access it. Our bundle might be
     * upp and running before the APSConfigServiceProvider bundle which handles the auto management of the config
     * by using the extender pattern. Even if the config service is running before us we might access the config
     * before the config service have had a change to manage the config. Using this constant instance of ManagedConfig
     * is the safest way to handle auto managed configurations.
     */
    public static final ManagedConfig<TopicConfig> managed = new ManagedConfig<TopicConfig>()


    @APSConfigItemDescription(
            description = "Provides a list of topics and the implementations to use for sending and receiving messages on these topics. For these mappings to have any effect the code must use APS.Messaging.Protocol.Name ('aps-messaging-protocol') OSGi service property constant with a value of APS.Value.Messaging.Protocol.ROUTER ('ROUTER') when looking up an APSMessageService.",
            environmentSpecific = true
    )
    public APSConfigList<Topic> topics

    @APSConfigDescription(
            configId = "topic-entry",
            description = "Maps a topic to an implementation.",
            version = "1.0.0"
    )
    static class Topic extends APSConfig {

        @APSConfigItemDescription(
                description = "The name of the topic.",
                environmentSpecific = false
        )
        public APSConfigValue name

        @APSConfigItemDescription(
                description = "The protocol to use for this topic. It is basically a name to differentiate different coexisting implementations. Each implementation will register themselves with this name in property 'aps-messaging-protocol' and the name here will be used to find the correct implementation by using the same property when looking for service. The documentation for each implementation should specify which name it is using in this property when registering itself as an OSGi service.",
                environmentSpecific = false
        )
        public APSConfigValue protocol
    }
}
