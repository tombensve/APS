/* 
 * 
 * PROJECT
 *     Name
 *         APS External Protocol Extender
 *     
 *     Code Version
 *         0.11.0
 *     
 *     Description
 *         This does two things:
 *         
 *         1) Looks for "APS-Externalizable: true" MANIFEST.MF entry in deployed bundles and if found and bundle status is
 *         ACTIVE, analyzes the service API and creates an APSExternallyCallable wrapper for each service method and
 *         keeps them in memory until bundle state is no longer ACTIVE. In addition to the MANIFEST.MF entry it has
 *         a configuration of fully qualified service names that are matched against the bundles registered services
 *         for which an APSExternallyCallable wrapper will be created.
 *         
 *         2) Registers an APSExternalProtocolExtenderService making the APSExternallyCallable objects handled available
 *         to be called. Note that APSExternallyCallable is an interface extending java.util.concurrent.Callable.
 *         This service is used by other bundles making the service available remotely trough some protocol like
 *         JSON for example.
 *         
 *         This extender is a middleman making access to services very easy to expose using whatever protocol you want.
 *         Multiple protocol bundles using the APSExternalProtocolExtenderService can be deployed at the same time making
 *         services available through more than one protocol.
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
 *         2011-12-31: Created!
 *         
 */
package se.natusoft.osgi.aps.externalprotocolextender;

import org.osgi.framework.*;
import se.natusoft.osgi.aps.api.core.config.service.APSConfigService;
import se.natusoft.osgi.aps.api.external.extprotocolsvc.APSExternalProtocolService;
import se.natusoft.osgi.aps.api.net.rpc.service.RPCProtocol;
import se.natusoft.osgi.aps.api.net.rpc.service.StreamedRPCProtocol;
import se.natusoft.osgi.aps.externalprotocolextender.model.ServiceDataReason;
import se.natusoft.osgi.aps.externalprotocolextender.pub.config.APSExternalProtocolConfig;
import se.natusoft.osgi.aps.externalprotocolextender.service.APSExternalProtocolServiceProvider;
import se.natusoft.osgi.aps.externalprotocolextender.service.ExternalizableServiceTracker;
import se.natusoft.osgi.aps.externalprotocolextender.service.RPCProtocolProviderTracker;
import se.natusoft.osgi.aps.externalprotocolextender.service.ServiceReferenceToServiceRepresentationConverter;
import se.natusoft.osgi.aps.tools.APSLogger;
import se.natusoft.osgi.aps.tools.APSServiceTracker;
import se.natusoft.osgi.aps.tools.data.TrivialDataBus;
import se.natusoft.osgi.aps.tools.tracker.OnServiceAvailable;
import se.natusoft.osgi.aps.tools.tracker.OnServiceLeaving;

import java.util.Properties;

/**
 * aps-external-protocol-extender activator.
 */
public class APSExternalProtocolExtenderActivator implements BundleActivator {
    //
    // Private Members
    //

    // Required Services

    /** Tracker for the APS configuration service. */
    private APSServiceTracker<APSConfigService> configServiceTracker = null;

    // Provided Services
    
    /** The platform service. */
    private ServiceRegistration externalProtocolServiceReg = null;

    // Other Members

    /** Our bundle context. */
    private BundleContext context = null;

    /** Tracks all framework services. */
    private ExternalizableServiceTracker externalizableServiceTracker = null;
    
    /** A tracker for RPCProtocolService providers. */
    private APSServiceTracker<RPCProtocol> rpcProtocolTracker = null;
    
    /** A tracker for StreamedProtocolService providers. */
    private APSServiceTracker<StreamedRPCProtocol> rpcStreamedProtocolTracker = null;
    
    /** Tracks all RPC protocol providers. */
    private RPCProtocolProviderTracker rpcProtocolProviderTracker = null;

    /** Our logger. */
    private APSLogger logger = null;

    //
    // Bundle Start.
    //
    
    @Override
    public void start(BundleContext context) throws Exception {
        this.context = context;

        this.logger = new APSLogger(System.out);
        this.logger.setLoggingFor("aps-external-protocol-extender");
        this.logger.start(context);

        // Setup dependent services. Please note one very important thing here: Three different service trackers
        // are created here with large timeouts. start() and stop() has to be quick and can't hang waiting for
        // to long, which is why you should not try to call services within a start() or stop() method! In
        // this case none of the tracked services are actually called from this start() method! The start()
        // methods on the other classes called within this start() method have the same restriction: no service
        // calls!
        this.configServiceTracker = new APSServiceTracker<APSConfigService>(context, APSConfigService.class, APSServiceTracker.LARGE_TIMEOUT);

        this.configServiceTracker.onActiveServiceAvailable(new OnServiceAvailable<APSConfigService>() {
            @Override
            public void onServiceAvailable(APSConfigService configService, ServiceReference serviceReference) throws Exception {
                startup(configService);
            }
        });

        this.configServiceTracker.onActiveServiceLeaving( new OnServiceLeaving<APSConfigService>() {
            @Override
            public void onServiceLeaving(ServiceReference service, Class serviceAPI) throws Exception {
                stop(APSExternalProtocolExtenderActivator.this.context);
            }
        });

        this.configServiceTracker.start();
    }

