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
 *         2012-02-02: Created!
 *         
 */
package se.natusoft.osgi.aps.externalprotocolextender.service;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import se.natusoft.osgi.aps.api.net.rpc.service.RPCProtocol;
import se.natusoft.osgi.aps.api.net.rpc.service.StreamedHTTPProtocol;
import se.natusoft.osgi.aps.api.net.rpc.service.StreamedRPCProtocol;
import se.natusoft.osgi.aps.exceptions.APSRuntimeException;
import se.natusoft.osgi.aps.externalprotocolextender.model.*;
import se.natusoft.osgi.aps.tools.data.TrivialDataBus;
import se.natusoft.osgi.aps.tools.data.TrivialDataBus.TrivialBusReceivingMember;
import se.natusoft.osgi.aps.tools.data.TrivialDataBus.TrivialManyDataRequest;
import se.natusoft.osgi.aps.tools.data.TrivialDataBus.TrivialSingleDataRequest;
import se.natusoft.osgi.aps.tools.tracker.OnServiceAvailable;
import se.natusoft.osgi.aps.tools.tracker.OnServiceLeaving;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Tracks StreamedRPCProtocolService service providers and stores them keyed on protocol name and version.
 * <p/>
 * This class must be used in conjunction with an APSServiceTracker<StreamedRPCProtocolService>.
 */
