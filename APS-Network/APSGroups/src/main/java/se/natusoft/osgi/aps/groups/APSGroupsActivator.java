/* 
 * 
 * PROJECT
 *     Name
 *         APSGroups
 *     
 *     Code Version
 *         0.9.0
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
 *         are configurable.
 *         
 *         Note that even though this is an OSGi bundle, the jar produced can also be used as a
 *         library outside of OSGi. The se.natusoft.apsgroups.APSGroups API should then be used.
 *         This API has no external dependencies, only this jar is required for that use.
 *         
 *         When run with java -jar a for test command line shell will run where you can check
 *         members, send messages and files and other things.
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
import se.natusoft.apsgroups.internal.net.MulticastTransport;
import se.natusoft.apsgroups.internal.net.Transport;
import se.natusoft.apsgroups.internal.protocol.DataReceiverThread;
import se.natusoft.apsgroups.internal.protocol.MemberManagerThread;
import se.natusoft.apsgroups.internal.protocol.NetTime;
import se.natusoft.apsgroups.internal.protocol.NetTimeThread;
import se.natusoft.apsgroups.logging.APSGroupsLogger;
import se.natusoft.osgi.aps.api.net.groups.service.APSGroupsService;
import se.natusoft.osgi.aps.groups.config.APSGroupsConfigRelay;
import se.natusoft.osgi.aps.groups.logging.APSGroupsLoggerRelay;
import se.natusoft.osgi.aps.groups.service.APSGroupsServiceProvider;
import se.natusoft.osgi.aps.tools.APSLogger;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Properties;

public class APSGroupsActivator implements BundleActivator {
    //
    // Private Members
    //

    // Required Services

    
    // Provided Services
    
    /** The platform service. */
    private ServiceRegistration groupsServiceReg = null;

    // Other Members

    /** Manages members. */
    private MemberManagerThread memberManagerThread = null;

    /** Handles receiving of all network packets. */
    private DataReceiverThread dataReceiverThread = null;

    /** Manages net time with other groups on possible other hosts. */
    private NetTimeThread netTimeThread = null;

    /** This instance holds our copy of net time. This gets updated by the NetTimeThread. */
    private NetTime netTime = null;

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

        connect();

        Dictionary platformServiceProps = new Properties();
        platformServiceProps.put(Constants.SERVICE_PID, APSGroupsServiceProvider.class.getName());
        APSGroupsServiceProvider groupsServiceProvider =
                new APSGroupsServiceProvider(memberManagerThread, dataReceiverThread, netTime, logger);
        this.groupsServiceReg = context.registerService(APSGroupsService.class.getName(), groupsServiceProvider, platformServiceProps);
    }

    //
    // Bundle Stop.
    //
    
    @Override
    public void stop(BundleContext context) throws Exception {
        if (this.groupsServiceReg != null) {
            try {
                this.groupsServiceReg.unregister();
                this.groupsServiceReg = null;
            }
            catch (IllegalStateException ise) { /* This is OK! */ }
        }

        disconnect();

        if (this.logger != null) {
            ((APSGroupsLoggerRelay)this.logger).getLogger().stop(context);
            this.logger = null;
        }
    }

    //
    // Support
    //

    /**
     * Starts upp and connects the groups engine. Nothing will work until this has been called and
     * should therefore be the first thing called after construction of instance.
     *
     * @throws java.io.IOException on failure to connect.
     */
    private void connect() throws IOException {
        Transport transport = new MulticastTransport(this.logger, this.config);
        transport.open();
        this.dataReceiverThread = new DataReceiverThread(this.logger, transport);
        this.dataReceiverThread.start();

        Transport memberTransport = new MulticastTransport(this.logger, this.config);
        memberTransport.open();
        this.memberManagerThread = new MemberManagerThread(this.logger, memberTransport, this.config);
        this.memberManagerThread.start();

        Transport netTimeTransport = new MulticastTransport(this.logger, this.config);
        netTimeTransport.open();
        this.netTime = new NetTime();
        this.netTimeThread = new NetTimeThread(this.netTime, this.logger, netTimeTransport);
        this.dataReceiverThread.addMessagePacketListener(this.netTimeThread);
        this.netTimeThread.start();

    }

    /**
     * Shuts down and disconnects the groups engine. After this call the functionality is dead until
     * connect() is called again.
     *
     * @throws IOException
     */
    private void disconnect()  {
        try {
            if (this.dataReceiverThread != null) {
                this.dataReceiverThread.terminate();
                this.dataReceiverThread.getTransport().close();
            }

            if (this.memberManagerThread != null) {
                this.memberManagerThread.terminate();
                this.memberManagerThread.getTransport().close();
            }

            if (this.netTimeThread != null) {
                this.netTimeThread.terminate();
                this.netTimeThread.getTransport().close();
            }
        }
        catch (IOException ioe) {
            if (this.logger != null) {
                this.logger.error("Failed to disconnect!", ioe);
            }
        }
    }

}
