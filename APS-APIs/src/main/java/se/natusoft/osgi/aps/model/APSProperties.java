package se.natusoft.osgi.aps.model;

import se.natusoft.osgi.aps.exceptions.APSValidationException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Utility for providing Map\<String, String\> properties.
 *
 * Note that the defined constants are only utilities! They are not required in any way. Whatever is put in an APSProperties
 * instance is up to the user. This whole class is just a utility to put a clearer name on a specific usage of
 * Map&lt;String, String&gt;.
 *
 * Also note that there is a "Provider" inner class that provides a default implementation. It provides utility methods
 * for adding data to the map in a fluent way. Many of them are Groovy operators that can only be used from Groovy in
 * operator form.
 *
 * The APIs are in Java, and some libraries are in Java, but most of the services are done in Groovy.
 */
@SuppressWarnings("unused")
public interface APSProperties extends Map<String, String> {

    //
    // Utils
    //

    static APSProperties.Provider props() {
        return new APSProperties.Provider();
    }

    //
    // Methods
    //

    /**
     * For static initialization with values.
     *
     * @param initial The initial Map to set.
     */
    static APSProperties with(Map<String, String> initial) {
        APSProperties properties = new APSProperties.Provider();
        properties.putAll(initial);
        return properties;
    }

    /**
     * @return A new empty APSMeta object.
     */
    static APSProperties empty() {
        return new APSProperties.Provider();
    }

    //
    // Default implementation
    //

    /**
     * Provides a default implementation of the APSMeta interface.
     */
    class Provider extends LinkedHashMap<String, String> implements APSProperties {

        //
        // Java, etc Support Methods
        //

        /**
         * Adds an entry in "key:value" format.
         *
         * @param keyColonValue The key and value to add.
         */
        public APSProperties.Provider add(String keyColonValue) {
            leftShift(keyColonValue);
            return this;
        }

        /**
         * Adds another map into this map.
         *
         * @param map The map to add.
         */
        public APSProperties.Provider add(Map<String, String> map) {
            this.putAll(map);

            return this;
        }

        //
        // Groovy Support Methods
        //

        private String key = null;

        /**
         * Adds a key or a value depending on if a key has been provided previously or not on >> operator.
         *
         * @param keyOrValue The key or value content.
         */
        @SuppressWarnings("WeakerAccess")
        public APSProperties.Provider rightShift(String keyOrValue) {
            if (this.key == null) {
                this.key = keyOrValue;
            }
            else {
                put(this.key, keyOrValue);
                this.key = null;
            }

            return this;
        }

        /**
         * Adds an entry in "key:value" or "key" << "value" format.
         *
         * Any spaces around key and value in "key:value" will be removed. In other words,
         * "key:value", "key : value", "key: value" are all the same!
         *
         * Provides Groovy '<<' operator.
         *
         * @param keyColonValue The key and value to add.
         */
        @SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
        public APSProperties.Provider leftShift(String keyColonValue) {
            String[] parts = keyColonValue.split(":");
            if (parts.length >= 2) {
                put(parts[0].trim(), parts[1].trim());
            }
            else {
                throw new APSValidationException("There can be only one ':' char! Ex: \"key: value\". Got:" + keyColonValue);
            }

            return this;
        }

        /**
         * Lets + operator do the same as right shift. If some thinks that is clearer than right shift.
         *
         * @param keyOrValue The key or value. Must be called first with key, then with value.
         */
        public APSProperties.Provider plus(String keyOrValue) {
            return rightShift(keyOrValue);
        }

        /**
         * For Java.
         *
         * @param key The key to set. This must be called before value(...).
         */
        public APSProperties.Provider key(String key) {
            return rightShift(key);
        }

        /**
         * For Java.
         *
         * @param value The value to set. key(...) must have been called before this.
         */
        public APSProperties.Provider value(String value) {
            return rightShift(value);
        }

        /**
         * Adds another map into this map. Provides Groovby '<<' operator.
         *
         * @param map The map to add.
         */
        @SuppressWarnings("WeakerAccess")
        public APSProperties.Provider leftShift(Map<String, String> map) {
            this.putAll(map);

            return this;
        }

        /**
         * Adds a whole Map to existing Map.
         *
         * @param map The Map to add.
         */
        public APSProperties.Provider plus(Map<String, String> map) {
            return leftShift(map);
        }
    }
}

