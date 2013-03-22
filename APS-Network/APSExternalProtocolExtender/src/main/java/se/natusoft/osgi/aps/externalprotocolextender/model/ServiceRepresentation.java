/* 
 * 
 * PROJECT
 *     Name
 *         APS External Protocol Extender
 *     
 *     Code Version
 *         0.9.1
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
package se.natusoft.osgi.aps.externalprotocolextender.model;

import org.osgi.framework.ServiceReference;
import se.natusoft.osgi.aps.api.external.extprotocolsvc.model.APSRESTCallable;
import se.natusoft.osgi.aps.api.external.model.type.DataType;
import se.natusoft.osgi.aps.api.external.model.type.ParameterDataTypeDescription;
import se.natusoft.osgi.aps.externalprotocolextender.service.APSRESTCallableImpl;
import se.natusoft.osgi.aps.externalprotocolextender.service.ServiceMethodCallable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This class represents one service.
 */
public class ServiceRepresentation {
    //
    // Private Members
    //

    /** The name of the service. */
    private String name;

    /** The reference to the service. */
    private ServiceReference serviceReference;

    /** The callable methods of the service. */
    private Map<String, ServiceMethodCallable> methods = new HashMap<>();

    /** The uniquely callable methods with arg types. */
    private Map<String, ServiceMethodCallable> methodsUnique = new HashMap<>();
    
    /** If this is a REST compatible service this will also be set. */
    private APSRESTCallable restCallable = null;
    
    /** For cacheing rest status. */
    private Boolean isREST = null;
    
    //
    // Constructors
    //

    /**
     * Creates a new ServiceRepresentation.
     *
     * @param name The name of the represented service.
     * @param serviceReference The reference to the service.
     */
    public ServiceRepresentation(String name, ServiceReference serviceReference) {
        this.name = name;
        this.serviceReference = serviceReference;
    }
    
    //
    // Methods
    //

    /**
     * Returns the name of the represented service. 
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the reference to the service.
     */
    public ServiceReference getServiceReference() {
        return this.serviceReference;
    }

    /**
     * Returns the names of the avalable methods. 
     */
    public Set<String> getMethodNames() {
        return this.methodsUnique.keySet();
    }

    /**
     * Returns an APSExternallyCallable for a specified method.
     *
     * @param method The method to get callable for.
     */
    public ServiceMethodCallable getMethodCallable(String method) {
        ServiceMethodCallable callable = this.methodsUnique.get(method);
        if (callable == null) {
            callable = this.methods.get(method);
        }

        return callable;
    }

    /**
     * Adds a method callable to the service representation.
     *
     * @param method The name of the method to add callable for.
     * @param callable The callable for the named method.
     */
    public void addMethodCallable(String method, ServiceMethodCallable callable) {
        this.methods.put(method, callable);

        StringBuilder methodName = new StringBuilder(method);
        methodName.append('(');
        String comma = "";
        for (ParameterDataTypeDescription param : callable.getParameterDataDescriptions()) {
            methodName.append(comma);
            if (param.getDataType() == DataType.OBJECT) {
                methodName.append(param.getDataTypeClass().getSimpleName());
            }
            else {
                methodName.append(param.getDataType().getTypeName());
            }
            comma = ",";
        }
        methodName.append(')');
        this.methodsUnique.put(methodName.toString(), callable);
    }

    /**
     * Returns true if the service represented by this object is REST compatible, that is having at least one method starting
     * with one of "_post_", "_put_", "_get_", or "_delete_".
     *
     * @return true/false.
     */
    public boolean isRESTCompatible() {
    	if (this.isREST == null) {
    		ServiceMethodCallable post = null;
    		ServiceMethodCallable put = null;
    		ServiceMethodCallable get = null;
    		ServiceMethodCallable delete = null;
    		boolean validREST = false;
    		
    		for (String methodName : this.methods.keySet()) {
    			methodName = methodName.toLowerCase();
    			// Please mote that there can only be one of each method type.
    			// If there are more than one of any method type we say that
    			// this is not REST compatible.
    			if (methodName.startsWith("post")) {
    				
    				if (post == null) {
    					validREST = true;
    					post = this.methods.get(methodName);
    				}
    				else {
    					validREST = false;
    					break;
    				}
    			}
    			else if (methodName.startsWith("put")) {
    				
    				if (put == null) {
    					validREST = true;
    					put = this.methods.get(methodName);
    				}
    				else {
    					validREST = false;
    					break;
    				}
    			}
    			else if (methodName.startsWith("get")) {
    				
    				if (get == null) {
    					validREST = true;
    					put = this.methods.get(methodName);
    				}
    				else {
    					validREST = false;
    					break;
    				}
    			}
    			else if (methodName.startsWith("delete")) {
    				
    				if (delete == null) {
    					validREST = true;
    					put = this.methods.get(methodName);
    				}
    				else {
    					validREST = false;
    					break;
    				}
    			}
    		}
    		
    		if (validREST) {
    			this.isREST = true;
    			APSRESTCallableImpl rc = new APSRESTCallableImpl();
    			APSRESTCallableImpl.Input input = rc.input();
    			input.providePost(post);
                input.providePut(put);
                input.provideGet(get);
                input.provideDelete(delete);
                this.restCallable = rc;
    		}
    		else {
    			this.isREST = false;
    		}
    	}

        return this.isREST;
    }

    /**
     * @return Returns an APSRestCallable if any or null otherwise.
     */
    public APSRESTCallable getRESTCallable() {
        return this.restCallable;
    }
}
