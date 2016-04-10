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

import se.natusoft.osgi.aps.api.misc.json.JSONErrorHandler;
import se.natusoft.osgi.aps.api.misc.json.model.*;
import se.natusoft.osgi.aps.api.misc.json.service.APSJSONExtendedService;
import se.natusoft.osgi.aps.json.JSON;
import se.natusoft.osgi.aps.json.JSONEOFException;
import se.natusoft.osgi.aps.json.model.*;
import se.natusoft.osgi.aps.json.tools.JSONToJava;
import se.natusoft.osgi.aps.json.tools.JavaToJSON;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Provides an implementation of JSONExtendedService.
 */
@SuppressWarnings("unchecked")
public class APSJSONServiceProvider implements APSJSONExtendedService {
    //
    // Private Members
    //

    //
    // Constructors
    //

    /**
     * Creates a new JSONService instance.
     */
    public APSJSONServiceProvider() {}

    //
    // JSONService Implementation
    //

    /**
     * @return a JSONObject.
     */
    @Override
    public JSONObject createJSONObject() {
        return new JSONObjectModel();
    }

    /**
     * @param value The value of the created JSONString.
     *
     * @return a JSONString.
     */
    @Override
    public JSONString createJSONString(String value) {
        return new JSONStringModel(value);
    }

    /**
     * @param value The numeric value of the created JSONNumber.
     *
     * @return a JSONNumber
     */
    @Override
    public JSONNumber createJSONNumber(Number value) {
        return new JSONNumberModel(value);
    }

    /**
     * @return a JSONNull.
     */
    @Override
    public JSONNull createJSONNull() {
        return new JSONNullModel();
    }

    /**
     * @param value The boolean value of the created JSONBoolean.
     *
     * @return a JSONBoolean.
     */
    @Override
    public JSONBoolean createJSONBoolean(Boolean value) {
        return new JSONBooleanModel(value);
    }

    /**
     * @return a JSONArray.
     */
    @Override
    public JSONArray createJSONArray() {
        return new JSONArrayModel();
    }

    /**
     * Reads JSON from an InputStream producing most probably a JSONObject.
     *
     * @param in           The stream to read from.
     * @param errorHandler An optional error handler. This can be null in which case all errors are ignored.
     *
     * @return A JSONObject.
     *
     * @throws IOException on IO Failure.
     */
    @Override
    public JSONValue readJSON(InputStream in, JSONErrorHandler errorHandler) throws IOException {
        JSONErrorHandlerProxy errorHandlerProxy = new JSONErrorHandlerProxy(errorHandler);
        try {
            se.natusoft.osgi.aps.json.JSONValue jv = JSON.read(in, errorHandlerProxy);

            JSONValue value = null;

            if (jv instanceof se.natusoft.osgi.aps.json.JSONArray) {
                value = new JSONArrayModel((se.natusoft.osgi.aps.json.JSONArray)jv);
            }
            else if (jv instanceof se.natusoft.osgi.aps.json.JSONObject) {
                value = new JSONObjectModel((se.natusoft.osgi.aps.json.JSONObject)jv);
            }
            else if (jv instanceof se.natusoft.osgi.aps.json.JSONBoolean) {
                value = new JSONBooleanModel((se.natusoft.osgi.aps.json.JSONBoolean)jv);
            }
            else if (jv instanceof se.natusoft.osgi.aps.json.JSONNull) {
                value = new JSONNullModel((se.natusoft.osgi.aps.json.JSONNull)jv);
            }
            else if (jv instanceof se.natusoft.osgi.aps.json.JSONNumber) {
                value = new JSONNumberModel((se.natusoft.osgi.aps.json.JSONNumber)jv);
            }
            else if (jv instanceof se.natusoft.osgi.aps.json.JSONString) {
                value = new JSONStringModel((se.natusoft.osgi.aps.json.JSONString)jv);
            }

            return value;
        }
        catch (JSONEOFException eofe) {
            throw new se.natusoft.osgi.aps.api.misc.json.JSONEOFException();
        }
    }

    /**
     * Writes a JSONValue to an OutputStream in compact format.
     *
     * @param out       The stream to write to.
     * @param jsonValue The value to write.
     *
     * @throws java.io.IOException on IO failure.
     */
    @Override
    public void writeJSON(OutputStream out, JSONValue jsonValue) throws IOException {
        JSON.write(out, ((JSONModel<se.natusoft.osgi.aps.json.JSONValue>)jsonValue).getAggregated());
    }

    /**
     * Writes a JSONValue to an OutputStream.
     *
     * @param out       The stream to write to.
     * @param jsonValue The value to write.
     * @param compact   If true then the output is compact and hard to read, if false then the output is easy to read and larger with indents.
     *
     * @throws java.io.IOException on IO failure.
     */
    @Override
    public void writeJSON(OutputStream out, JSONValue jsonValue, boolean compact) throws IOException {
        JSON.write(out, ((JSONModel<se.natusoft.osgi.aps.json.JSONValue>)jsonValue).getAggregated(), compact);
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
     *
     * @return An instance of the specified bean type.
     *
     * @throws IOException on IO failure.
     */
    @Override
    public <T> T readJSONToBean(InputStream in, JSONErrorHandler errorHandler, Class<T> beanType) throws IOException {
        try {
            JSONErrorHandlerProxy errorHandlerProxy = new JSONErrorHandlerProxy(errorHandler);
            se.natusoft.osgi.aps.json.JSONObject obj = new se.natusoft.osgi.aps.json.JSONObject(errorHandlerProxy);
            obj.readJSON(in);
            return JSONToJava.convert(obj, beanType);
        }
        catch (JSONEOFException eofe) {
            throw new se.natusoft.osgi.aps.api.misc.json.JSONEOFException();
        }
    }

    /**
     * Writes JSON from a JavaBean instance.
     *
     * @param out          The OutputStream to write to.
     * @param bean         The JavaBean to write.
     *
     * @throws IOException on IO failure.
     */
    @Override
    public void writeJSONFromBean(OutputStream out, Object bean) throws IOException {
        JavaToJSON.convertObject(bean).writeJSON(out);
    }

    /**
     * Takes a JSONValue and converts it to a Java value. You will get one of String, Number, Boolean, null, JavaBean or an array of those
     * depending on the JSONValue subclass passed.
     *
     * @param jsonValue The JSONObject whose information should be transferred to the JavaBean.
     * @param javaType The class of the Java type to return a converted instance of.
     *
     * @return A populated JavaBean instance.
     */
    @Override
    public <T> T jsonToJava(JSONValue jsonValue, Class<T> javaType) {
        return JSONToJava.convert(((JSONModel<se.natusoft.osgi.aps.json.JSONValue>)jsonValue).getAggregated(), javaType);
    }

    /**
     * Takes a Java object and converts it to a JSONValue subclass.
     *
     * @param java The Java value to convert to a JSONValue. It can be one of String, Number, Boolean, null, JavaBean, or an array of those.
     *
     * @return A JSONValue subclass.
     */
    @Override
    public JSONValue javaToJSON(Object java) {
        return JSONModel.convertLibValue(JavaToJSON.convertValue(java));
    }

}
