package se.natusoft.osgi.aps.tcpipsvc.config

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.osgi.aps.api.core.config.APSConfig
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigDescription
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigItemDescription
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue

@APSConfigDescription(
        configId = "destinations",
        description = "Named destinations",
        version = "1.0.0"
)
@CompileStatic
@TypeChecked
class NamedDestinationsConfig extends APSConfig {

    @APSConfigItemDescription(description = "The destination name.", environmentSpecific = true)
    public APSConfigValue destName

    @APSConfigItemDescription(description = "The destination URI. (tcp|udp|multicast://host:port[#secure])", environmentSpecific = true)
    public APSConfigValue destURI
}
