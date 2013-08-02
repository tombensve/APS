/* 
 * 
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         0.9.2
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
 *         2011-12-31: Created!
 *         
 */
package se.natusoft.osgi.aps.api.external.extprotocolsvc.model;

import org.osgi.framework.Bundle;
import se.natusoft.osgi.aps.api.external.model.type.DataTypeDescription;
import se.natusoft.osgi.aps.api.external.model.type.ParameterDataTypeDescription;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * This API represents one callable service method.
 */
public interface APSExternallyCallable<ReturnType> extends Callable<ReturnType> {

    /**
     * @return The name of the service this callable is part of.
     */
    public String getServiceName();

    /**
     * @return The name of the service function this callable represents.
     */
    public String getServiceFunctionName();

    /**
     * @return A description of the return type.
     */
    public DataTypeDescription getReturnDataDescription();

    /**
     * @return A description of each parameter type.
     */
    public List<ParameterDataTypeDescription> getParameterDataDescriptions();

    /**
     * @return The bundle the service belongs to.
     */
    public Bundle getServiceBundle();
    
    /**
     * Provides parameters to the callable using a varags list of parameter values.
     *
     * @param value A parameter value.
     */
    public void setArguments(Object... value);

    /**
     * Calls the service method represented by this APSExternallyCallable.
     *
     * @return The return value of the method call if any or null otherwise.
     * @throws Exception Any exception the called service method threw.
     */
    @Override
    ReturnType call() throws Exception;

}
