/*
 *
 * PROJECT
 *     Name
 *         APS NTP Time Service Provider
 *
 *     Code Version
 *         1.0.0
 *
 *     Description
 *         Provides time from a configured NTP server.
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
 *         2018-05-25: Created!
 *
 */
package se.natusoft.osgi.aps.misc.time

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.apache.commons.net.ntp.TimeInfo
import se.natusoft.osgi.aps.activator.annotation.ConfigListener
import se.natusoft.osgi.aps.api.core.config.APSConfig
import se.natusoft.osgi.aps.api.misc.time.APSTimeService
import se.natusoft.osgi.aps.constants.APS

import se.natusoft.osgi.aps.util.APSLogger
import se.natusoft.osgi.aps.activator.annotation.Managed
import se.natusoft.osgi.aps.activator.annotation.OSGiProperty
import se.natusoft.osgi.aps.activator.annotation.OSGiServiceProvider
import se.natusoft.osgi.aps.activator.annotation.Schedule

import java.time.Instant
import java.util.concurrent.TimeUnit
import org.apache.commons.net.ntp.NTPUDPClient

/**
 * Provides an implementation of APSTimeService.
 */
@SuppressWarnings( "GroovyUnusedDeclaration" )
@CompileStatic
@OSGiServiceProvider(
        properties = [
                @OSGiProperty( name = APS.Service.Provider, value = "aps-ntp-time-service-provider" ),
                @OSGiProperty( name = APS.Service.Category, value = APS.Value.Service.Category.Misc ),
                @OSGiProperty( name = APS.Service.Function, value = APS.Value.Service.Function.Time ),
                @OSGiProperty( name = APS.Uses.Network, value = APS.TRUE )
        ]
)
class APSNTPTimeServiceProvider implements APSTimeService {

    //
    // Private Members
    //

    @Managed
    private APSLogger logger

    private APSConfig config

    // Note to self: Must keep local time diff or call NTP server on every call to getTime()!
    private long remoteTimeDiff = 0

    private Instant lastTimeUpdate = null

    private NTPUDPClient client = new NTPUDPClient()

    @SuppressWarnings( "GroovyUnusedDeclaration" )
    @Schedule( delay = 0L, repeat = 60L, timeUnit = TimeUnit.MINUTES )
    private Runnable refreshTime = {
        this.logger.info( "Refreshing time!" )

        if ( this.config != null ) {

            String ntpServer = (String)this.config[ "ntpServer" ]

            try {

                this.logger.info( "Getting time from '${ ntpServer }' ..." )

                TimeInfo timeInfo = this.client.getTime( InetAddress.getByName( ntpServer ) )

                long now = new Date().time
                long receivedTime = timeInfo.getMessage().getTransmitTimeStamp().getTime()

                this.remoteTimeDiff = now - receivedTime
                this.lastTimeUpdate = Instant.ofEpochMilli( now )
            }
            catch ( UnknownHostException uhe ) {

                this.logger.error( "Failed to find specified NTP server '${ ntpServer }'!", uhe )
            }
            catch ( IOException ioe ) {

                this.logger.error( "Failed to get time from '${ ntpServer }'!", ioe )
            }
        }
    }

    //
    // Methods
    //

    @ConfigListener( apsConfigId = "apsNTPTimeProvider" )
    void configReceiver( APSConfig config ) {
        this.config = config
        this.refreshTime.run()
    }

    /**
     * Returns the time provided by the service.
     */
    @Override
    Instant getTime() {
        Instant.ofEpochMilli( new Date().time + this.remoteTimeDiff )
    }

    /**
     * Returns the last time the time was updated or null if there have been no successful updates.
     */
    @Override
    Instant getLastTimeUpdate() {
        return this.lastTimeUpdate
    }
}
