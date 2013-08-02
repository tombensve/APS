/* 
 * 
 * PROJECT
 *     Name
 *         APS External Protocol Extender
 *     
 *     Code Version
 *         0.9.2
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
 *         2012-01-01: Created!
 *         
 */
package se.natusoft.osgi.aps.externalprotocolextender.service;

import org.osgi.framework.ServiceReference;
import se.natusoft.osgi.aps.api.external.extprotocolsvc.APSExternalProtocolService;
import se.natusoft.osgi.aps.api.external.extprotocolsvc.model.APSExternalProtocolListener;
import se.natusoft.osgi.aps.api.external.extprotocolsvc.model.APSExternallyCallable;
import se.natusoft.osgi.aps.api.external.extprotocolsvc.model.APSRESTCallable;
import se.natusoft.osgi.aps.api.net.rpc.service.RPCProtocol;
import se.natusoft.osgi.aps.api.net.rpc.service.StreamedRPCProtocol;
import se.natusoft.osgi.aps.externalprotocolextender.model.ProtocolEvent;
import se.natusoft.osgi.aps.externalprotocolextender.model.ServiceDataReason;
import se.natusoft.osgi.aps.externalprotocolextender.model.ServiceRepresentation;
import se.natusoft.osgi.aps.tools.APSLogger;
import se.natusoft.osgi.aps.tools.data.TrivialDataBus;
import se.natusoft.osgi.aps.tools.data.TrivialDataBus.TrivialBusReceivingMember;
import se.natusoft.osgi.aps.tools.data.TrivialDataBus.TrivialManyDataRequest;
import se.natusoft.osgi.aps.tools.data.TrivialDataBus.TrivialSingleDataRequest;
import se.natusoft.osgi.aps.tools.exceptions.APSNoServiceAvailableException;

import java.util.*;

/**
 * Provides an implementation of APSExternalProtocolService.
 */
public class APSExternalProtocolServiceProvider implements APSExternalProtocolService, TrivialBusReceivingMember<ServiceDataReason, Object> {
    //
    // Private Members
    //

    /** Our logger */
    private APSLogger logger;

    /** Holds all currently known services. */
    private Map<String /*service*/, ServiceRepresentation> services = new HashMap<>();
    
    /** Double stored by service reference. */
    private Map<ServiceReference, ServiceRepresentation> servicesByRef = new HashMap<>();

    /** The registered external service listeners. */
    private List<APSExternalProtocolListener> externalServiceListeners = new LinkedList<>();

    /** The bus we will be a member of. */
    private TrivialDataBus bus = null;

    //
    // Constructors
    //

    /**
     * Creates a new APSExternalProtocolServiceProvider.
     * 
     * @param logger The logger to log to.
     */
    public APSExternalProtocolServiceProvider(APSLogger logger) {
        this.logger = logger;
    }

    //
    // Methods
    //

    /**
     * Returns all currently available services.
     */
    @Override
    public Set<String> getAvailableServices() {
        return this.services.keySet();
    }

    /**
     * Returns all APSExternallyCallable for the names service object.
     *
     * @param serviceName The name of the service to get callables for.
     *
     * @throws se.natusoft.osgi.aps.tools.exceptions.APSNoServiceAvailableException
     *          If the service is not available.
     */
    @Override
    public List<APSExternallyCallable> getCallables(String serviceName) throws APSNoServiceAvailableException {
        List<APSExternallyCallable> serviceMethods = new LinkedList<>();
        
        ServiceRepresentation serviceRep = this.services.get(serviceName);
        if (serviceRep != null) {
            for (String methodName : serviceRep.getMethodNames()) {
                serviceMethods.add(new ServiceMethodCallable(serviceRep.getMethodCallable(methodName)));
            }
        }

        return serviceMethods;
    }

