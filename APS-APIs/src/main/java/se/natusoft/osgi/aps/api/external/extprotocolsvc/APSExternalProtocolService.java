/* 
 * 
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         0.9.0
 *     
 *     Description
 *         Provides the APIs for the application platform services.
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
package se.natusoft.osgi.aps.api.external.extprotocolsvc;

import se.natusoft.osgi.aps.api.external.extprotocolsvc.model.APSExternalProtocolListener;
import se.natusoft.osgi.aps.api.external.extprotocolsvc.model.APSExternallyCallable;
import se.natusoft.osgi.aps.api.net.rpc.service.RPCProtocol;
import se.natusoft.osgi.aps.api.net.rpc.streamed.service.StreamedRPCProtocol;
import se.natusoft.osgi.aps.exceptions.APSNoServiceAvailableException;

import java.util.List;
import java.util.Set;

/**
 * This service makes the currently available externalizable services available for calling. It should be used by
 * a bundle providing an externally available way of calling a service (JSON over http for example) to translate
 * and forward calls to the local service. The locally called service is not required to be aware that it is called
 * externally.
 * <p/>
 * Never cache any result of this service! Always make a new call to get the current state. Also note that it is
 * possible that the service represented by an APSExternallyCallable have gone away after it was returned, but
 * before you do call() on it! In that case an APSNoServiceAvailableException will be thrown.
 * <p/>
 * <img src="doc-files/APSExternalProtocolExtender.png" width="800" height="400"/>
 *
 */
public interface APSExternalProtocolService {

    /**
     * Returns all currently available services.
     */
    public Set<String> getAvailableServices();
    
    /**
     * Returns all APSExternallyCallable for the names service object.
     *
     * @param serviceName The name of the service to get callables for.
     *
     * @throws APSNoServiceAvailableException If the service is not available.
     */
    public List<APSExternallyCallable> getCallable(String serviceName) throws APSNoServiceAvailableException;

    /**
     * Returns all available functions of the specified service.
     *
     * @param serviceName The service to get functions for.
     */
    public Set<String> getAvailableServiceFunctions(String serviceName);
    
    /**
     * Gets an APSExternallyCallable for a specified service name and service function name.
     *
     * @param serviceName The name of the service object to get callable for.
     * @param serviceFunctionName The name of the service function of the service object to get callable for.
     *
     * @return An APSExternallyCallable instance or null if the combination of service and serviceFunction is not available.
     */
    public APSExternallyCallable getCallable(String serviceName, String serviceFunctionName);

    /**
     * @return All currently deployed providers of RPCProtocolService.
     */
    public List<RPCProtocol> getAllProtocols();

    /**
     * Returns an RPCProtocolService provider by protocol name and version.
     *
     * @param name The name of the protocol to get.
     * @param version The version of the protocol to get.
     *
     * @return Any matching protocol or null if nothing matches.
     */
    public RPCProtocol getProtocolByNameAndVersion(String name, String version);

    /**
     * @return All currently deployed providers of StreamedRPCProtocolService.
     */
    public List<StreamedRPCProtocol> getAllStreamedProtocols();

    /**
     * Returns a StreamedRPCProtocolService provider by protocol name and version.
     *
     * @param name The name of the streamed protocol to get.
     * @param version The version of the streamed protocol to get.
     *
     * @return Any matching protocol or null if nothing matches.
     */
    public StreamedRPCProtocol getStreamedProtocolByNameAndVersion(String name, String version);
    
    /**
     * Add a listener for externally available services.
     *
     * @param externalServiceListener The listener to add.
     */
    public void addExternalProtocolListener(APSExternalProtocolListener externalServiceListener);

    /**
     * Removes a listener for externally available services.
     *
     * @param externalServiceListener The listener to remove.
     */
    public void removeExternalProtocolListener(APSExternalProtocolListener externalServiceListener);
}
