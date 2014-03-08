/* 
 * 
 * PROJECT
 *     Name
 *         APS Net Time Service Provider
 *     
 *     Code Version
 *         0.10.0
 *     
 *     Description
 *         Provides time translation between local time an network neutral time.
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
 *         2013-08-31: Created!
 *         
 */
package se.natusoft.osgi.aps.time;

import se.natusoft.osgi.aps.api.net.groups.service.APSGroupsService;
import se.natusoft.osgi.aps.api.net.groups.service.GroupMember;
import se.natusoft.osgi.aps.api.net.groups.service.NetTime;
import se.natusoft.osgi.aps.api.net.time.service.APSNetTimeService;
import se.natusoft.osgi.aps.tools.APSLogger;
import se.natusoft.osgi.aps.tools.annotation.activator.BundleStop;
import se.natusoft.osgi.aps.tools.annotation.activator.Managed;
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiService;
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiServiceProvider;

import java.io.IOException;
import java.util.Date;

/**
 * Provides an implementation of APSNetTimeService using APSGroupsService for providing the net time.
 */
@OSGiServiceProvider
public class APSNetTimeServiceProvider implements APSNetTimeService {

    //
    // Private Members
    //

    @OSGiService(timeout = "30 seconds")
    private APSGroupsService apsGroupsService;

    @Managed(loggingFor = "aps-net-time-service-provider")
    private APSLogger logger;

    private GroupMember member = null;

    //
    // Methods
    //

    @BundleStop
    public void shutdown() {
        if (this.member != null) {
            try {
                this.apsGroupsService.leaveGroup(this.member);
            }
            catch (IOException ioe) {
                this.logger.error("Failed to disconnect from service group!", ioe);
            }
        }
    }

    private GroupMember getGroupMember() {
        if (this.member == null) {
            try {
                this.member = this.apsGroupsService.joinGroup("APSNetTimeServiceProvider");
            }
            catch (IOException ioe) {
                this.logger.error("Failed to join service group!", ioe);
            }
        }

        return this.member;
    }

    /**
     * Converts from net time to local time.
     *
     * @param netTime The net time to convert.
     * @return local time.
     */
    @Override
    public long netToLocalTime(long netTime) {
        long localTime = netTime;
        GroupMember groupMember = getGroupMember();
        if (groupMember != null) {
            NetTime netTimeObj = groupMember.createFromNetTime(netTime);
            localTime = netTimeObj.getLocalTimeDate().getTime();
        }
        return localTime;
    }

    /**
     * Converts from net time to local time.
     *
     * @param netTime The net time to convert.
     * @return local time.
     */
    @Override
    public Date netToLocalTime(Date netTime) {
        Date localTime = netTime;
        GroupMember groupMember = getGroupMember();
        if (groupMember != null) {
            NetTime netTimeObj = groupMember.createFromNetTime(netTime);
            localTime = netTimeObj.getLocalTimeDate();
        }
        return localTime;
    }

    /**
     * Converts from local time to net time.
     *
     * @param localTime The local time to convert.
     * @return net time.
     */
    @Override
    public long localToNetTime(long localTime) {
        long netTime = localTime;
        GroupMember groupMember = getGroupMember();
        if (groupMember != null) {
            NetTime netTimeObj = groupMember.createFromLocalTime(localTime);
            netTime = netTimeObj.getNetTime();
        }
        return netTime;
    }

    /**
     * Converts from local time to net time.
     *
     * @param localTime The local time to convert.
     * @return net time.
     */
    @Override
    public Date localToNetTime(Date localTime) {
        Date netTime = localTime;
        GroupMember groupMember = getGroupMember();
        if (groupMember != null) {
            NetTime netTimeObj = groupMember.createFromLocalTime(localTime);
            netTime = netTimeObj.getNetTimeDate();
        }
        return netTime;
    }
}
