/* 
 * 
 * PROJECT
 *     Name
 *         APS Groups
 *     
 *     Code Version
 *         0.11.0
 *     
 *     Description
 *         Provides network groups where named groups can be joined as members and then send and
 *         receive data messages to the group. This is based on multicast and provides a verified
 *         multicast delivery with acknowledgements of receive to the sender and resends if needed.
 *         The sender will get an exception if not all members receive all data. Member actuality
 *         is handled by members announcing themselves relatively often and will be removed when
 *         an announcement does not come in expected time. So if a member dies unexpectedly
 *         (network goes down, etc) its membership will resolve rather quickly. Members also
 *         tries to inform the group when they are doing a controlled exit. Most network aspects
 *         are configurable. Please note that this does not support streaming! That would require
 *         a far more complex protocol. It waits in all packets of a message before delivering
 *         the message.
 *         
 *         Note that even though this is an OSGi bundle, the jar produced can also be used as a
 *         library outside of OSGi. The se.natusoft.apsgroups.APSGroups API should then be used.
 *         This API has no external dependencies, only this jar is required for that use.
 *         
 *         When run with java -jar a for test command line shell will run where you can check
 *         members, send messages and files.
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
 *     Tommy Svensson (tommy@natusoft.se)
 *         Changes:
 *         2012-12-28: Created!
 *         
 */
package se.natusoft.osgi.aps.groups;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import se.natusoft.apsgroups.config.APSGroupsConfig;
import se.natusoft.apsgroups.internal.protocol.DataReceiverThread;
import se.natusoft.apsgroups.internal.protocol.MemberManagerThread;
import se.natusoft.apsgroups.logging.APSGroupsLogger;
import se.natusoft.osgi.aps.api.net.groups.service.APSGroupsInfoService;
import se.natusoft.osgi.aps.api.net.groups.service.APSGroupsService;
import se.natusoft.osgi.aps.groups.config.APSGroupsConfigRelay;
import se.natusoft.osgi.aps.groups.logging.APSGroupsLoggerRelay;
import se.natusoft.osgi.aps.groups.service.APSGroupsInfoServiceProvider;
import se.natusoft.osgi.aps.groups.service.APSGroupsServiceProvider;
import se.natusoft.osgi.aps.tools.APSLogger;

import java.util.Dictionary;
import java.util.Properties;

public class APSGroupsActivator implements BundleActivator {
    //
    // Private Members
    //

    // Required Services

    
    // Provided Services
    
    /** The APSGroupsService registration. */
    private ServiceRegistration groupsServiceReg = null;

    /** The APSGroupsInfoService registration. */
    private ServiceRegistration groupsInfoServiceReg = null;

    /** The APSGroupsService provider instance. */
    private APSGroupsServiceProvider apsGroupsServiceProvider = null;

    /** The APSGroupsInfoService provider instance. */
    private APSGroupsInfoServiceProvider apsGroupsInfoServiceProvider = null;

    // Other Members

    /** For logging. */
    private APSGroupsLogger logger = null;

    /** Our config. */
    private APSGroupsConfig config = null;

    //
    // Bundle Start.
    //
    
    @Override
    public void start(BundleContext context) throws Exception {
        APSLogger apsLogger = new APSLogger(System.out);
        apsLogger.setLoggingFor("APSGroups");
        apsLogger.start(context);
        this.logger = new APSGroupsLoggerRelay(apsLogger);

        this.config = new APSGroupsConfigRelay();

        DataReceiverThread.init( this.logger, this.config);

        MemberManagerThread.init(this.logger, this.config);

        Dictionary serviceProps = new Properties();
        serviceProps.put(Constants.SERVICE_PID, APSGroupsServiceProvider.class.getName());
        this.apsGroupsServiceProvider =
                new APSGroupsServiceProvider(this.config, this.logger);
        this.groupsServiceReg = context.registerService(
                APSGroupsService.class.getName(),
                this.apsGroupsServiceProvider,
                serviceProps
        );

        Dictionary infoServiceProps = new Properties();
        serviceProps.put(Constants.SERVICE_PID, APSGroupsInfoServiceProvider.class.getName());
        this.apsGroupsInfoServiceProvider =
                new APSGroupsInfoServiceProvider(this.config);
        this.groupsInfoServiceReg = context.registerService(
                APSGroupsInfoService.class.getName(),
                this.apsGroupsInfoServiceProvider,
                infoServiceProps
        );
    }

    //
    // Bundle Stop.
    //
    
    @Override
    public void stop(BundleContext context) throws Exception {
        this.apsGroupsServiceProvider.disconnect();

        if (this.groupsServiceReg != null) {
            try {
                this.groupsServiceReg.unregister();
                this.groupsServiceReg = null;
            }
            catch (IllegalStateException ise) { /* This is OK! */ }
            this.apsGroupsServiceProvider = null;
        }

        if (this.groupsInfoServiceReg != null) {
            this.groupsInfoServiceReg.unregister();
            this.groupsInfoServiceReg = null;
            this.apsGroupsInfoServiceProvider = null;
        }

        if (this.logger != null) {
            ((APSGroupsLoggerRelay)this.logger).getLogger().stop(context);
            this.logger = null;
        }

        try {
            DataReceiverThread dataReceiverThread = (DataReceiverThread)DataReceiverThread.get();
            if (dataReceiverThread != null) {
                dataReceiverThread.terminate();
                dataReceiverThread.join(3000);
            }
        }
        finally {
            MemberManagerThread memberManagerThread = MemberManagerThread.get();
            if (memberManagerThread != null) {
                memberManagerThread.terminate();

                memberManagerThread.join(3000);
            }
        }

    }

}
