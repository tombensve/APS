package se.natusoft.osgi.aps.misc.time

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.apache.commons.net.ntp.TimeInfo
import se.natusoft.osgi.aps.api.misc.time.APSTimeService
import se.natusoft.osgi.aps.constants.APS
import se.natusoft.osgi.aps.misc.time.config.NTPConfig
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.annotation.activator.Managed
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiProperty
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiServiceProvider
import se.natusoft.osgi.aps.tools.annotation.activator.Schedule

import java.time.Instant
import java.util.concurrent.TimeUnit
import org.apache.commons.net.ntp.NTPUDPClient

/**
 * Provides an implementation of APSTimeService.
 */
@SuppressWarnings("GroovyUnusedDeclaration")
@CompileStatic
@TypeChecked
@OSGiServiceProvider(
        properties = [
                @OSGiProperty(name = APS.Service.Provider, value = "aps-ntp-time-service-provider"),
                @OSGiProperty(name = APS.Service.Category, value = APS.Value.Service.Category.Misc),
                @OSGiProperty(name = APS.Service.Function, value = APS.Value.Service.Function.Time),
                @OSGiProperty(name = APS.Uses.Network, value = APS.TRUE)
        ]
)
public class APSNTPTimeServiceProvider implements APSTimeService {

    //
    // Private Members
    //

    @Managed
    private APSLogger logger

    private long remoteTimeDiff = 0

    private Instant lastTimeUpdate = null

    private NTPUDPClient client = new NTPUDPClient()

    @SuppressWarnings("GroovyUnusedDeclaration")
    @Schedule(delay = 0L, repeat = 30L, timeUnit = TimeUnit.MINUTES)
    private Runnable refreshTime = {
        this.logger.info("Refreshing time!")

        String[] ntpservers = NTPConfig.get.ntpServers.string.split(",")
        if (ntpservers != null && ntpservers.length > 0) {
            for (String ntpServer : ntpservers) {
                try {
                    this.logger.info("Trying '${ntpServer}' ...")

                    TimeInfo timeInfo = this.client.getTime(InetAddress.getByName(ntpServer))
                    long now = new Date().time

                    long receivedTime = timeInfo.getMessage().getTransmitTimeStamp().getTime()
                    this.remoteTimeDiff = now - receivedTime;
                    this.lastTimeUpdate = Instant.ofEpochMilli(now)

                    logger.info("Got time: (diff: ${this.remoteTimeDiff} milliseconds) " +
                            "${Instant.ofEpochMilli(timeInfo.getMessage().getTransmitTimeStamp().getTime())}")

                    break;
                }
                catch (UnknownHostException uhe) {
                    this.logger.error("Failed to find specified NTP server '${ntpServer}'!", uhe)
                }
                catch (IOException ioe) {
                    this.logger.error("Failed to get time from '${ntpServer}'!", ioe)
                }
            }
        }
    }

    //
    // Methods
    //

    /**
     * Returns the time provided by the service.
     */
    @Override
    Instant getTime() {
        Instant.ofEpochMilli(this.remoteTimeDiff + new Date().time)
    }

    /**
     * Returns the last time the time was updated or null if there have been no successful updates.
     */
    @Override
    Instant getLastTimeUpdate() {
        return this.lastTimeUpdate
    }
}