    /**
     * Returns true if the service has put*(...), get*(...), and/or delete*(...)
     * methods.
     *
     * @param serviceName The service to check if it has any REST methods.
     */
    @Override
    public boolean isRESTCallable(String serviceName) throws RuntimeException {
        ServiceRepresentation serviceRep = this.services.get(serviceName);
        return serviceRep != null && serviceRep.isRESTCompatible();
    }

    /**
     * Returns an APSRESTCallable containing one or more of post, put.get, and delete
     * methods.
     *
     * @param serviceName The name of the service to get the REST Callables for.
     */
    @Override
    public APSRESTCallable getRESTCallable(String serviceName) {
        ServiceRepresentation serviceRep = this.services.get(serviceName);
        return serviceRep != null ? serviceRep.getRESTCallable() : null;
    }

    /**
     * Returns all available functions of the specified service.
     *
     * @param serviceName The service to get functions for.
     */
    @Override
    public Set<String> getAvailableServiceFunctionNames(String serviceName) {
        ServiceRepresentation serviceRep = this.services.get(serviceName);
        if (serviceRep != null) {
            return serviceRep.getMethodNames();
        }

        return new HashSet<>();
    }

    /**
     * Gets an APSExternallyCallable for a specified service name and service function name.
     *
     * @param serviceName         The name of the service object to get callable for.
     * @param serviceFunctionName The name of the service function of the service object to get callable for.
     *
     * @return An APSExternallyCallable instance or null if the combination of service and serviceFunction is not available.
     */
    @Override
    public APSExternallyCallable getCallable(String serviceName, String serviceFunctionName) {
        ServiceRepresentation serviceRep = this.services.get(serviceName);
        APSExternallyCallable callable = null;

        if (serviceRep != null) {
            callable = new ServiceMethodCallable(serviceRep.getMethodCallable(serviceFunctionName));
        }

        return callable;
    }


    /**
     * @return All currently deployed providers of RPCProtocolService.
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<RPCProtocol> getAllProtocols() {
        TrivialManyDataRequest<RPCProtocol> dataRequest = new TrivialManyDataRequest<>(RPCProtocol.class, null);
        this.bus.sendData(ServiceDataReason.INFORMATION_REQUEST, dataRequest);
        return dataRequest.getData();
    }

    /**
     * Returns an RPCProtocolService provider by protocol name and version.
     *
     * @param name    The name of the protocol to get.
     * @param version The version of the protocol to get.
     *
     * @return Any matching protocol or null if nothing matches.
     */
    @Override
    @SuppressWarnings("unchecked")
    public RPCProtocol getProtocolByNameAndVersion(String name, String version) {
        TrivialSingleDataRequest<RPCProtocol> dataRequest = new TrivialSingleDataRequest<>(RPCProtocol.class, name, version);
        this.bus.sendData(ServiceDataReason.INFORMATION_REQUEST, dataRequest);
        return dataRequest.getData();
    }

    /**
     * @return All currently deployed providers of StreamedRPCProtocolService.
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<StreamedRPCProtocol> getAllStreamedProtocols() {
        TrivialManyDataRequest<StreamedRPCProtocol> dataRequest =
                new TrivialManyDataRequest<>(StreamedRPCProtocol.class, null);
        this.bus.sendData(ServiceDataReason.INFORMATION_REQUEST, dataRequest);
        return dataRequest.getData();
    }

    /**
     * Returns a StreamedRPCProtocolService provider by protocol name and version.
     *
     * @param name    The name of the streamed protocol to get.
     * @param version The version of the streamed protocol to get.
     *
     * @return Any matching protocol or null if nothing matches.
     */
    @Override
    @SuppressWarnings("unchecked")
    public StreamedRPCProtocol getStreamedProtocolByNameAndVersion(String name, String version) {
        TrivialSingleDataRequest<StreamedRPCProtocol> dataRequest =
                new TrivialSingleDataRequest<>(StreamedRPCProtocol.class, name, version);
        this.bus.sendData(ServiceDataReason.INFORMATION_REQUEST, dataRequest);
        return dataRequest.getData();
    }

