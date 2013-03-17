/* 
 * 
 * PROJECT
 *     Name
 *         APS JSON Library
 *     
 *     Code Version
 *         0.9.1
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
 *         2012-01-06: Created!
 *         
 */
package se.natusoft.osgi.aps.json.tools;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This wraps a Java Bean instance allowing it to be populated with data using setProperty(String, Object) methods
 * handling all reflection calls.
 */
public class BeanInstance {
    //
    // Private Members
    //

    /** The test model instance. */
    private Object modelInstance = null;

    //
    // Constructors
    //

    /**
     * Creates a new ModelInstance.
     *
     * @param modelInstance The model instance to wrap.
     */
    public BeanInstance(Object modelInstance) {
        this.modelInstance = modelInstance;
    }

    //
    // Methods
    //

    /**
     * Returns the test model instance held by this object.
     */
    public Object getModelInstance() {
        return this.modelInstance;
    }

    /**
     * Returns a list of settable properties.
     */
    public List<String> getSettableProperties() {
        List<String> properties = new ArrayList<String>();
        for (Method method : this.modelInstance.getClass().getMethods()) {
            if (method.getName().startsWith("set") && method.getParameterTypes().length == 1) {
                String prop = method.getName().substring(3);
                prop = prop.substring(0,1).toLowerCase() + prop.substring(1);
                properties.add(prop);
            }
        }

        return properties;
    }

    /**
     * Returns a list of gettable properties.
     */
    public List<String> getGettableProperties() {
        List<String> properties = new ArrayList<String>();
        for (Method method : this.modelInstance.getClass().getMethods()) {
            if ((method.getName().startsWith("get") || method.getName().startsWith("is")) && method.getParameterTypes().length == 0) {
                String prop = method.getName().substring(2);
                if (method.getName().startsWith("get")) {
                    prop = prop.substring(1);
                }
                prop = prop.substring(0,1).toLowerCase() + prop.substring(1);
                properties.add(prop);
            }
        }

        return properties;
    }

    /**
     * Sets a property
     *
     * @param property The name of the property to set.
     * @param value The value to set with.
     *
     * @exception JSONConvertionException on any failure to set the property.
     */
    public void setProperty(String property, Object value) throws JSONConvertionException {
        Class propertyType = null;
        try {
            propertyType = getPropertyType(property);

            boolean doSetValue = true;
//            if (value == null && "int long byte short boolean double float".indexOf(propertyType.getSimpleName()) >= 0) {
//                doSetValue = false;
//            }

            if (doSetValue) {
                String methodName = "set" + property.substring(0,1).toUpperCase() + property.substring(1);
                Method setter = this.modelInstance.getClass().getMethod(methodName, propertyType);

                Object setValue = convertToPropType(value, propertyType);

                setter.invoke(this.modelInstance, setValue);
            }
        }
        catch (Exception e) {
            throw new JSONConvertionException("Failed to set property '" + property + "[" + propertyType.getSimpleName() + "]' with object '" + value + "[" + value.getClass().getSimpleName() + "]' in model '" + this.modelInstance.getClass().getName() + "! Cause: " + e.getMessage(), e);
        }
    }

    /**
     * Returns the value of the specified property.
     * 
     * @param property The property to return value of.
     *                 
     * @return The property value.
     * 
     * @throws JSONConvertionException on failure (probably bad property name!).
     */
    public Object getProperty(String property) throws JSONConvertionException {
        try {
            String methodName = null;
            Class propertyType = getPropertyType(property);
            if (Boolean.class.isAssignableFrom(propertyType) || boolean.class.isAssignableFrom(propertyType)) {
                methodName = "is";
            }
            else {
                methodName ="get";
            }
            methodName += property.substring(0,1).toUpperCase() + property.substring(1);
            Method getter = this.modelInstance.getClass().getMethod(methodName);
            
            return getter.invoke(this.modelInstance);
        }
        catch (Exception e) {
            throw new JSONConvertionException("Failed to get property '" + property + "' due to '" + e.getMessage() + "'!", e);
        }
    }
    
