/*
 *
 * PROJECT
 *     Name
 *         APS JSON Service Provider
 *
 *     Code Version
 *         1.0.0
 *
 *     Description
 *         Provides an implementation of aps-apis:se.natusoft.osgi.aps.api.misc.json.service.APSJSONExtendedService
 *         using aps-json-lib as JSON parser/creator.
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
 *         2012-01-22: Created!
 *
 */
package se.natusoft.osgi.aps.json.service;

import org.osgi.framework.Constants;
import se.natusoft.docutations.NotNull;
import se.natusoft.docutations.Nullable;
import se.natusoft.osgi.aps.api.misc.json.JSONEOFException;
import se.natusoft.osgi.aps.api.misc.json.JSONErrorHandler;
import se.natusoft.osgi.aps.api.misc.json.model.*;
import se.natusoft.osgi.aps.api.misc.json.service.APSJSONExtendedService;
import se.natusoft.osgi.aps.api.misc.json.service.APSJSONService;
import se.natusoft.osgi.aps.api.reactive.APSHandler;
import se.natusoft.osgi.aps.api.reactive.APSResult;
import se.natusoft.osgi.aps.api.reactive.APSValue;
import se.natusoft.osgi.aps.constants.APS;
import se.natusoft.osgi.aps.exceptions.APSIOException;
import se.natusoft.osgi.aps.json.*;
import se.natusoft.osgi.aps.json.tools.JSONMapConv;
import se.natusoft.osgi.aps.json.tools.JSONToJava;
import se.natusoft.osgi.aps.json.tools.JavaToJSON;
import se.natusoft.osgi.aps.tools.APSLogger;
import se.natusoft.osgi.aps.tools.annotation.activator.Managed;
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiProperty;
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiServiceInstance;
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiServiceProvider;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * Provides an implementation of APSJSONService & APSJSONExtendedService.
 */
@SuppressWarnings({"unchecked", "unused"}) // This class is instantiated and manged by APSActivator.
@OSGiServiceProvider(
        instances = {
                @OSGiServiceInstance(
                        serviceAPIs = APSJSONService.class,
                        properties = {
                                @OSGiProperty(name = Constants.SERVICE_PID, value = "APSJSONService"),
                                @OSGiProperty(name = APS.Service.Provider, value = "aps-json-service-provider"),
                                @OSGiProperty(name = APS.Service.Category, value = APS.Value.Service.Category.Misc),
                                @OSGiProperty(name = APS.Service.Function, value = APS.Value.Service.Function.JSON)
                        }
                ),
                @OSGiServiceInstance(
                        serviceAPIs = APSJSONExtendedService.class,
                        properties = {
                                @OSGiProperty(name = Constants.SERVICE_PID, value = "APSJSONExtendedService"),
                                @OSGiProperty(name = APS.Service.Provider, value = "aps-json-service-provider"),
                                @OSGiProperty(name = APS.Service.Category, value = APS.Value.Service.Category.Misc),
                                @OSGiProperty(name = APS.Service.Function, value = APS.Value.Service.Function.JSON)
                        }
                )
        }
)
public class APSJSONServiceProvider implements APSJSONExtendedService {
    //
    // Private Members
    //

    @Managed(loggingFor = "aps-json-service-provider")
    private APSLogger logger = null; // IDEA complains 'field logger is never assigned' if not set to null!
                                     // This is not true since this field gets injected. In this specific
                                     // case however IDEA does not let me ignore the warning!

    //
    // Constructors
    //

    /**
     * Creates a new JSONService instance.
     */
    public APSJSONServiceProvider() {
    }

    //
    // JSONService Implementation
    //

    /**
     * @return a JSONObject.
     */
    @Override
    public JSONObject createJSONObject() {
        return new JSONObjectProvider();
    }

    /**
     * @param value The value of the created JSONString.
     * @return a JSONString.
     */
    @Override
    public JSONString createJSONString(String value) {
        return new JSONStringProvider(value);
    }

    /**
     * @param value The numeric value of the created JSONNumber.
     * @return a JSONNumber
     */
    @Override
    public JSONNumber createJSONNumber(Number value) {
        return new JSONNumberProvider(value);
    }

    /**
     * @return a JSONNull.
     */
    @Override
    public JSONNull createJSONNull() {
        return new JSONNullProvider();
    }

    /**
     * @param value The boolean value of the created JSONBoolean.
     * @return a JSONBoolean.
     */
    @Override
    public JSONBoolean createJSONBoolean(Boolean value) {
        return new JSONBooleanProvider(value);
    }

    /**
     * @return a JSONArray.
     */
    @Override
    public JSONArray createJSONArray() {
        return new JSONArrayProvider();
    }

    /**
     * Reads JSON from an InputStream producing most probably a JSONObject.
     *
     * @param in            The stream to read from.
     * @param resultHandler This will be called with the result.
     */
    @Override
    public void readJSON(@NotNull InputStream in, @NotNull APSHandler<APSResult<JSONValue>> resultHandler) {
        JSON.read(in, resultHandler);
    }