public class RPCProtocolProviderTracker implements
        OnServiceAvailable<RPCProtocol>, OnServiceLeaving<RPCProtocol>, TrivialBusReceivingMember<ServiceDataReason, Object> {
    //
    // Private Members
    //

    /** The context of our bundle. */
    private BundleContext context = null;
    
    /**
     * The RPC services keyed on a string that is a combination of name and version.
     */
    private Map<String /*name and version*/, RPCProtocolWrapper> rpcProtocolByProtocolAndVersion = new HashMap<String, RPCProtocolWrapper>();

    /**
     * The RPC services keyed on their ServiceReference.
     */
    private Map<ServiceReference, String /*name and version*/> rpcProtocolByServiceReference = new HashMap<ServiceReference, String>();

    /**
     * The RPC services keyed on a string that is a combination of name and version.
     */
    private Map<String /*name and version*/, StreamedRPCProtocolWrapper> rpcStreamedProtocolByProtocolAndVersion =
            new HashMap<String, StreamedRPCProtocolWrapper>();

    /**
     * The RPC services keyed on their ServiceReference.
     */
    private Map<ServiceReference, String /*name and version*/> rpcStreamedProtocolByServiceReference =
            new HashMap<ServiceReference, String>();

    /** The trivial data bus we are a member of. */
    private TrivialDataBus<ServiceDataReason, Object> bus = null;

    //
    // Constructors
    //

    /**
     * Creates a new RPCServiceTracker instance.
     */
    public RPCProtocolProviderTracker(BundleContext context) {
        this.context = context;
    }

    //
    // Methods
    //

    /**
     * Sends new protocol available event to listeners.
     *
     * @param name The name of the new protocol to announce.
     * @param version The version of the new protocol to announce.
     */
    private void sendProtocolAvailableEvent(String name, String version) {
        this.bus.sendData(ServiceDataReason.EVENT, new ProtocolEvent(true, name, version));
    }

    /**
     * Sends protocol leaving event to listeners.
     *
     * @param name The name of the leaving protocol.
     * @param version The version of the leaving protocol.
     */
    private void sendProtocolLeavingEvent(String name, String version) {
        this.bus.sendData(ServiceDataReason.EVENT, new ProtocolEvent(false, name, version));
    }
    
    /**
     * Receives a new service.
     *
     * @param service          The received service.
     * @param serviceReference The reference to the received service.
     *
     * @throws Exception Implementation can throw any exception. How it is handled depends on the APSServiceTracker method this
     *                   gets passed to.
     */
    @Override
    public synchronized void onServiceAvailable(RPCProtocol service, ServiceReference serviceReference) throws Exception {
        Object protocol = this.context.getService(serviceReference);
        try {

            if (protocol instanceof StreamedHTTPProtocol) {
                StreamedHTTPProtocolWrapper protocolWrapper = new StreamedHTTPProtocolWrapper(this.context, serviceReference,
                        (StreamedRPCProtocol)protocol);

                this.rpcStreamedProtocolByProtocolAndVersion.put(
                        protocolWrapper.getServiceProtocolName() + ":" + protocolWrapper.getServiceProtocolVersion(),
                        protocolWrapper
                );

                this.rpcStreamedProtocolByServiceReference.put(
                        serviceReference,
                        protocolWrapper.getServiceProtocolName() + ":" + protocolWrapper.getServiceProtocolVersion()
                );

                sendProtocolAvailableEvent(protocolWrapper.getServiceProtocolName(), protocolWrapper.getServiceProtocolVersion());
            }
            else if (protocol instanceof StreamedRPCProtocol) {
                StreamedRPCProtocolWrapper protocolWrapper = new StreamedRPCProtocolWrapper(this.context, serviceReference, (StreamedRPCProtocol)protocol);

                this.rpcStreamedProtocolByProtocolAndVersion.put(
                        protocolWrapper.getServiceProtocolName() + ":" + protocolWrapper.getServiceProtocolVersion(),
                        protocolWrapper
                );

                this.rpcStreamedProtocolByServiceReference.put(
                        serviceReference,
                        protocolWrapper.getServiceProtocolName() + ":" + protocolWrapper.getServiceProtocolVersion()
                );

                sendProtocolAvailableEvent(protocolWrapper.getServiceProtocolName(), protocolWrapper.getServiceProtocolVersion());
            }
            else {
                RPCProtocolWrapper protocolWrapper = new RPCProtocolWrapper(this.context, serviceReference, (RPCProtocol)protocol);

                this.rpcProtocolByProtocolAndVersion.put(
                        protocolWrapper.getServiceProtocolName() + ":" + protocolWrapper.getServiceProtocolVersion(),
                        protocolWrapper
                );

                this.rpcProtocolByServiceReference.put(
                        serviceReference,
                        protocolWrapper.getServiceProtocolName() + ":" + protocolWrapper.getServiceProtocolVersion()
                );
                
                sendProtocolAvailableEvent(protocolWrapper.getServiceProtocolName(), protocolWrapper.getServiceProtocolVersion());
            }
        }
        finally {
            this.context.ungetService(serviceReference);
        }
    }

    /**
     * A service is leaving.
     *
     * @param serviceReference The leaving service. Please note that this can only be used for information! Dont try to get a service with it!
     * @param serviceAPI       The service API (interface) class for this service for more easy identification.
     *
     * @throws Exception Implementation can throw any exception. How it is handled depends on the APSServiceTracker method this
     *                   gets passed to.
     */
    @Override
    public synchronized void onServiceLeaving(ServiceReference serviceReference, Class serviceAPI) throws Exception {
        String nameAndVersion = null;
        if (this.rpcStreamedProtocolByServiceReference.containsKey(serviceReference)) {
            nameAndVersion = this.rpcStreamedProtocolByServiceReference.remove(serviceReference);
            this.rpcStreamedProtocolByProtocolAndVersion.remove(nameAndVersion);
        }
        else {
            nameAndVersion = this.rpcProtocolByServiceReference.remove(serviceReference);
            this.rpcProtocolByProtocolAndVersion.remove(nameAndVersion);
        }
        
        String[] nameAndVersionParts = nameAndVersion.split(":");
        if (nameAndVersionParts.length == 2) {
            sendProtocolLeavingEvent(nameAndVersionParts[0], nameAndVersionParts[1]);
        }
        else {
            throw new APSRuntimeException("BUG: 'nameAndVersion' split into " + nameAndVersionParts.length + " parts. Expected 2!");
        }
    }

    /**
     * Checks if an RPC protocol is available for a specific protocol and version.
     *
     * @param protocol The name of the RPC protocol.
     * @param version  The version of the RPC protocol.
     *
     * @return true if service is available, false otherwise.
     */
    public boolean hasRPCProtocol(String protocol, String version) {
        return getRPCProtocol(protocol, version) != null;
    }

    /**
     * Looks up an RPC protocol provider for the specified protocol and version.
     *
     * @param protocol The name of the RPC protocol.
     * @param version  The version of the RPC protocol.
     *
     * @return An RPC service implementation or null if not available.
     */
    public RPCProtocol getRPCProtocol(String protocol, String version) {
        String protocolAndVersion = protocol + ":" + version;
        return this.rpcProtocolByProtocolAndVersion.get(protocolAndVersion);
    }

    /**
     * @return All currently available protocols.
     */
    public List<RPCProtocol> getAllRPCProtocols() {
        List<RPCProtocol> services = new LinkedList<RPCProtocol>();
        for (String key : this.rpcProtocolByProtocolAndVersion.keySet()) {
            services.add(this.rpcProtocolByProtocolAndVersion.get(key));
        }
        return services;
    }

    /**
     * Checks if a streamed RPC protocol is available for a specific protocol and version.
     *
     * @param protocol The name of the RPC protocol.
     * @param version  The version of the RPC protocol.
     *
     * @return true if service is available, false otherwise.
     */
    public boolean hasStreamedRPCProtocol(String protocol, String version) {
        return getStreamedRPCProtocol(protocol, version) != null;
    }

    /**
     * Looks up a streamed RPC protocol provider for the specified protocol and version.
     *
     * @param protocol The name of the RPC protocol.
     * @param version  The version of the RPC protocol.
     *
     * @return An RPC service implementation or null if not available.
     */
    public StreamedRPCProtocol getStreamedRPCProtocol(String protocol, String version) {
        String protocolAndVersion = protocol + ":" + version;
        return this.rpcStreamedProtocolByProtocolAndVersion.get(protocolAndVersion);
    }

    /**
     * @return All currently available streamed protocols.
     */
    public List<StreamedRPCProtocol> getAllStreamedRPCProtocols() {
        List<StreamedRPCProtocol> services = new LinkedList<StreamedRPCProtocol>();
        for (String key : this.rpcStreamedProtocolByProtocolAndVersion.keySet()) {
            services.add(this.rpcStreamedProtocolByProtocolAndVersion.get(key));
        }
        return services;
    }

    /**
     * Data is received on the bus.
     *
     * @param serviceDataReason The reason for the data received.
     * @param data            The data received.
     */
    @Override
    public synchronized void dataReceived(ServiceDataReason serviceDataReason, Object data) {
        switch (serviceDataReason) {
            case INFORMATION_REQUEST:
                if (data instanceof TrivialSingleDataRequest) {
                    TrivialSingleDataRequest dataRequest = (TrivialSingleDataRequest)data;
                    
                    Object[] queryData = dataRequest.getQueryData();
    
                    if (queryData != null && queryData.length >= 2) {
                        if (StreamedRPCProtocol.class.isAssignableFrom(dataRequest.getRequestedDataType())) {
                            dataRequest.setData(getStreamedRPCProtocol(queryData[0].toString(), queryData[1].toString()));
                        }
                        else if (RPCProtocol.class.isAssignableFrom(dataRequest.getRequestedDataType())) {
                            dataRequest.setData(getRPCProtocol(queryData[0].toString(), queryData[1].toString()));
                        }
                    }
                }
                else if (data instanceof TrivialManyDataRequest) {
                    TrivialManyDataRequest dataRequest = (TrivialManyDataRequest)data;
    
                    if (StreamedRPCProtocol.class.isAssignableFrom(dataRequest.getRequestedDataType())) {
                        dataRequest.setData(getAllStreamedRPCProtocols());
                    }
                    else if (RPCProtocol.class.isAssignableFrom(dataRequest.getRequestedDataType())) {
                        dataRequest.setData(getAllRPCProtocols());
                    }
                }
                break;
        }
        
    }

    /**
     * When a member is added to a bus this is called to receive the bus being added to.
     *
     * @param bus The bus the member now is part of.
     */
    @Override
    public void memberOf(TrivialDataBus bus) {
        this.bus = bus;
    }
}
