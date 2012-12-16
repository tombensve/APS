/* 
 * 
 * PROJECT
 *     Name
 *         APS External Protocol Extender
 *     
 *     Code Version
 *         1.0.0
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
 *         2012-01-02: Created!
 *         
 */
package se.natusoft.osgi.aps.externalprotocolextender.service;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import se.natusoft.osgi.aps.api.external.extprotocolsvc.model.APSExternallyCallable;
import se.natusoft.osgi.aps.api.external.model.type.DataTypeDescription;
import se.natusoft.osgi.aps.api.external.model.type.ParameterDataTypeDescription;
import se.natusoft.osgi.aps.exceptions.APSNoServiceAvailableException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

/**
 * Provides an implementation of APSExternallyCallable.
 */
public class ServiceMethodCallable implements APSExternallyCallable {
    //
    // Private Members
    //
    
    /** The name of the service this method belongs to. */
    private String serviceName = null;
    
    /** The method to call. */
    private Method method = null;
    
    /** The reference to the service to call. */
    private ServiceReference serviceReference = null;
    
    /** A bundle context to lookup service instance with. */
    private BundleContext bundleContext = null;
    
    /** The arguments to the call. */
    private Object[] arguments = null;

    /** Description of return data. */
    private DataTypeDescription returnDataDescription = null;

    /** Descriptions of parameter data. */
    private List<ParameterDataTypeDescription> parameterDataDescriptions = new LinkedList<ParameterDataTypeDescription>();

    /** The bundle the service this callable method belongs to. */
    private Bundle serviceBundle;
    
    //
    // Constructors
    //

    /**
     * Creates a new ServiceMethodCallable.
     *
     * @param serviceName The name of the service this method belongs to.
     * @param method The method to call.
     * @param serviceReference the reference to the service to call.
     * @param bundleContext The bundle context to lookup service instance with.
     * @param serviceBundle The bundle the service of this callable method belongs to.
     */
    public ServiceMethodCallable(String serviceName, Method method, ServiceReference serviceReference, BundleContext bundleContext, Bundle serviceBundle) {
        this.serviceName = serviceName;
        this.method = method;
        this.serviceReference = serviceReference;
        this.bundleContext = bundleContext;
        this.serviceBundle = serviceBundle;
    }
    
    //
    // Methods
    //
    
    /**
     * @return The name of the service this callable is part of.
     */
    @Override
    public String getServiceName() {
        return this.serviceName;
    }

    /**
     * @return The name of the service function this callable represents.
     */
    @Override
    public String getServiceFunctionName() {
        return this.method.getName();
    }

    /**
     * Sets a description of the return data.
     * 
     * @param returnDataDescription The return data description to set.
     */
    public void setReturnDataDescription(DataTypeDescription returnDataDescription) {
        this.returnDataDescription = returnDataDescription;
    }
    /**
     * @return A description of the return type.
     */
    @Override
    public DataTypeDescription getReturnDataDescription() {
        return this.returnDataDescription;
    }

    /**
     * Adds a data description of a paramter.
     *
     * @param parameterDataDescription The description to add.
     */
    public void addParamterDataDescription(ParameterDataTypeDescription parameterDataDescription) {
        this.parameterDataDescriptions.add(parameterDataDescription);
    }
    
    /**
     * @return A description of each parameter type.
     */
    @Override
    public List<ParameterDataTypeDescription> getParameterDataDescriptions() {
        return this.parameterDataDescriptions;
    }

    /**
     * @return The bundle the service of this callable method belongs to.
     */
    @Override
    public Bundle getServiceBundle() {
        return this.serviceBundle;
    }

    /**
     * Provides parameters to the callable using a varags list of parameter values.
     *
     * @param value A parameter value.
     */
    @Override
    public void setArguments(Object... value) {
        this.arguments = value;
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     *
     * @throws se.natusoft.osgi.aps.exceptions.APSNoServiceAvailableException
     *                   This is thrown if the service represented by this object
     *                   have gone away between the time you got this instance and
     *                   the call to this method. This is a runtime exception!
     * @throws Exception if unable to compute a result.
     */
    @Override
    public Object call() throws Exception {
        Object service = this.bundleContext.getService(this.serviceReference);
        if (service == null) {
            throw new APSNoServiceAvailableException("Service '" + this.serviceName + "' is no longer available!");
        }

        try {
            return this.method.invoke(service, this.arguments);
        }
        catch (InvocationTargetException ite) {
            try {
                throw (Exception)ite.getCause();
            }
            catch (ClassCastException cce) {
                throw ite;
            }
        }
        finally {
            try {
                this.bundleContext.ungetService(this.serviceReference);
            }
            catch (Exception e) {/* If it fails, it fails, but we at least tried. */}
        }
    }
}
