/*
 *
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         1.0.0
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
 *     tommy ()
 *         Changes:
 *         2018-05-26: Created!
 *
 */
package se.natusoft.osgi.aps.types;

/**
 * This is a utility to wrap an object and easily check for what it is among String, number, boolean, or object.
 *
 * Note that it also implements APSValue!
 *
 * @param <T>
 */
public class APSObject<T> implements APSValue<T> {

    //
    // Private Members
    //

    /** The object we wrap. */
    private T object;

    //
    // Constructors
    //

    /**
     * Creates a new APSObject.
     *
     * @param object The object to wrap.
     */
    public APSObject(T object) {
        this.object = object;
    }

    //
    // Methods
    //

    /**
     * @return The held value.
     */
    @Override
    public T content() {
        return this.object;
    }

    /**
     * Updates the value.
     *
     * @param val The new value to set.
     */
    @Override
    public void content(T val) {
        this.object = val;
    }

    public boolean isNull() {
        return this.object == null;
    }

    public Class type() {
        return getType();
    }

    public Class getType() {
        return this.object.getClass();
    }

    public boolean isBoolean() {
        return Boolean.class.isAssignableFrom(this.object.getClass());
    }

    public boolean isString() {
        return String.class.isAssignableFrom(this.object.getClass());
    }

    public boolean isNumber() {
        return Number.class.isAssignableFrom(this.object.getClass());
    }

    public boolean isInt() {
        return Integer.class.isAssignableFrom(this.object.getClass());
    }

    public boolean isLong() {
        return Long.class.isAssignableFrom(this.object.getClass());
    }

    public boolean isFloat() {
        return Float.class.isAssignableFrom(this.object.getClass());
    }

    public boolean isDouble() {
        return Double.class.isAssignableFrom(this.object.getClass());
    }

    public boolean isUnknownObject() {
        return !isBoolean() && !isString() && !isNumber();
    }

    public boolean is(Class<?> clazz) {
        return clazz.isAssignableFrom(this.object.getClass());
    }

    public boolean getBoolean() {
        return (Boolean)this.object;
    }

    public String getString() {
        return (String)this.object;
    }

    public Number getNumber() {
        return (Number)this.object;
    }

    public int getInt() {
        return (Integer)this.object;
    }

    public long getLong() {
        return (Long)this.object;
    }

    public float getFloat() {
        return (Float)this.object;
    }

    public double getDouble() {
        return (Double)this.object;
    }

    public Object getUnknownObject() {
        return content();
    }

    public APSObject onBoolean(APSHandler<Boolean> handler) {
        if(isBoolean()) {
            handler.handle(getBoolean());
        }

        return this;
    }

    public APSObject onTrue(APSHandler<Boolean> handler) {
        if (isBoolean() && getBoolean()) {
            handler.handle(getBoolean());
        }

        return this;
    }

    public APSObject onFalse(APSHandler<Boolean> handler) {
        if (isBoolean() && !getBoolean()) {
            handler.handle(getBoolean());
        }

        return this;
    }

    public APSObject onString(APSHandler<String> handler) {
        if (isString()) {
            handler.handle(getString());
        }

        return this;
    }

    public APSObject onNumber(APSHandler<Number> handler) {
        if (isNumber()) {
            handler.handle(getNumber());
        }

        return this;
    }

    public APSObject onDecimals(APSHandler<Double> handler) {
        if (isDouble() || isFloat()) {
            handler.handle((Double)this.object);
        }

        return this;
    }

    public APSObject onIntegers(APSHandler<Long> handler) {
        if (isInt() || isLong()) {
            handler.handle((Long)this.object);
        }

        return this;
    }

    public APSObject onUnknownObject(APSHandler<Object> handler) {
        if (isUnknownObject()) {
            handler.handle(this.object);
        }

        return this;
    }

    public APSObject onAvailable(APSHandler<T> handler) {
        if (this.object != null) {
            handler.handle(this.object);
        }

        return this;
    }

    public APSObject onNull(APSHandler<T> handler) {
        if (this.object == null) {
            handler.handle(null);
        }

        return this;
    }
}
