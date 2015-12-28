package se.natusoft.osgi.aps.tcpipsvc.config

import se.natusoft.osgi.aps.api.core.config.APSConfig
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigDescription
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigItemDescription
import se.natusoft.osgi.aps.api.core.config.annotation.APSDefaultValue
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue

@APSConfigDescription(
        configId = "expert",
        description = "Special expert settings..",
        version = "1.0.0"
)
public class ExpertConfig extends APSConfig {

    @APSConfigItemDescription(description = "Sets the size of the thread pool for executing callbacks of TCP service listeners.",
            environmentSpecific = true, defaultValue = @APSDefaultValue(value = "50"))
    public APSConfigValue tcpCallbackThreadPoolSize

    @APSConfigItemDescription(description = "If time in milliseconds between exceptions are less than this, exception guard will trigger when the below number of exceptions are reached.", environmentSpecific = true, defaultValue = @APSDefaultValue(value = "500"))
    public APSConfigValue exceptionGuardReactLimit

    @APSConfigItemDescription(description = "If the number of consecutive exceptions reaches this and they are all within the above reoccur time limit then the exception guard will report a problem and terminate whatever loop this occurs in.", environmentSpecific = true, defaultValue = @APSDefaultValue(value = "10"))
    public APSConfigValue exceptionGuardMaxExceptions
}
