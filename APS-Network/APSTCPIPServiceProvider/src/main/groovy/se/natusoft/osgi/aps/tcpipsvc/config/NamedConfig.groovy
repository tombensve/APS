package se.natusoft.osgi.aps.tcpipsvc.config

import se.natusoft.osgi.aps.api.core.config.APSConfig
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigDescription
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigItemDescription
import se.natusoft.osgi.aps.api.core.config.annotation.APSDefaultValue
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue

@APSConfigDescription(
        configId = "named",
        description = "This is a named config entry.",
        version = "1.0.0"
)
public class NamedConfig extends APSConfig {

    @APSConfigItemDescription(
            description = "The name of this config. This is the name passed in the service to use this configuration.",
            environmentSpecific = true
    )
    public APSConfigValue name

    @APSConfigItemDescription(
            description = "A comment / description of the entry.",
            environmentSpecific = true
    )
    public APSConfigValue comment

    @APSConfigItemDescription(
            description = "Is this a TCP, UDP, or Multicast configuration ?",
            // https://jira.codehaus.org/browse/GROOVY-3278
            validValues = ["TCP", "UDP", "Multicast"],
            environmentSpecific = true
    )
    public APSConfigValue type

    @APSConfigItemDescription(
            description =
                    "An ip address or hostname. For receiving this address is bound to, for sending this address is connected to. For multicast you can leave this blank to default to 224.0.0.1.",
            environmentSpecific = true
    )
    public APSConfigValue address

    @APSConfigItemDescription(
            description = "The port to listen or connect to.",
            environmentSpecific = true
    )
    public APSConfigValue port

    @APSConfigItemDescription(
            description = "Make connection secure when security is available.",
            isBoolean = true,
            defaultValue = @APSDefaultValue(value = "false"),
            environmentSpecific = true
    )
    public APSConfigValue secure
}
