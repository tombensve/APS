/* 
 * 
 * PROJECT
 *     Name
 *         APS External Protocol Extender
 *     
 *     Code Version
 *         0.10.0
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
 *         2012-01-04: Created!
 *         
 */
package se.natusoft.osgi.aps.externalprotocolextender.service;

import org.osgi.framework.*;
import se.natusoft.osgi.aps.api.core.config.event.APSConfigChangedEvent;
import se.natusoft.osgi.aps.api.core.config.event.APSConfigChangedListener;
import se.natusoft.osgi.aps.api.core.config.service.APSConfigException;
import se.natusoft.osgi.aps.api.core.config.service.APSConfigService;
import se.natusoft.osgi.aps.externalprotocolextender.model.ServiceDataReason;
import se.natusoft.osgi.aps.externalprotocolextender.pub.config.APSExternalProtocolConfig;
import se.natusoft.osgi.aps.externalprotocolextender.pub.config.APSExternalProtocolConfig.ExternalizableService;
import se.natusoft.osgi.aps.tools.APSLogger;
import se.natusoft.osgi.aps.tools.data.TrivialDataBus;
import se.natusoft.osgi.aps.tools.data.TrivialDataBus.TrivialBusMember;

/**
 * This tracks all OSGi framework services coming and going.
 */
public class ExternalizableServiceTracker implements ServiceListener, TrivialBusMember<ServiceDataReason, Object>,
        APSConfigChangedListener {
    //
    // Private Members
    //

    /** The bundle context to use for tracking services. */
    private BundleContext context = null;

    /** The configuration service to get APSExternalProtocolConfig from. */
    private APSConfigService configService = null;

    /** our config. */
    private APSExternalProtocolConfig externalProtocolConfig = null;

    /** The logger to log to. */
    private APSLogger logger = null;

    /** The trivial data bus we are a member of. */
    private TrivialDataBus bus = null;
    
    //
    // Constructors
    //

    /**
     * Creates a new ExternalizableServiceTracker.
     *
     * @param configService The configuration service to get bundle configuration from.
     * @param logger A logger to log to.
     */
    public ExternalizableServiceTracker(APSConfigService configService, APSLogger logger) {
        this.configService = configService;
        this.logger = logger;        
    }
    
    // 
    // Methods
    //

    /**
     * Starts the tracker.
     *
     * @param context A bundle context.
     */
    public void start(BundleContext context) {
        this.context = context;

        this.configService.registerConfiguration(APSExternalProtocolConfig.class, false);
        this.externalProtocolConfig = this.configService.getConfiguration(APSExternalProtocolConfig.class);

        updateAllCurrentServices();

        this.externalProtocolConfig.addConfigChangedListener(this);

        this.context.addServiceListener(this);
    }

    /**
     * Event listener callback when event occurs.
     *
     * @param event information about the event.
     */
    @Override
    public void apsConfigChanged(APSConfigChangedEvent event) {
        updateAllCurrentServices();
    }

    /**
     * Goes through all currently known services and checks if they are externalizable.
     */
    private void updateAllCurrentServices() {
        try {
            for (ServiceReference serviceReference : this.context.getAllServiceReferences(null, null)) {
                if (checkExternalizationPermission(serviceReference)) {
                    this.bus.sendData(ServiceDataReason.SERVICE_AVAILABLE, serviceReference);
                }
            }
        }
        catch (InvalidSyntaxException ise) { /* This is OK! */ }
    }

    /**
     * Stops the tracker.
     *
     * @param context A bundle context.
     */
    public void stop(BundleContext context) {
        context.removeServiceListener(this);
        this.configService.unregisterConfiguration(APSExternalProtocolConfig.class);
    }
    
    /**
     * Receives notification that a service has had a lifecycle change.
     *
     * @param event The <code>ServiceEvent</code> object.
     */
    @Override
    public void serviceChanged(ServiceEvent event) {
        if (event.getType() == ServiceEvent.REGISTERED) {
            if (checkExternalizationPermission(event.getServiceReference())) {
                this.bus.sendData(ServiceDataReason.SERVICE_AVAILABLE, event.getServiceReference());
            }
        }
        else if (event.getType() == ServiceEvent.UNREGISTERING || event.getType() == ServiceEvent.MODIFIED) {
            if (checkExternalizationPermission((event.getServiceReference()))) {
                this.bus.sendData(ServiceDataReason.SERVICE_LEAVING, event.getServiceReference());
            }
        }
    }

    /**
     * Checks permission for a specified service.
     *
     * @param serviceReference The service to check permission for.
     *
     * @return true or false.
     */
    public boolean checkExternalizationPermission(ServiceReference serviceReference) {

        //  First check the service itself if it is externalizable.
        String aps_externalizable = (String)serviceReference.getProperty("aps-externalizable");
        if (aps_externalizable != null && aps_externalizable.toLowerCase().equals("true")) {
            return true;
        }

        // Secondly, check bundle for permission.
        String externalizable = (String)serviceReference.getBundle().getHeaders().get("APS-Externalizable");
        if (externalizable != null) {
            if (externalizable.trim().toLowerCase().equals("true")) {
                return true;
            }
            else if (externalizable.trim().toLowerCase().equals("false")) {
                return false;
            }
        }

        // Thirdly, check if the service is externalizable in configuration.
        boolean permitted = false;
        try {
            Object service = this.context.getService(serviceReference);

            try {
                for (ExternalizableService externalizableService : this.externalProtocolConfig.externalizableServices) {
                    try {
                        Class extSvcClass = serviceReference.getBundle().loadClass(externalizableService.serviceQName.toString().trim());
                        if (extSvcClass.isAssignableFrom(service.getClass())) {
                            permitted = true;
                            break;
                        }
                    }
                    catch (ClassNotFoundException cnfe) {
                        // We will get here if a configured service name is not part of the same bundle that the service
                        // belongs to. But that also means that the service is not implementing the interface and is thus
                        // not a service we are interested in.
                    }
                }
            }
            catch (Exception e) {
                this.logger.error(e.getMessage(), e);
            }
            finally {
                if (service != null) this.context.ungetService(serviceReference);
            }
        }
        catch (APSConfigException ce) {
            this.logger.error("Failed to get configuration for 'APSExternalProtocolConfig'!", ce);
        }

        return permitted;
    }

    /**
     * When a member is added to a bus this is called to receive the bus being added to.
     *
     * @param bus The bus the member now is part of.
     */
    @Override
    public void memberOf(TrivialDataBus<ServiceDataReason, Object> bus) {
        this.bus = bus;
    }
}
