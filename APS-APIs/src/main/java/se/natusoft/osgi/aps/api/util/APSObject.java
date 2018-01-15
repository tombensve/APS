package se.natusoft.osgi.aps.api.util;

import se.natusoft.osgi.aps.api.reactive.APSValue;

import java.util.function.Consumer;

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
    public T value() {
        return this.object;
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
        return value();
    }

    public APSObject onBoolean(Consumer<Boolean> consumer) {
        if(isBoolean()) {
            consumer.accept(getBoolean());
        }

        return this;
    }

    public APSObject onTrue(Consumer<Boolean> consumer) {
        if (isBoolean() && getBoolean()) {
            consumer.accept(getBoolean());
        }

        return this;
    }

    public APSObject onFalse(Consumer<Boolean> consumer) {
        if (isBoolean() && !getBoolean()) {
            consumer.accept(getBoolean());
        }

        return this;
    }

    public APSObject onString(Consumer<String> consumer) {
        if (isString()) {
            consumer.accept(getString());
        }

        return this;
    }

    public APSObject onNumber(Consumer<Number> consumer) {
        if (isNumber()) {
            consumer.accept(getNumber());
        }

        return this;
    }

    public APSObject onDecimals(Consumer<Double> consumer) {
        if (isDouble() || isFloat()) {
            consumer.accept((Double)this.object);
        }

        return this;
    }

    public APSObject onIntegers(Consumer<Long> consumer) {
        if (isInt() || isLong()) {
            consumer.accept((Long)this.object);
        }

        return this;
    }

    public APSObject onUnknownObject(Consumer<Object> consumer) {
        if (isUnknownObject()) {
            consumer.accept(this.object);
        }

        return this;
    }
}
