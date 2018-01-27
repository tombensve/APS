package se.natusoft.osgi.aps.core.config

import org.osgi.framework.ServiceRegistration
import se.natusoft.osgi.aps.api.core.config.APSConfig
import se.natusoft.osgi.aps.api.core.filesystem.model.APSDirectory
import se.natusoft.osgi.aps.api.core.filesystem.model.APSFilesystem
import se.natusoft.osgi.aps.api.core.filesystem.service.APSFilesystemService
import se.natusoft.osgi.aps.api.core.platform.service.APSExecutionService
import se.natusoft.osgi.aps.api.core.platform.service.APSNodeInfoService
import se.natusoft.osgi.aps.api.core.store.APSDataStoreService
import se.natusoft.osgi.aps.api.messaging.APSMessageService
import se.natusoft.osgi.aps.core.lib.MapJsonDocValidator
import se.natusoft.osgi.aps.core.lib.StructMap
import se.natusoft.osgi.aps.exceptions.APSConfigException
import se.natusoft.osgi.aps.exceptions.APSValidationException
import se.natusoft.osgi.aps.json.JSON
import se.natusoft.osgi.aps.json.JSONErrorHandler
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.annotation.activator.BundleStop
import se.natusoft.osgi.aps.tools.annotation.activator.Initializer
import se.natusoft.osgi.aps.tools.annotation.activator.Managed
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiService

class ConfigManager {

    //
    // Private Members
    //

    @Managed(loggingFor = "aps-config-provider:config-manager")
    private APSLogger logger

    @OSGiService(additionalSearchCriteria = "(aps-messaging-protocol=vertx-eventbus)", nonBlocking = true)
    private APSMessageService messageService

    @OSGiService(nonBlocking = true)
    private APSNodeInfoService nodeInfoService

    @OSGiService(additionalSearchCriteria = "(service-persistence-scope=clustered)", nonBlocking = true)
    private APSDataStoreService dataStoreService

    @OSGiService(nonBlocking = true)
    private APSExecutionService execService

    private Map<String, ServiceRegistration> regs = [ : ]

    //
    // Initializer / Shutdown
    //

    @Initializer
    void init() {

    }

    @BundleStop
    private void shutDown() {

    }

    //
    // Methods
    //




}