    private void startup(APSConfigService configService) throws Exception {
        // Setup the different parts of the service implementation. They all communicate over a TrivialDataBus.
        TrivialDataBus<ServiceDataReason, Object> trivialDataBus = new TrivialDataBus<ServiceDataReason, Object>();

        APSExternalProtocolServiceProvider apsExternalProtocolServiceProvider = new APSExternalProtocolServiceProvider(this.logger);
        trivialDataBus.addMember(apsExternalProtocolServiceProvider);

        ServiceReferenceToServiceRepresentationConverter serviceRefToServiceRepConverter = new ServiceReferenceToServiceRepresentationConverter(context);
        trivialDataBus.addMember(serviceRefToServiceRepConverter);

        this.externalizableServiceTracker = new ExternalizableServiceTracker(configService, this.logger);
        trivialDataBus.addMember(this.externalizableServiceTracker);
        this.externalizableServiceTracker.start(this.context);

        this.rpcProtocolTracker = new APSServiceTracker<RPCProtocol>(this.context, RPCProtocol.class, APSServiceTracker.LARGE_TIMEOUT);
        this.rpcStreamedProtocolTracker =
                new APSServiceTracker<StreamedRPCProtocol>(this.context, StreamedRPCProtocol.class, APSServiceTracker.LARGE_TIMEOUT);
        this.rpcProtocolProviderTracker = new RPCProtocolProviderTracker(this.context);
        trivialDataBus.addMember(this.rpcProtocolProviderTracker);
        this.rpcProtocolTracker.onServiceAvailable(this.rpcProtocolProviderTracker);
        this.rpcProtocolTracker.onServiceLeaving(this.rpcProtocolProviderTracker);
        this.rpcStreamedProtocolTracker.onServiceAvailable(this.rpcProtocolProviderTracker);
        this.rpcStreamedProtocolTracker.onServiceLeaving(this.rpcProtocolProviderTracker);
        this.rpcProtocolTracker.start();
        this.rpcStreamedProtocolTracker.start();

        // Register our service.
        Properties platformServiceProps = new Properties();
        platformServiceProps.put(Constants.SERVICE_PID, APSExternalProtocolService.class.getName());
        this.externalProtocolServiceReg =
                this.context.registerService(APSExternalProtocolService.class.getName(), apsExternalProtocolServiceProvider, platformServiceProps);

    }

    //
    // Bundle Stop.
    //
    
    @Override
    public void stop(BundleContext context) throws Exception {
        if (this.configServiceTracker != null) {
            this.configServiceTracker.setTimeout(APSServiceTracker.SHORT_TIMEOUT);
        }
        if (this.externalProtocolServiceReg != null) {
            try {
                this.externalProtocolServiceReg.unregister();
            }
            catch (IllegalStateException ise) { /* This is OK! */ }
            this.externalProtocolServiceReg = null;
        }

        if (this.rpcProtocolTracker != null) {
            this.rpcProtocolTracker.stop(context);
            this.rpcProtocolTracker = null;
        }

        if (this.rpcStreamedProtocolTracker != null) {
            this.rpcStreamedProtocolTracker.setTimeout(APSServiceTracker.SHORT_TIMEOUT);
            this.rpcStreamedProtocolTracker.stop(context);
            this.rpcStreamedProtocolTracker = null;
        }

        this.rpcProtocolProviderTracker = null;

        // This is not an APSServiceTracker!
        if (this.externalizableServiceTracker != null) {
            this.externalizableServiceTracker.stop(context);
            this.externalizableServiceTracker = null;
        }
        
        if (this.configServiceTracker != null) {
            if (this.configServiceTracker.hasTrackedService()) {
                try {
                    APSConfigService configService = this.configServiceTracker.allocateService();
                    configService.unregisterConfiguration(APSExternalProtocolConfig.class);
                    this.configServiceTracker.releaseService();
                }
                catch (RuntimeException re) {}
            }

            this.configServiceTracker.stop(context);
            this.configServiceTracker = null;
        }

        if (this.logger != null) {
            this.logger.stop(context);
            this.logger = null;
        }

    }

}
