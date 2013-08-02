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
 *         2012-01-02: Created!
 *         
 */
package se.natusoft.osgi.aps.externalprotocolextender.service;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import se.natusoft.osgi.aps.annotations.APSServiceAPI;
import se.natusoft.osgi.aps.api.external.model.type.DataType;
import se.natusoft.osgi.aps.api.external.model.type.DataTypeDescription;
import se.natusoft.osgi.aps.api.external.model.type.ParameterDataTypeDescription;
import se.natusoft.osgi.aps.externalprotocolextender.model.ServiceDataReason;
import se.natusoft.osgi.aps.externalprotocolextender.model.ServiceRepresentation;
import se.natusoft.osgi.aps.tools.data.TrivialDataBus;
import se.natusoft.osgi.aps.tools.data.TrivialDataBus.TrivialBusReceivingMember;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class resolves the callable methods of each service. It creates APSExternallyCallable
 * implementations for each service method found. It also tries to provide an as good as possible
 * description of return and parameter types which is provided through the APSExternallyCallable
 * implementation.
 */
public class ServiceReferenceToServiceRepresentationConverter implements TrivialBusReceivingMember<ServiceDataReason, Object> {
    //
    // Private Members
    //

    /** The bundle context used to lookup services with. */
    private BundleContext bundleContext = null;
    
    /** The trivial data bus we are a member of. */
    private TrivialDataBus bus = null;

    /** Saved copies of already parsed and created descriptions. */
    private Map<Class, DataTypeDescription> classDescriptions = new HashMap<Class, DataTypeDescription>();

    //
    // Constructors
    //

    /**
     * Creates a new ExternalizableServicesTracker instance.
     *
     * @param bundleContext The bundle context for looking up services.
     */
    public ServiceReferenceToServiceRepresentationConverter(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }
    
    //
    // Methods
    //

    /**
     * Updates an empty description with information form the specified class.
     * 
     * @param dataDescription The description to update.
     * @param clazz The class to get information from.
     *
     * @return The passed dataDescription or a created one if the passed was null.
     */
    private DataTypeDescription createClassDescription(DataTypeDescription dataDescription, Class clazz) {
        DataTypeDescription existingDescription = this.classDescriptions.get(clazz);
        if (existingDescription != null) {
            if (dataDescription != null) {
                dataDescription.copyFrom(existingDescription);
                existingDescription = dataDescription;
            }

            return existingDescription;
        }

        if (dataDescription == null) {
            dataDescription = new DataTypeDescription();
        }

        if (clazz == void.class) {
            dataDescription.setDataType(DataType.VOID);
        }
        else if (clazz == Boolean.class || clazz == boolean.class) {
            dataDescription.setDataType(DataType.BOOLEAN);
            dataDescription.setObjectQName(java.lang.Boolean.class.getName());
        }
        else if (clazz == Byte.class || clazz == byte.class) {
            dataDescription.setDataType(DataType.BYTE);
            dataDescription.setObjectQName(java.lang.Byte.class.getName());
        }
        else if (clazz == Character.class || clazz == char.class) {
            dataDescription.setDataType(DataType.CHAR);
            dataDescription.setObjectQName(java.lang.Character.class.getName());
        }
        else if (clazz == Short.class || clazz == short.class) {
            dataDescription.setDataType(DataType.SHORT);
            dataDescription.setObjectQName(java.lang.Short.class.getName());
        }
        else if (clazz == Integer.class || clazz == int.class) {
            dataDescription.setDataType(DataType.INT);
            dataDescription.setObjectQName(java.lang.Integer.class.getName());
        }
        else if (clazz == Long.class || clazz == long.class) {
            dataDescription.setDataType(DataType.LONG);
            dataDescription.setObjectQName(java.lang.Long.class.getName());
        }
        else if (clazz == Float.class || clazz == float.class) {
            dataDescription.setDataType(DataType.FLOAT);
            dataDescription.setObjectQName(java.lang.Float.class.getName());
        }
        else if (clazz == Double.class || clazz == double.class) {
            dataDescription.setDataType(DataType.DOUBLE);
            dataDescription.setObjectQName(java.lang.Double.class.getName());
        }
        else if (clazz == String.class) {
            dataDescription.setDataType(DataType.STRING);
            dataDescription.setObjectQName(java.lang.String.class.getName());
        }
        else if (List.class.isAssignableFrom(clazz)) {
            dataDescription.setDataType(DataType.LIST);
            dataDescription.setObjectQName(java.util.LinkedList.class.getName());
        }
        else if (Map.class.isAssignableFrom(clazz)) {
            dataDescription.setDataType(DataType.MAP);
            dataDescription.setObjectQName(java.util.HashMap.class.getName());
        }
        else {
            dataDescription.setDataType(DataType.OBJECT);
            dataDescription.setObjectQName(clazz.getName());
            if (!clazz.equals(Object.class)) {
                // We have to store this description already here since it is fully possible
                // for the class to have a getter that returns an instance of the same type as this!
                // Thereby we avoid a never ending recursion!
                this.classDescriptions.put(clazz, dataDescription);

                for (Method method : clazz.getDeclaredMethods()) {
                    if (Modifier.isPublic(method.getModifiers())) {
                        if (method.getName().startsWith("is")) {
                            String name = method.getName();
                            if (name.length() > 2) {
                                name = name.substring(2);
                                String name2 = name.substring(0, 1).toLowerCase();
                                if (name2.length() > 0) name2 = name2 + name.substring(1);
                                DataTypeDescription memberDataDescription = createClassDescription(null, method.getReturnType());
                                memberDataDescription.setOwner(dataDescription);
                                dataDescription.addMember(name2, memberDataDescription);
                            }
                        }
                        else if (method.getName().startsWith("get")) {
                            String name = method.getName();
                            if (name.length() > 3) {
                                name = name.substring(3);
                                String name2 = name.substring(0, 1).toLowerCase();
                                if (name2.length() > 0) name2 = name2 + name.substring(1);
                                DataTypeDescription memberDataDescription = createClassDescription(null, method.getReturnType());
                                memberDataDescription.setOwner(dataDescription);
                                dataDescription.addMember(name2, memberDataDescription);
                            }
                        }
                    }
                }
            }
        }

        this.classDescriptions.put(clazz, dataDescription);

        return dataDescription;
    }


