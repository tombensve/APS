/*
 *
 * PROJECT
 *     Name
 *         APS JSON Library
 *
 *     Code Version
 *         1.0.0
 *
 *     Description
 *         Provides a JSON parser and creator. Please note that this bundle has no dependencies to any
 *         other APS bundle! It can be used as is without APS in any Java application and OSGi container.
 *         The reason for this is that I do use it elsewhere and don't want to keep 2 different copies of
 *         the code. OSGi wise this is a library. All packages are exported and no activator nor services
 *         are provided.
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
 *         2012-01-16: Created!
 *
 */
package se.natusoft.osgi.aps.json.tools;

import se.natusoft.osgi.aps.json.*;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.time.temporal.Temporal;
import java.util.*;

/**
 * Takes a JavaBean and produces a JSONObject.
 */
@SuppressWarnings("WeakerAccess")
public class JavaToJSON {

    /**
     * Converts a JavaBean object into a _JSONObject_.
     *
     * @param javaBean The JavaBean object to convert.
     *
     * @return A JSONObject containing all values from the JavaBean.
     *
     * @throws JSONConvertionException on converting failure.
     */
    public static JSONObjectProvider convertObject(Object javaBean) throws JSONConvertionException {
        return convertObject(new JSONObjectProvider(), javaBean);
    }

    /**
     * Converts a JavaBean object into a _JSONObject_.
     *
     * @param jsonObject The jsonObject to convert the bean into or null for a new JSONObject.
     * @param javaBean The JavaBean object to convert.
     *
     * @return A JSONObject containing all values from the JavaBean.
     *
     * @throws JSONConvertionException on converting failure.
     */
    public static JSONObjectProvider convertObject(JSONObjectProvider jsonObject, Object javaBean) throws JSONConvertionException {
        try {
            JSONObjectProvider obj = jsonObject;
            if (obj == null) {
                obj = new JSONObjectProvider();
            }

            if (Dictionary.class.isAssignableFrom(javaBean.getClass())) {
                Enumeration dictEnum = ((Dictionary) javaBean).keys();
                while (dictEnum.hasMoreElements()) {
                    Object key = dictEnum.nextElement();
                    String value = ((Dictionary) javaBean).get(key).toString();
                    obj.addValue(key.toString(), new JSONStringProvider(value));
                }
            } else if (Map.class.isAssignableFrom(javaBean.getClass())) {
                for (Object key : ((Map) javaBean).keySet()) {
                    Object value = ((Map) javaBean).get(key);
                    obj.addValue(key.toString(), convertValue(value));
                }
            } else {
                for (Method method : javaBean.getClass().getMethods()) {
                    if (
                            !method.getName().equals("getClass") &&
                                    (
                                            method.getName().startsWith("is") ||
                                                    (
                                                            method.getName().startsWith("get") &&
                                                                    method.getName().length() > 3
                                                    )
                                    )
                            ) {
                        Object value;
                        String prop = null;

                        if (method.getName().startsWith("get")) {
                            prop = method.getName().substring(3);
                        } else if (method.getName().startsWith("is")) {
                            prop = method.getName().substring(2);
                        }
                        if (prop != null) {
                            prop = prop.substring(0, 1).toLowerCase() + prop.substring(1);
                        } else {
                            prop = "bug"; // This should not happen! :-)
                        }

                        try {
                            value = method.invoke(javaBean);
                        } catch (Exception e) {
                            value = e.getMessage();
                        }

                        obj.addValue(prop, convertValue(value));
                    }
                }
            }

            return obj;
        }
        catch (Exception e) {
            throw new JSONConvertionException("Failed to convert Java to JSON! (" + e.getMessage() + ")", e);
        }
    }

    /**
     * Converts a value from a java value to a _JSONValue_.
     *
     * @param value The java value to convert. It can be one of String, Number, Boolean, null, JavaBean, or an array of those.
     *              If you pass in something else you will get an empty JSONObject back!
     *
     * @return The converted JSONValue.
     */
    public static JSONValueProvider convertValue(Object value) {
        JSONValueProvider json;

        if (value == null) {
            json = new JSONNullProvider();
        }
        else if (Date.class.isAssignableFrom(value.getClass()) || Calendar.class.isAssignableFrom(value.getClass()) || Temporal.class.isAssignableFrom(value.getClass())) {
            json = new JSONStringProvider(value.toString());
        }
        else if (
                Number.class.isAssignableFrom(value.getClass()) ||
                byte.class.isAssignableFrom(value.getClass()) ||
                double.class.isAssignableFrom(value.getClass()) ||
                float.class.isAssignableFrom(value.getClass()) ||
                int.class.isAssignableFrom(value.getClass()) ||
                long.class.isAssignableFrom(value.getClass()) ||
                short.class.isAssignableFrom(value.getClass())
                ) {
            json = new JSONNumberProvider((Number)value);
        }
        else if (String.class.isAssignableFrom(value.getClass())) {
            json = new JSONStringProvider((String)value);
        }
        else if (boolean.class.isAssignableFrom(value.getClass()) || Boolean.class.isAssignableFrom(value.getClass())) {
            json = new JSONBooleanProvider((Boolean)value);
        }
        else if (Date.class.isAssignableFrom(value.getClass())) {
            json = new JSONNumberProvider(((Date)value).getTime());
        }
        else if (value.getClass().isArray()) {
            JSONArrayProvider array = new JSONArrayProvider();
            int length = Array.getLength(value);
            for (int i = 0; i < length; i++) {
                Object aValue = Array.get(value, i);
                array.addValue(convertValue(aValue));
            }
            json = array;
        }
        else if (Collection.class.isAssignableFrom(value.getClass())) {
            JSONArrayProvider array = new JSONArrayProvider();
            for (Object cValue : (Collection)value) {
                array.addValue(convertValue(cValue));
            }
            json = array;
        }
        else if (value.getClass().isEnum()) {
            json = new JSONStringProvider(((Enum)value).name());
        }
        else { // Treat as object
            json = convertObject(value);
        }

        return json;
    }
}
