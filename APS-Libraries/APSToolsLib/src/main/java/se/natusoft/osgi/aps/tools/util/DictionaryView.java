package se.natusoft.osgi.aps.tools.util;

import se.natusoft.osgi.aps.tools.APSLogger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Provides a Dictionary view of a Java Bean. This is very useful in conjunction with OSGi:s FrameworkUtil.createFilter(...).
 *
 *     Filter filter = FrameworkUtil.createFilter("(&(serviceId=aps-platform-service)(serviceHost=duck.dev.whatever.se)");
 *     DictionaryView dictView = new DictionaryView(ServiceDescription.class, serviceDescription, this.logger)
 *     if (filter.match(dictView)) {...}
 */
public class DictionaryView extends Dictionary<String, String> {

    //
    // Private Members
    //

    /** The logger to use when things go due south. */
    APSLogger logger = new APSLogger();

    /** The size of the Dictionary. This is calculated in the constructor. */
    private int size = 0;

    /** The instance to call on get. */
    private Object instance = null;

    /** The extracted keys. */
    private Vector<String> keys = new Vector<>();

    /** The extracted getter methods mapped to their extracted keys. */
    private Map<String, Method> getters = new HashMap<>();

    //
    // Constructors
    //

    /**
     * Creates a new ServiceDescriptionDictionary.
     *
     * @param api A class representing the Java bean API.
     * @param instance The instance to make a Dictionary view of.
     */
    public DictionaryView(Class api, Object instance) {
        this(api, instance, null);
    }

    /**
     * Creates a new ServiceDescriptionDictionary.
     *
     * @param api A class representing the Java bean API.
     * @param instance The instance to make a Dictionary view of.
     * @param logger For logging failures. Can be null.
     */
    public DictionaryView(Class api, Object instance, APSLogger logger) {
        this.instance = instance;
        for (Method method : api.getDeclaredMethods()) {
            if (method.getName().startsWith("get")) {
                ++this.size;
                String key = method.getName().substring(3,4).toLowerCase();
                if (method.getName().length() > 4) {
                    key += method.getName().substring(4);
                }
                this.keys.add(key);
                this.getters.put(key, method);
            }
            else if (method.getName().startsWith("is")) {
                ++this.size;
                String key = method.getName().substring(2,3).toLowerCase();
                if (method.getName().length() > 3) {
                    key += method.getName().substring(3);
                }
                this.keys.add(key);
                this.getters.put(key, method);
            }
        }

        if (logger != null) {
            this.logger = logger;
        }
    }

    /**
     * Returns the number of entries (distinct keys) in this dictionary.
     *
     * @return the number of keys in this dictionary.
     */
    @Override
    public int size() {
        return this.size;
    }

    /**
     * Tests if this dictionary maps no keys to value. The general contract
     * for the <tt>isEmpty</tt> method is that the result is true if and only
     * if this dictionary contains no entries.
     *
     * @return <code>true</code> if this dictionary maps no keys to values;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean isEmpty() {
        return this.keys.isEmpty();
    }

    /**
     * Returns an enumeration of the keys in this dictionary. The general
     * contract for the keys method is that an <tt>Enumeration</tt> object
     * is returned that will generate all the keys for which this dictionary
     * contains entries.
     *
     * @return an enumeration of the keys in this dictionary.
     * @see Dictionary#elements()
     * @see Enumeration
     */
    @Override
    public Enumeration<String> keys() {
        return this.keys.elements();
    }

    /**
     * Returns an enumeration of the values in this dictionary. The general
     * contract for the <tt>elements</tt> method is that an
     * <tt>Enumeration</tt> is returned that will generate all the elements
     * contained in entries in this dictionary.
     *
     * @return an enumeration of the values in this dictionary.
     * @see Dictionary#keys()
     * @see Enumeration
     */
    @Override
    public Enumeration<String> elements() {
        Vector<String> elems = new Vector<>();
        for (String key : this.keys) {
            elems.add(get(key));
        }
        return elems.elements();
    }

    /**
     * Returns the value to which the key is mapped in this dictionary.
     * The general contract for the <tt>isEmpty</tt> method is that if this
     * dictionary contains an entry for the specified key, the associated
     * value is returned; otherwise, <tt>null</tt> is returned.
     *
     * @param key a key in this dictionary.
     *            <code>null</code> if the key is not mapped to any value in
     *            this dictionary.
     * @return the value to which the key is mapped in this dictionary;
     * @throws NullPointerException if the <tt>key</tt> is <tt>null</tt>.
     * @see Dictionary#put(Object, Object)
     */
    @Override
    public String get(Object key) {
        String result = null;

        Method getter = this.getters.get(key.toString());
        if (getter != null) {
            try {
                result = getter.invoke(this.instance).toString();
            }
            catch (IllegalAccessException | InvocationTargetException e) {
                this.logger.error("DictionaryView.get(\"" + key + "\") failed!", e);
            }
        }

        return result;
    }

    /**
     * Maps the specified <code>key</code> to the specified
     * <code>value</code> in this dictionary. Neither the key nor the
     * value can be <code>null</code>.
     * <p>
     * If this dictionary already contains an entry for the specified
     * <tt>key</tt>, the value already in this dictionary for that
     * <tt>key</tt> is returned, after modifying the entry to contain the
     * new element. <p>If this dictionary does not already have an entry
     * for the specified <tt>key</tt>, an entry is created for the
     * specified <tt>key</tt> and <tt>value</tt>, and <tt>null</tt> is
     * returned.
     * <p>
     * The <code>value</code> can be retrieved by calling the
     * <code>get</code> method with a <code>key</code> that is equal to
     * the original <code>key</code>.
     *
     * @param key   the hashtable key.
     * @param value the value.
     * @return the previous value to which the <code>key</code> was mapped
     * in this dictionary, or <code>null</code> if the key did not
     * have a previous mapping.
     * @throws NullPointerException if the <code>key</code> or
     *                              <code>value</code> is <code>null</code>.
     * @see Object#equals(Object)
     * @see Dictionary#get(Object)
     */
    @Override
    public String put(String key, String value) {
        throw new RuntimeException("This Dictionary is read only! DictionaryView.put(...) is not supported!");
    }

    /**
     * Removes the <code>key</code> (and its corresponding
     * <code>value</code>) from this dictionary. This method does nothing
     * if the <code>key</code> is not in this dictionary.
     *
     * @param key the key that needs to be removed.
     * @return the value to which the <code>key</code> had been mapped in this
     * dictionary, or <code>null</code> if the key did not have a
     * mapping.
     * @throws NullPointerException if <tt>key</tt> is <tt>null</tt>.
     */
    @Override
    public String remove(Object key) {
        throw new RuntimeException("This Dictionary is read only! DictionaryView.removes(...) is not supported!");
    }
}