    /**
     * Converts a service reference to a service representation.
     *
     * @param serviceRef The service reference to convert.
     *
     * @return A ServiceRepresentation or null if not convertable.
     */
    private ServiceRepresentation serviceRefToServiceRep(ServiceReference serviceRef) {
        Object service = this.bundleContext.getService(serviceRef);

        ServiceRepresentation serviceRep = null;

        if (service.getClass().getInterfaces() != null && service.getClass().getInterfaces().length >= 1) {
            String serviceAPIName = null;
            Class serviceAPIClass = null;

            for (Class svcAPI : service.getClass().getInterfaces()) {
                if (svcAPI.getAnnotation(APSServiceAPI.class) != null) {
                    serviceAPIName = svcAPI.getName();
                    serviceAPIClass = svcAPI;
                    break;
                }
            }

            if (serviceAPIName == null) {
                serviceAPIName = service.getClass().getInterfaces()[0].getName();
                serviceAPIClass = service.getClass().getInterfaces()[0];
            }

            serviceRep = new ServiceRepresentation(serviceAPIName, serviceRef);
            for (Method method : serviceAPIClass.getDeclaredMethods()) {
                if (Modifier.isPublic(method.getModifiers())) {
                    ServiceMethodCallable serviceMethodCallable =
                            new ServiceMethodCallable(serviceRep.getName(), method, serviceRef, this.bundleContext, serviceRef.getBundle());

                    serviceMethodCallable.setReturnDataDescription(createClassDescription(null, method.getReturnType()));

                    int paramPos = 0;
                    for (Class parameterType : method.getParameterTypes()) {
                        ParameterDataTypeDescription parameterDataDescription = new ParameterDataTypeDescription();
                        parameterDataDescription.setPosition(paramPos++);
                        createClassDescription(parameterDataDescription, parameterType);
                        parameterDataDescription.setDataTypeClass(parameterType);

                        serviceMethodCallable.addParamterDataDescription(parameterDataDescription);
                    }

                    serviceRep.addMethodCallable(method.getName(), serviceMethodCallable);
                }
            }
        }

        this.bundleContext.ungetService(serviceRef);

        return serviceRep;
    }

    /**
     * Receives an event.
     *
     * @param serviceEventType The type of the event.
     * @param data The data of the event.
     */
    @Override
    public void dataReceived(ServiceDataReason serviceEventType, Object data) {
        if (data instanceof ServiceReference) {
            ServiceReference serviceReference = (ServiceReference)data;

            if (serviceEventType == ServiceDataReason.SERVICE_AVAILABLE) {
                ServiceRepresentation serviceRep = serviceRefToServiceRep(serviceReference);
                if (serviceRep != null) {
                    this.bus.sendData(ServiceDataReason.SERVICE_AVAILABLE, serviceRep);
                }
            }
        }
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
