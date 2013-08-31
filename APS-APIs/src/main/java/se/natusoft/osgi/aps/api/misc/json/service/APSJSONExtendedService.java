/* 
 * 
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         0.9.3
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
 *         2012-01-17: Created!
 *         
 */
package se.natusoft.osgi.aps.api.misc.json.service;

import se.natusoft.osgi.aps.api.misc.json.JSONErrorHandler;
import se.natusoft.osgi.aps.api.misc.json.model.JSONValue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Provides additional functionality for populating and extracting from JavaBeans. Implementing
 * this part is of course optional. If this is implemented the service has to be registered both
 * as _JSONService_ and _JSONExtendedService_. Clients that need the extended service should ask for
 * that. If the extended service is not needed only _JSONService_ should be asked for. Clients
 * should never ever ask for both since the extended service do extend the base service!
 */
public interface APSJSONExtendedService extends APSJSONService {
    
    /**
     * Reads JSON from an InputStream and produce a populated JavaBean. T
     *
     * @param in The InputStream to read from.
     * @param errorHandler An optional error handler. This can be null in which case all errors are ignored.
     * @param beanType The type of the JavaBean to create, populate and return.
     * @param <T> Auto-resolved JavaBean type from passed class (beanType).
     *
     * @return An instance of the specified bean type.
     *
     * @throws IOException on IO failure.
     */
    public <T> T readJSONToBean(InputStream in, JSONErrorHandler errorHandler, Class<T> beanType) throws IOException;

    /**
     * Writes JSON from a JavaBean instance.
     *
     * @param out The OutputStream to write to.
     * @param bean The JavaBean to write.
     *
     * @throws IOException on IO failure.
     */
    public void writeJSONFromBean(OutputStream out, Object bean) throws IOException;

    /**
     * Takes a _JSONValue_ and converts it to a Java value. You will get one of _String_, _Number_, _Boolean_,
     * _null_, JavaBean or an array of those depending on the _JSONValue_ subclass passed.
     *
     * @param jsonValue The JSONObject whose information should be transferred to the JavaBean.
     * @param javaType The class of the Java type to return a converted instance of.
     *
     * @return A populated JavaBean instance.
     */
    public <T> T jsonToJava(JSONValue jsonValue, Class<T> javaType);

    /**
     * Takes a Java object and converts it to a _JSONValue_ subclass.
     *
     * @param java The Java value to convert to a JSONValue. It can be one of String, Number, Boolean, null, JavaBean, or an array of those.
     *
     * @return A JSONValue subclass.
     */
    public JSONValue javaToJSON(Object java);
}