    /**
     * Converts a property value to the correct type for the property.
     *
     * @param value The value to convert.
     * @param propType The type of the property.
     *
     * @throws JSONConvertionException on failure.
     */
    private Object convertToPropType(Object value, Class propType) throws JSONConvertionException {
        Object setValue = null;

        if (value instanceof String) {
            String strValue = (String)value;
            if (byte.class.isAssignableFrom(propType) || Byte.class.isAssignableFrom(propType)) {
                setValue = Byte.valueOf(strValue);
            }
            else if (short.class.isAssignableFrom(propType) || Short.class.isAssignableFrom(propType)) {
                setValue = Short.valueOf(strValue);
            }
            else if (int.class.isAssignableFrom(propType) || Integer.class.isAssignableFrom(propType)) {
                setValue = Integer.valueOf(strValue);
            }
            else if (long.class.isAssignableFrom(propType) || Long.class.isAssignableFrom(propType)) {
                setValue = Long.valueOf(strValue);
            }
            else if (float.class.isAssignableFrom(propType) || Float.class.isAssignableFrom(propType)) {
                setValue = Float.valueOf(strValue);
            }
            else if (double.class.isAssignableFrom(propType) || Double.class.isAssignableFrom(propType)) {
                setValue = Double.valueOf(strValue);
            }
            else if (boolean.class.isAssignableFrom(propType) || Boolean.class.isAssignableFrom(propType)) {
                setValue = Boolean.valueOf(strValue);
            }
            else if (char.class.isAssignableFrom(propType) || Character.class.isAssignableFrom(propType)) {
                setValue = Character.valueOf(((String)strValue).charAt(0));
            }
            else if (Date.class.isAssignableFrom(propType)) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    setValue = sdf.parse(strValue);
                }
                catch (ParseException pe) {
                    sdf = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
                    try {
                        setValue = sdf.parse(strValue);
                    }
                    catch (ParseException pe1) {
                        sdf = new SimpleDateFormat("HH:mm:ss");
                        try {
                            setValue = sdf.parse(strValue);
                        }
                        catch (ParseException pe2) {
                            sdf = new SimpleDateFormat("yyyy-MM-dd");
                            try {
                                setValue = sdf.parse(strValue);
                            }
                            catch (ParseException pe3) {
                                sdf = new SimpleDateFormat("yy-MM-dd");
                                try {
                                    setValue = sdf.parse(strValue);
                                }
                                catch (ParseException pe4) {
                                    throw new JSONConvertionException("Date string in unknown format!");
                                }
                            }
                        }
                    }
                }
            }
            else if (propType.isEnum()) {
                for (Object enumConstObj : propType.getEnumConstants()) {
                    Enum enumConst = (Enum)enumConstObj;
                    if (enumConst.name().equals(value)) {
                        setValue = enumConst;
                        break;
                    }
                }
                if (setValue == null) {
                    throw new JSONConvertionException("Bad enum const value: '" + value + "'!");
                }
            }
            else {
                setValue = value;
            }
        }
        else if (value instanceof List) {
            if (propType.isArray()) {
                // Convert entries in list.
                List valueList = (List)value;
                Object array = Array.newInstance(propType.getComponentType(), valueList.size());
                for (int i = 0 ; i < valueList.size(); i++) {
                    Object entry = valueList.get(i);
                    Array.set(array, i, convertToPropType(entry, propType));
                }
                setValue = array;
            }
            else if (List.class.isAssignableFrom(propType)) {
                setValue = value;
            }
            else {
                throw new JSONConvertionException("An array have been provided in input, but model value (" + propType.getName() + ") is not an array!");
            }
        }
        else if ((long.class.isAssignableFrom(value.getClass()) || Long.class.isAssignableFrom(value.getClass())) && Date.class.isAssignableFrom(propType)) {
            setValue = new Date((Long)value);
        }
        else { // int, long, Date, etc and null will fall in here and just be returned as passed.
            setValue = value;
        }

        return setValue;
    }
    
    /**
     * Returns the type of the specified property.
     *
     * @param property The property to get the type for.
     *
     * @return The class representing the property type.
     *
     * @throws JSONConvertionException if property does not exist.
     */
    public Class getPropertyType(String property) throws JSONConvertionException {
        String methodName = "get" + property.substring(0,1).toUpperCase() + property.substring(1);
        try {
            Class[] params = new Class[0];
            Method getter = this.modelInstance.getClass().getMethod(methodName, params);
            return getter.getReturnType();
        }
        catch (NoSuchMethodException nsme) {
            methodName = "is" + property.substring(0,1).toUpperCase() + property.substring(1);
            try {
                Class[] params = new Class[0];
                Method getter = this.modelInstance.getClass().getMethod(methodName, params);
                return getter.getReturnType();
            }
            catch (NoSuchMethodException nsme2) {
                throw new JSONConvertionException("Property of name '" + property + "' does not exist!");
            }
        }
    }
}