    /**
     * Reads JSON from an InputStream producing a `Map<String, Object>`.
     *
     * @param in            The stream to read from. *Must* be a JSON object! Does not support a sub JSON structure.
     * @param resultHandler This will be called with the result.
     */
    @Override
    public void readJSONObject(@NotNull InputStream in, @NotNull APSHandler<APSResult<Map<String, Object>>> resultHandler) {

        Map<String, Object> map = null;
        try {
            map = JSONMapConv.jsonObjectToMap(in, new JSONErrorHandler() {
                public void warning(String message) {
                    logger.warn(message);
                }

                public void fail(String message, Throwable cause) throws RuntimeException {
                    throw new APSIOException(message, cause);
                }
            });
        }
        catch (APSIOException ioe) {
            resultHandler.handle(APSResult.failure(ioe));
        }

        resultHandler.handle(APSResult.successj(new APSValue.Provider(map)));
    }

    /**
     * Writes a JSONValue to an OutputStream in compact format.
     *
     * @param out           The stream to write to.
     * @param jsonValue     The value to write.
     * @param resultHandler This wil be called with the result. Only success() and failure() are relevant here!
     */
    @Override
    public void writeJSON(@NotNull OutputStream out, @NotNull JSONValue jsonValue, @Nullable APSHandler<APSResult<Void>> resultHandler) {

        JSON.write(out, jsonValue, resultHandler);
    }

    /**
     * Writes a JSONValue to an OutputStream.
     *
     * @param out           The stream to write to.
     * @param jsonValue     The value to write.
     * @param compact       If true then the output is compact and hard to read, if false then the output is easy to read and larger
     *                      with indents.
     * @param resultHandler This wil be called with the result. Only success() and failure() are relevant here!
     */
    @Override
    public void writeJSON(@NotNull OutputStream out, @NotNull JSONValue jsonValue, boolean compact,
                          @Nullable APSHandler<APSResult<Void>> resultHandler)
            throws APSIOException {

        JSON.write(out, jsonValue, compact, resultHandler);
    }

    /**
     * Writes a JSON _Map_ to an _OutputStream_.
     *
     * @param out     The output stream to write to.
     * @param jsonMap The Map to write.
     * @throws APSIOException on IO failure.
     */
    @Override
    public void writeJSONObject(@NotNull OutputStream out, @NotNull Map<String, Object> jsonMap,
                                @Nullable APSHandler<APSResult<Void>> resultHandler) throws APSIOException {

        writeJSON(out, JSONMapConv.mapToJSONObject(jsonMap), resultHandler);
    }

    /**
     * Converts a JSONObject into a `Map<String, Object>`. This supports working with a JSON structure using a standard
     * java.util.Map. This works well in languages like Groovy.
     *
     * @param jsonObject The JSONObject to convert.
     * @return A Map containing the same structure as the JSONObject.
     */
    @Override
    public Map<String, Object> toMap(JSONObject jsonObject) {
        return JSONMapConv.jsonObjectToMap(jsonObject);
    }

    /**
     * Converts a `Map<String, Object>` into a JSONObject. This supports working with a JSON structure using a standard
     * java.util.Map. This works well in languages like Groovy.
     *
     * @param jsonMap The map to convert. Expects a JSON compatible structure!
     * @return The converted to JSONObject.
     */
    @Override
    public JSONObject toJSONObject(Map<String, Object> jsonMap) {
        return JSONMapConv.mapToJSONObject(jsonMap);
    }

    //
    // JSONExtendedService Implementation
    //

    /**
     * Reads JSON from an InputStream and produce a populated JavaBean. T
     *
     * @param in           The InputStream to read from.
     * @param errorHandler An optional error handler. This can be null in which case all errors are ignored.
     * @param beanType     The type of the JavaBean to create, populate and return.
     * @param <T>          Autoresolved JavaBean type from passed class (beanType).
     * @return An instance of the specified bean type.
     * @throws APSIOException on IO failure.
     */
    @Override
    public <T> T readJSONToBean(InputStream in, JSONErrorHandler errorHandler, Class<T> beanType) throws APSIOException {
        try {

            JSONObjectProvider obj = new JSONObjectProvider(errorHandler);
            obj.readJSON(in);

            return JSONToJava.convert(obj, beanType);

        } catch (JSONEOFException eofe) {

            throw new se.natusoft.osgi.aps.api.misc.json.JSONEOFException();
        }
    }

    /**
     * Writes JSON from a JavaBean instance.
     *
     * @param out  The OutputStream to write to.
     * @param bean The JavaBean to write.
     * @throws APSIOException on IO failure.
     */
    @Override
    public void writeJSONFromBean(OutputStream out, Object bean) throws APSIOException {
        JavaToJSON.convertObject(bean).writeJSON(out);
    }

    /**
     * Takes a JSONValue and converts it to a Java value. You will get one of String, Number, Boolean, null, JavaBean or an array of those
     * depending on the JSONValue subclass passed.
     *
     * @param jsonValue The JSONObject whose information should be transferred to the JavaBean.
     * @param javaType  The class of the Java type to return a converted instance of.
     * @return A populated JavaBean instance.
     */
    @Override
    public <T> T jsonToJava(JSONValue jsonValue, Class<T> javaType) {
        return JSONToJava.convert(jsonValue, javaType);
    }

    /**
     * Takes a Java object and converts it to a JSONValue subclass.
     *
     * @param java The Java value to convert to a JSONValue. It can be one of String, Number, Boolean, null, JavaBean, or an array of those.
     * @return A JSONValue subclass.
     */
    @Override
    public JSONValue javaToJSON(Object java) {
        return JavaToJSON.convertValue(java);
    }

}