    /**
     * Add a listener for externally available services.
     *
     * @param externalServiceListener The listener to add.
     */
    @Override
    public synchronized void addExternalProtocolListener(APSExternalProtocolListener externalServiceListener) {
        this.externalServiceListeners.add(externalServiceListener);
        for (String serviceName : this.services.keySet()) {
            externalServiceListener.externalServiceAvailable(serviceName, "0.0.0");
        }
    }

    /**
     * Removes a listener for externally available services.
     *
     * @param externalServiceListener The listener to remove.
     */
    @Override
    public synchronized void removeExternalProtocolListener(APSExternalProtocolListener externalServiceListener) {
        this.externalServiceListeners.remove(externalServiceListener);
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

    /**
     * Data is received on the bus.
     *
     * @param serviceDataReason The reason for the data.
     * @param data            The data received.
     */
    @Override
    public void dataReceived(ServiceDataReason serviceDataReason, Object data) {
        switch (serviceDataReason) {
            case SERVICE_AVAILABLE:
                if (data instanceof ServiceRepresentation) {
                    handleServiceAvailable((ServiceRepresentation)data);
                }
                break;

            case SERVICE_LEAVING:
                if (data instanceof ServiceReference) {
                    handleServiceLeaving((ServiceReference)data);
                }
                break;

            case EVENT:
                if (data instanceof ProtocolEvent) {
                    ProtocolEvent event = (ProtocolEvent)data;
                    for (APSExternalProtocolListener listener : this.externalServiceListeners) {
                        if (event.isAvailable()) {
                            listener.protocolAvailable(event.getName(), event.getVersion());
                        }
                        else {
                            listener.protocolLeaving(event.getName(), event.getVersion());
                        }
                    }
                }
                break;
        }
    }

    /**
     * Handles a received ServiceRepresentation.
     *
     * @param serviceRepresentation The ServiceRepresentation received.
     */
    private void handleServiceAvailable(ServiceRepresentation serviceRepresentation) {
        APSExternalProtocolServiceProvider.this.services.put(serviceRepresentation.getName(), serviceRepresentation);
        APSExternalProtocolServiceProvider.this.servicesByRef.put(serviceRepresentation.getServiceReference(), serviceRepresentation);

        for (APSExternalProtocolListener listener : APSExternalProtocolServiceProvider.this.externalServiceListeners) {
            listener.externalServiceAvailable(serviceRepresentation.getName(), serviceRepresentation.getServiceReference().getBundle().getVersion().toString());
        }

        APSExternalProtocolServiceProvider.this.logger.debug("Received service '" + serviceRepresentation.getName() +
                "' part of bundle '" + serviceRepresentation.getServiceReference().getBundle().getSymbolicName() + "'.");
        for (String methodName : serviceRepresentation.getMethodNames()) {
            APSExternalProtocolServiceProvider.this.logger.debug("    method: " + methodName);
        }
    }

    /**
     * Handles a received ServiceReference.
     *
     * @param serviceReference The ServiceReference received.
     */
    private void handleServiceLeaving(ServiceReference serviceReference) {
        ServiceRepresentation serviceRep = APSExternalProtocolServiceProvider.this.servicesByRef.get(serviceReference);
        if (serviceRep != null) {
            for (APSExternalProtocolListener listener : APSExternalProtocolServiceProvider.this.externalServiceListeners) {
                listener.externalServiceLeaving(serviceRep.getName(), serviceReference.getBundle().getVersion().toString());
            }

            APSExternalProtocolServiceProvider.this.servicesByRef.remove(serviceReference);
            APSExternalProtocolServiceProvider.this.services.remove(serviceRep.getName());

            APSExternalProtocolServiceProvider.this.logger.debug("Removed service '" + serviceReference.toString() +
                    "' part of bundle '" + serviceReference.getBundle().getSymbolicName() + "'.");
        }
    }
}
